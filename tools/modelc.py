import re
import json
import sys


# ==========================================
# 1. ПАРСИНГ JAVA КОДА
# ==========================================

def parse_java(java_code):
    # Убираем переносы строк и лишние пробелы для упрощения regex
    clean_code = re.sub(r'\s+', ' ', java_code)

    # Регулярка для поиска определений частей
    # Ловит: [PartDefinition var =] parent.addOrReplaceChild("name", cubes, pose)
    pattern = re.compile(
        r'(?:PartDefinition\s+(?P<var_name>\w+)\s*=\s*)?'  # Объявление переменной (опционально)
        r'(?P<parent_var>\w+)\.addOrReplaceChild\s*\(\s*'  # Родительская переменная
        r'"(?P<part_name>\w+)"\s*,\s*'  # Имя части
        r'CubeListBuilder\.create\(\)(?P<cubes_data>.*?),\s*'  # Данные кубов
        r'PartPose\.(?P<pose_type>ZERO|offset)\s*\((?P<pose_args>[^\)]*)\)\s*\);'  # Позиция (Pivot)
    )

    parts_db = {}

    # Создаем фиктивный корень (partdefinition из аргументов метода)
    parts_db["partdefinition"] = {
        "id": "root",
        "name": "root",
        "var_name": "partdefinition",
        "parent_var": None,
        "offset": [0.0, 0.0, 0.0],
        "cubes": [],
        "children": []
    }

    # Ищем все совпадения
    for match in pattern.finditer(clean_code):
        var_name = match.group("var_name")
        parent_var = match.group("parent_var")
        part_name = match.group("part_name")
        cubes_raw = match.group("cubes_data")
        pose_type = match.group("pose_type")
        pose_args = match.group("pose_args")

        # Если переменная не присвоена явно (напр. lid.addOrReplaceChild),
        # то используем имя части как ID для внутренней логики,
        # но это не создаст новую переменную для привязки детей.
        # Однако, если дальше идет addOrReplaceChild к этому имени, нам нужен ID.
        # В твоем коде: lid.addOrReplaceChild... -> родитель "lid".
        current_id = var_name if var_name else part_name

        # Парсим Offset (Pivot)
        ox, oy, oz = 0.0, 0.0, 0.0
        if pose_type == "offset":
            args = [float(x.strip().replace('F', '')) for x in pose_args.split(',')]
            if len(args) >= 3:
                ox, oy, oz = args[0], args[1], args[2]

        # Парсим Кубы
        cubes = []
        # Ищем .addBox(...)
        box_pattern = re.compile(r'\.addBox\((?P<args>[^\)]+)\)')
        for box_match in box_pattern.finditer(cubes_raw):
            b_args = [float(x.strip().replace('F', '')) for x in box_match.group("args").split(',')]
            # Java addBox: x, y, z, w, h, d
            if len(b_args) >= 6:
                cubes.append({
                    "off": [b_args[0], b_args[1], b_args[2]],
                    "size": [b_args[3], b_args[4], b_args[5]]
                })

        # Сохраняем часть
        parts_db[current_id] = {
            "id": current_id,
            "name": part_name,
            "var_name": var_name,
            "parent_var": parent_var,
            "offset": [ox, oy, oz],
            "cubes": cubes,
            "children": []
        }

    # Строим дерево (связываем детей с родителями)
    # Нам нужно найти реальный объект родителя по имени переменной
    root_nodes = []

    # Маппинг: имя_переменной -> id части
    var_map = {"partdefinition": "partdefinition"}
    for pid, pdata in parts_db.items():
        if pdata["var_name"]:
            var_map[pdata["var_name"]] = pid

    for pid, pdata in parts_db.items():
        if pid == "partdefinition": continue

        parent_id = var_map.get(pdata["parent_var"])
        if parent_id and parent_id in parts_db:
            parts_db[parent_id]["children"].append(pdata)
        else:
            # Если родитель не найден, кидаем в корень (fallback)
            parts_db["partdefinition"]["children"].append(pdata)

    return parts_db["partdefinition"]["children"]


# ==========================================
# 2. КОНВЕРТАЦИЯ В BLOCKBENCH JSON
# ==========================================

def convert_to_bb(tree_nodes):
    elements = []
    groups = []

    # Глобальный счетчик для индексов элементов
    elem_index = 0

    def process_node(node, parent_abs_origin):
        nonlocal elem_index

        # 1. Рассчитываем абсолютную точку вращения (Origin) для этой группы
        # В Java offset относителен родителю. В BB origin абсолютен.
        abs_origin = [
            parent_abs_origin[0] + node["offset"][0],
            parent_abs_origin[1] + node["offset"][1],
            parent_abs_origin[2] + node["offset"][2]
        ]

        group_data = {
            "name": node["name"],
            "origin": abs_origin,
            "color": 0,
            "children": []
        }

        # 2. Создаем элементы (кубы)
        for cube in node["cubes"]:
            # Координаты куба в Java задаются относительно Pivot (abs_origin)
            # BB From = Origin + CubeOffset

            c_off = cube["off"]
            c_size = cube["size"]

            # From
            fx = abs_origin[0] + c_off[0]
            fy = abs_origin[1] + c_off[1]
            fz = abs_origin[2] + c_off[2]

            # To
            tx = fx + c_size[0]
            ty = fy + c_size[1]
            tz = fz + c_size[2]

            element = {
                "from": [fx, fy, fz],
                "to": [tx, ty, tz],
                "color": 2,  # Цвет в редакторе
                "faces": {
                    "north": {"uv": [0, 0, 1, 1], "texture": "#0"},
                    "east": {"uv": [0, 0, 1, 1], "texture": "#0"},
                    "south": {"uv": [0, 0, 1, 1], "texture": "#0"},
                    "west": {"uv": [0, 0, 1, 1], "texture": "#0"},
                    "up": {"uv": [0, 0, 1, 1], "texture": "#0"},
                    "down": {"uv": [0, 0, 1, 1], "texture": "#0"}
                }
            }

            elements.append(element)
            group_data["children"].append(elem_index)
            elem_index += 1

        # 3. Рекурсивно обрабатываем детей
        for child in node["children"]:
            child_group = process_node(child, abs_origin)
            group_data["children"].append(child_group)

        return group_data

    # Запускаем обработку для корневых узлов
    # Начальный origin [0,0,0], так как PartPose.ZERO у корня
    for node in tree_nodes:
        groups.append(process_node(node, [0, 0, 0]))

    return {
        "format_version": "1.9.0",
        "credit": "Converted from Java",
        "texture_size": [64, 64],
        "textures": {
            "0": "missing",
            "particle": "missing"
        },
        "elements": elements,
        "groups": groups
    }


# ==========================================
# 3. ЗАПУСК
# ==========================================

# Вставь свой Java код сюда (тело метода)
java_source = """
        PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create()
                        .texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F),
                PartPose.ZERO);

        PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 9.0F, 1.0F));

        lid.addOrReplaceChild("lock", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
"""

if __name__ == "__main__":
    # Парсим
    tree = parse_java(java_source)
    # Конвертируем
    bb_model = convert_to_bb(tree)
    # Выводим JSON
    print(json.dumps(bb_model, indent=4))
import os
import json
from PIL import Image

TILE_SIZE = 16

# === Настройки ===
ATLAS_FILES = {
    "dry": "farmland_dry_full_.png",
    "wet_overlay": "farmland_wet_overlay_full_spring.png"
}

SHAPES = {
    "single": (0, 0),
    "horizontal_left": (1, 3),
    "horizontal_mid": (2, 3),
    "horizontal_right": (3, 3),
    "vertical_top": (0, 1),
    "vertical_mid": (0, 2),
    "vertical_bottom": (0, 3),
    "top_left": (1, 0),
    "top": (2, 0),
    "top_right": (3, 0),
    "left": (1, 1),
    "center": (2, 1),
    "right": (3, 1),
    "bottom_left": (1, 2),
    "bottom": (2, 2),
    "bottom_right": (3, 2)
}

# === Пути ===
SPLIT_FOLDER = "split_farmland"
MODELS_FOLDER = "generated_models/block/farmland"
BLOCKSTATES_FOLDER = "generated_models/blockstates"

# === Создание директорий ===
os.makedirs(SPLIT_FOLDER, exist_ok=True)
os.makedirs(MODELS_FOLDER, exist_ok=True)
os.makedirs(BLOCKSTATES_FOLDER, exist_ok=True)

# === Нарезка атласов ===
for prefix, atlas_path in ATLAS_FILES.items():
    img = Image.open(atlas_path)
    for name, (x, y) in SHAPES.items():
        box = (x * TILE_SIZE, y * TILE_SIZE, (x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE)
        tile = img.crop(box)
        tile.save(os.path.join(SPLIT_FOLDER, f"{prefix}_{name}.png"))
print("✅ Атласы нарезаны.")

# === Генерация моделей (cube_all) ===
available = set(f.replace(".png", "") for f in os.listdir(SPLIT_FOLDER))

for tex in available:
    tex_path = f"stardew:block/farmland/{tex}"
    model_json = {
        "parent": "block/cube_all",
        "textures": {"all": tex_path}
    }
    with open(os.path.join(MODELS_FOLDER, f"{tex}.json"), "w") as f:
        json.dump(model_json, f, indent=2)
print("✅ cube_all модели сгенерированы.")

# === Генерация multipart blockstate ===

multipart = []

# Слой dry — обязателен всегда
for shape in SHAPES:
    part = {
        "when": { "shape": shape },
        "apply": { "model": f"stardew:block/farmland/dry_{shape}" }
    }
    multipart.append(part)

# Слой overlay — только если hydrated=true
for shape in SHAPES:
    overlay_name = f"wet_overlay_{shape}"
    if overlay_name in available:
        part = {
            "when": { "hydrated": True, "wet_shape": shape },
            "apply": { "model": f"stardew:block/farmland/{overlay_name}" }
        }
        multipart.append(part)

# Сохраняем farmland.json
blockstate_json = { "multipart": multipart }

with open(os.path.join(BLOCKSTATES_FOLDER, "farmland.json"), "w") as f:
    json.dump(blockstate_json, f, indent=2)

print("✅ multipart farmland.json сгенерирован.")

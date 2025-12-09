import os

PATH = "/home/flomik/IdeaProjects/StardewMC/src/main/resources/assets/stardew/textures/block/grass/winter"

Y_FLIP_MAP = {
    "single": "single",
    "center": "center",

    "top": "bottom",
    "bottom": "top",
    "left": "right",
    "right": "left",

    "top_left": "bottom_right",
    "top_right": "bottom_left",
    "bottom_left": "top_right",
    "bottom_right": "top_left",

    "horizontal_left": "horizontal_left",
    "horizontal_mid": "horizontal_mid",
    "horizontal_right": "horizontal_right",

    "vertical_top": "vertical_bottom",
    "vertical_mid": "vertical_mid",
    "vertical_bottom": "vertical_top",

    "inner_top_left": "inner_bottom_left",
    "inner_top_right": "inner_bottom_right",
    "inner_bottom_left": "inner_top_left",
    "inner_bottom_right": "inner_top_right",
}

# shape — это ОТДЕЛЬНЫЙ токен в имени, разделённый _
SHAPES = sorted(Y_FLIP_MAP.keys(), key=len, reverse=True)

# 1. строим карту old → new
rename_map = {}

for file in os.listdir(PATH):
    if not file.endswith(".png"):
        continue

    name, ext = os.path.splitext(file)
    parts = name.split("_")

    new_parts = []
    for part in parts:
        new_parts.append(Y_FLIP_MAP.get(part, part))

    new_name = "_".join(new_parts) + ext
    rename_map[file] = new_name

# 2. ПРОВЕРКА НА КОЛЛИЗИИ
targets = list(rename_map.values())
if len(targets) != len(set(targets)):
    raise RuntimeError(
        "❌ COLLISION DETECTED:\n"
        + "\n".join(sorted(targets))
    )

# 3. TMP rename
tmp_map = {}
for src in rename_map:
    tmp = "__tmp__" + src
    os.rename(
        os.path.join(PATH, src),
        os.path.join(PATH, tmp)
    )
    tmp_map[tmp] = rename_map[src]

# 4. FINAL rename
for tmp, final in tmp_map.items():
    os.rename(
        os.path.join(PATH, tmp),
        os.path.join(PATH, final)
    )
    print(f"✔ {tmp[7:]} → {final}")

print("✅ SAFE Y-flip rename completed")

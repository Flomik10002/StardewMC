import os
import json

BASE_PATH = "/home/flomik/IdeaProjects/StardewMC/src/main/resources/assets/stardew/models/block/grass/winter"

for file in os.listdir(BASE_PATH):
    if not file.endswith(".json"):
        continue

    if file == "center.json":
        continue  # ❌ исключаем center

    path = os.path.join(BASE_PATH, file)

    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    # работаем ТОЛЬКО с нужным форматом
    if data.get("parent") != "block/cube_bottom_top":
        continue

    textures = data.get("textures", {})
    if not {"bottom", "side", "top"}.issubset(textures):
        continue

    # 🔥 целевая замена
    textures["side"] = "stardew:block/dirt/fall/dirt_1"
    data["textures"] = textures

    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

    print(f"✔ side replaced in: {file}")

print("✅ side texture replaced (center excluded)")

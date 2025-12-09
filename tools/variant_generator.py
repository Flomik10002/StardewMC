import json
import os

SEASONS = ["spring", "summer", "fall", "winter"]
VARIANTS = range(14)

BASE_PATH = "block/dirt"

TEMPLATE = {
    "parent": "block/cube_column",
    "textures": {
        "end": "stardew:block/dirt/{season}/dirt_{variant}",
        "side": "stardew:block/dirt/{season}/dirt_0"
    }
}

for season in SEASONS:
    season_path = os.path.join(BASE_PATH, season)
    os.makedirs(season_path, exist_ok=True)

    for variant in VARIANTS:
        model = {
            "parent": TEMPLATE["parent"],
            "textures": {
                "end": TEMPLATE["textures"]["end"]
                    .replace("{season}", season)
                    .replace("{variant}", str(variant)),
                "side": TEMPLATE["textures"]["side"]
                    .replace("{season}", season)
            }
        }

        file_path = os.path.join(season_path, f"dirt_{variant}.json")
        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(model, f, indent=2)

print("✔ Models generated")

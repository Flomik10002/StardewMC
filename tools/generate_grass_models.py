import os, json

# === настройки ===
modid = "stardew"
out_dir = "generated"                     # куда положить результат
base_path = f"{out_dir}/grass"
seasons = ["spring", "summer", "fall", "winter"]

shapes = [
    "single", "center",
    "top", "bottom", "left", "right",
    "top_left", "top_right", "bottom_left", "bottom_right",
    "inner_top_left", "inner_top_right", "inner_bottom_left", "inner_bottom_right"
]

def make_model(modid, season, shape):
    return {
        "parent": "block/block",
        "textures": {
            "all": f"{modid}:block/grass/{season}/{shape}"
        }
    }

# генерация
for season in seasons:
    season_dir = os.path.join(out_dir, season)
    os.makedirs(season_dir, exist_ok=True)
    for shape in shapes:
        path = os.path.join(season_dir, f"{shape}.json")
        data = make_model(modid, season, shape)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
        print(f"✔ {season}/{shape}.json")

print(f"\n✅ Сгенерировано {len(seasons)*len(shapes)} файлов в {out_dir}")

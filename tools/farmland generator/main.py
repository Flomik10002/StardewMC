import os
import json
from PIL import Image

TILE_SIZE = 16
SEASONS = ["spring", "summer", "fall", "winter"]

ATLAS_FILES = {
    "dry": "farmland_dry_full_{season}.png",
    "wet_overlay": "farmland_wet_overlay_full_{season}.png"
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

ROOT_FOLDER = "generated_models"
SPLIT_FOLDER = os.path.join(ROOT_FOLDER, "split_farmland")
MODELS_FOLDER = os.path.join(ROOT_FOLDER, "models", "block", "farmland")
BLOCKSTATE_PATH = os.path.join(ROOT_FOLDER, "blockstates", "farmland.json")

os.makedirs(SPLIT_FOLDER, exist_ok=True)
os.makedirs(MODELS_FOLDER, exist_ok=True)
os.makedirs(os.path.dirname(BLOCKSTATE_PATH), exist_ok=True)

multipart = []

for season in SEASONS:
    print(f"\n🌾 Генерация для сезона: {season}")

    season_split = os.path.join(SPLIT_FOLDER, season)
    season_models = os.path.join(MODELS_FOLDER, season)
    os.makedirs(season_split, exist_ok=True)
    os.makedirs(season_models, exist_ok=True)

    # === Нарезка атласов ===
    for prefix, atlas_template in ATLAS_FILES.items():
        atlas_path = atlas_template.format(season=season)
        if not os.path.exists(atlas_path):
            print(f"⚠️ Пропущен {atlas_path} — файл не найден")
            continue

        img = Image.open(atlas_path).convert("RGBA")  # сохраняем альфа-канал
        for name, (x, y) in SHAPES.items():
            box = (x * TILE_SIZE, y * TILE_SIZE, (x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE)
            tile = img.crop(box)
            tile.save(os.path.join(season_split, f"{prefix}_{name}.png"))
    print(f"✅ Атласы нарезаны для {season}")

    # === Генерация моделей ===
    available = {f.replace(".png", "") for f in os.listdir(season_split) if f.endswith(".png")}

    for tex in available:
        tex_path = f"stardew:block/farmland/{season}/{tex}"
        model_json = {
            "parent": "block/cube_all",
            "textures": {"all": tex_path}
        }
        with open(os.path.join(season_models, f"{tex}.json"), "w") as f:
            json.dump(model_json, f, indent=2)

    print(f"✅ Модели cube_all сгенерированы ({season})")

    # === Добавляем в общий multipart ===
    # Сначала dry, потом overlay (чтобы overlay был поверх)
    for shape in SHAPES:
        multipart.append({
            "when": {"shape": shape, "season": season},
            "apply": {"model": f"stardew:block/farmland/{season}/dry_{shape}"}
        })

    for shape in SHAPES:
        overlay_name = f"wet_overlay_{shape}"
        if overlay_name in available:
            multipart.append({
                "when": {"hydrated": True, "wet_shape": shape, "season": season},
                "apply": {"model": f"stardew:block/farmland/{season}/{overlay_name}"}
            })

print("\n🧩 Формирование финального farmland.json...")
with open(BLOCKSTATE_PATH, "w") as f:
    json.dump({"multipart": multipart}, f, indent=2)

print(f"✅ Финальный farmland.json создан: {BLOCKSTATE_PATH}")
print("\n🎉 Генерация всех сезонов завершена успешно.")

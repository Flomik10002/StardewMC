import os
import json
from PIL import Image
from itertools import product

TILE_SIZE = 16

# === Настройки ===
ATLAS_FILES = {
    "dry": "farmland_dry_full.png",
    "wet_overlay": "farmland_wet_overlay_full.png"
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

# === Список доступных текстур ===
available = set(f.replace(".png", "") for f in os.listdir(SPLIT_FOLDER))

# === Генерация cube_all моделей для dry/overlay ===
for tex in available:
    path = f"stardew:block/farmland/{tex}"
    model = {
        "parent": "block/cube_all",
        "textures": {"all": path}
    }
    with open(os.path.join(MODELS_FOLDER, f"{tex}.json"), "w") as f:
        json.dump(model, f, indent=2)

print("✅ cube_all модели сгенерированы.")

# === Генерация layered моделей и blockstates ===
variants = {}

for dry_shape in SHAPES:
    dry_name = f"dry_{dry_shape}"

    # Вариант: hydrated=false → dry only
    variants[f"hydrated=false,shape={dry_shape},wet_shape=single"] = {
        "model": f"stardew:block/farmland/{dry_name}"
    }

    for wet_shape in SHAPES:
        wet_overlay_name = f"wet_overlay_{wet_shape}"
        if dry_name in available and wet_overlay_name in available:
            model_name = f"layered_{dry_shape}_{wet_shape}"
            model = {
                "parent": "block/block",
                "textures": {
                    "layer0": f"stardew:block/farmland/{dry_name}",
                    "layer1": f"stardew:block/farmland/{wet_overlay_name}"
                }
            }
            with open(os.path.join(MODELS_FOLDER, f"{model_name}.json"), "w") as f:
                json.dump(model, f, indent=2)

            key = f"hydrated=true,shape={dry_shape},wet_shape={wet_shape}"
            variants[key] = {"model": f"stardew:block/farmland/{model_name}"}

# === Сохраняем blockstate ===
with open(os.path.join(BLOCKSTATES_FOLDER, "farmland.json"), "w") as f:
    json.dump({"variants": variants}, f, indent=2)

print("✅ farmland.json сгенерирован.")

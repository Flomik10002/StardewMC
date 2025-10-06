import os
import json

# === Настройки ===
TILE_SIZE = 16  # размер тайла в px
TEX_DRY = "stardew:block/farmland_dry_full"
TEX_WET = "stardew:block/farmland_wet_full"
OUTPUT_DIR = "generated_models"

# === Карта форм и их координат на атласе ===
# Формат: "shape_name": (x, y)
SHAPES = {
    "single":        (0, 0),
    "horizontal_left":   (1, 3),
    "horizontal_mid":    (2, 3),
    "horizontal_right":  (3, 3),
    "vertical_top":      (0, 1),
    "vertical_mid":      (0, 2),
    "vertical_bottom":   (0, 3),
    "top_left":      (1, 0),
    "top":           (2, 0),
    "top_right":     (3, 0),
    "left":          (1, 1),
    "center":        (2, 1),
    "right":         (3, 1),
    "bottom_left":   (1, 2),
    "bottom":        (2, 2),
    "bottom_right":  (3, 2)
}

os.makedirs(f"{OUTPUT_DIR}/block/farmland", exist_ok=True)

def make_model(texture, uv_coords):
    x, y = uv_coords
    uv = [x * TILE_SIZE, y * TILE_SIZE, (x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE]

    return {
        "parent": "block/block",
        "textures": {
            "layer0": texture
        },
        "elements": [
            {
                "from": [0, 0, 0],
                "to": [16, 2, 16],
                "faces": {
                    "up": {
                        "uv": uv,
                        "texture": "#layer0"
                    },
                    "down": { "texture": "#layer0" },
                    "north": { "texture": "#layer0" },
                    "south": { "texture": "#layer0" },
                    "west":  { "texture": "#layer0" },
                    "east":  { "texture": "#layer0" }
                }
            }
        ]
    }

for hydrated, texture in [("dry", TEX_DRY), ("wet", TEX_WET)]:
    for shape, uv_coords in SHAPES.items():
        filename = f"{hydrated}_{shape}.json"
        model = make_model(texture, uv_coords)
        with open(f"{OUTPUT_DIR}/block/farmland/{filename}", "w") as f:
            json.dump(model, f, indent=2)

print("✅ Модели сгенерированы в", OUTPUT_DIR)

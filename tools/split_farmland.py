from PIL import Image
import os

TILE_SIZE = 16
INPUT_PATH = "farmland_wet_full.png"  # или farmland_dry_full.png
PREFIX = "wet"  # или "dry"
OUTPUT_FOLDER = "split_farmland"

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

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

img = Image.open(INPUT_PATH)

for name, (x, y) in SHAPES.items():
    box = (x * TILE_SIZE, y * TILE_SIZE, (x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE)
    tile = img.crop(box)
    out_name = f"{PREFIX}_{name}.png"
    tile.save(os.path.join(OUTPUT_FOLDER, out_name))

print("✅ Все плитки сохранены в", OUTPUT_FOLDER)

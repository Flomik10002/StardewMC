from PIL import Image
import os
import re

INPUT_DIR = "big_stone_chest/colored_without"
OVERLAY = "big_stone_chest/big_stone_chest_overlay.png"
OUT_DIR = "big_stone_chest/colored_final"

pattern = re.compile(r"color_(\d+)\.png")

def apply_overlay():
    if not os.path.exists(OUT_DIR):
        os.makedirs(OUT_DIR)

    overlay = Image.open(OVERLAY).convert("RGBA")

    indexed_files = []
    for f in os.listdir(INPUT_DIR):
        match = pattern.match(f)
        if match:
            idx = int(match.group(1))
            indexed_files.append((idx, f))

    # Сортировка по индексу: 1,2,3,4...
    indexed_files.sort(key=lambda x: x[0])

    for idx, file in indexed_files:
        base_path = os.path.join(INPUT_DIR, file)
        base = Image.open(base_path).convert("RGBA")

        if overlay.size != base.size:
            ov = overlay.resize(base.size, Image.LANCZOS)
        else:
            ov = overlay

        # альфа-композит
        result = Image.alpha_composite(base, ov)

        # chest_i
        new_name = f"big_stone_chest_{idx}.png"
        out_path = os.path.join(OUT_DIR, new_name)

        result.save(out_path)
        print(f"✔ {file} → {new_name}")

if __name__ == "__main__":
    apply_overlay()

import os
from PIL import Image

BASE_PATH = "/home/flomik/IdeaProjects/StardewMC/src/main/resources/assets/stardew/textures/block/dirt"

for root, _, files in os.walk(BASE_PATH):
    for file in files:
        if not file.lower().endswith(".png"):
            continue

        path = os.path.join(root, file)

        with Image.open(path) as img:
            img.transpose(Image.FLIP_TOP_BOTTOM).save(path)

        print(f"✔ flipped Y: {path}")

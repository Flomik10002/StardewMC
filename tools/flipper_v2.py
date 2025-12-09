import os
from PIL import Image

PATH = "/home/flomik/IdeaProjects/StardewMC/src/main/resources/assets/stardew/textures/block/grass/winter"

for file in os.listdir(PATH):
    if not file.lower().endswith(".png"):
        continue

    path = os.path.join(PATH, file)

    with Image.open(path) as img:
        img.rotate(180).save(path)

    print(f"✔ rotated 180°: {file}")

print("✅ all images rotated 180°")

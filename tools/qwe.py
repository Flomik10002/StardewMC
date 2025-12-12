from PIL import Image, ImageEnhance
import numpy as np

img = Image.open("/home/flomik/IdeaProjects/StardewMC/tools/chests/big_stone_chest/big_stone_chest_recolored.png").convert("RGB")
arr = np.array(img) / 255.0

# Осветление через гамму
arr = arr ** 0.75

img = Image.fromarray((arr * 255).astype("uint8"))

# Лёгкий контраст
contrast = ImageEnhance.Contrast(img)
img = contrast.enhance(1.1)

img.save("output.png")

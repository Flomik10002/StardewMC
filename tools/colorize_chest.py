from PIL import Image
import os
import math

# ======== НАСТРОЙКИ ========
INPUT_IMAGE = "chest_1.png"   # твоя текстура
OUT_DIR = "output_colors"
ALPHA = 0.9              # прозрачность для корпуса
ALPHA_LID = ALPHA ** 2      # прозрачность для крышки (Stardew так делает)

PALETTE = {
    1:  (85, 85, 255),
    2:  (119, 191, 255),
    3:  (0, 170, 170),
    4:  (0, 234, 175),
    5:  (0, 170, 0),
    6:  (159, 236, 0),
    7:  (255, 234, 18),
    8:  (255, 167, 18),
    9:  (255, 105, 18),
    10: (255, 0, 0),
    11: (135, 0, 35),
    12: (255, 173, 199),
    13: (255, 117, 195),
    14: (172, 0, 198),
    15: (143, 0, 255),
    16: (89, 11, 142),
    17: (64, 64, 64),
    18: (100, 100, 100),
    19: (200, 200, 200),
    20: (254, 254, 254)
}


# ===============================================

def normalize(c):
    return (c[0]/255, c[1]/255, c[2]/255)

def multiply_color(px, col_norm, alpha):
    r = int(px[0] * col_norm[0] * alpha)
    g = int(px[1] * col_norm[1] * alpha)
    b = int(px[2] * col_norm[2] * alpha)
    return (r, g, b, 255)

def process_image():
    if not os.path.exists(OUT_DIR):
        os.makedirs(OUT_DIR)

    img = Image.open(INPUT_IMAGE).convert("RGBA")
    w, h = img.size
    pixels = img.load()

    for idx, col in PALETTE.items():
        col_norm = normalize(col)

        out = Image.new("RGBA", (w, h))
        out_px = out.load()

        for y in range(h):
            for x in range(w):
                base = pixels[x, y]

                # Применяем multiply
                out_px[x, y] = multiply_color(base, col_norm, ALPHA)

        out.save(f"{OUT_DIR}/color_{idx}.png")
        print(f"Saved: color_{idx}.png")

if __name__ == "__main__":
    process_image()

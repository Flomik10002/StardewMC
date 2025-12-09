from PIL import Image
import numpy as np

SOURCE = "source.png"   # что перекрашиваем
TARGET = "target.png"   # откуда берём оттенки
OUTPUT = "recolored.png"


def luminance(c):
    # стандарт яркости
    return 0.2126*c[0] + 0.7152*c[1] + 0.0722*c[2]


def extract_palette(img):
    img = img.convert("RGBA")
    arr = np.array(img)
    h, w, _ = arr.shape

    # получаем все уникальные цвета
    pixels = arr.reshape(-1, 4)
    pixels = pixels[pixels[:, 3] > 0]  # игнорируем alpha=0
    unique = np.unique(pixels, axis=0)

    # считаем яркость
    brightness = np.array([luminance(c[:3]) for c in unique])

    # сортируем по яркости
    order = np.argsort(brightness)

    return unique[order]


def build_lut(source_palette, target_palette):
    # источник и цель отсортированы по яркости
    lut = {}
    n_src = len(source_palette)
    n_tar = len(target_palette)

    for i, col in enumerate(source_palette):
        # ищем индекс по пропорции
        t = i / (n_src - 1)
        j = int(t * (n_tar - 1))
        lut[tuple(col)] = tuple(target_palette[j])

    return lut


def apply_lut(img_path, lut):
    img = Image.open(img_path).convert("RGBA")
    arr = np.array(img)
    h, w, _ = arr.shape

    for y in range(h):
        for x in range(w):
            px = tuple(arr[y, x])
            if px[3] == 0:
                continue
            if px in lut:
                arr[y, x] = lut[px]
            else:
                # fallback: nearest by luminance
                lum = luminance(px[:3])
                closest = min(lut.keys(), key=lambda k: abs(luminance(k[:3]) - lum))
                arr[y, x] = lut[closest]

    out = Image.fromarray(arr, "RGBA")
    out.save(OUTPUT)
    print("Saved:", OUTPUT)


# ----------------------
# RUN
# ----------------------

src_palette = extract_palette(Image.open(SOURCE))
tar_palette = extract_palette(Image.open(TARGET))

lut = build_lut(src_palette, tar_palette)

apply_lut(SOURCE, lut)

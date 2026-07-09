#!/usr/bin/env python3
"""
1:1 повтор SkinComposer.applyTintMask (Java, dev.flomik.stardew.client.character.skin)
для локальной проверки перед тем, как класть маску в assets/.

Формула (см. SkinComposer.applyTintMask/luminance/clampByte):
  gray   = round(clamp(0.2126*r + 0.7152*g + 0.0722*b))   # luminance маски в этом пикселе
  factor = gray / 128.0                                    # 128 = "цвет без изменений"
  out.rgb = round(clamp(chosenColor.rgb * factor))
  out.a  = mask.a (альфа НЕ трогается)

Не место в asset-tools/ (тот каталог трогать не положено, см. CLAUDE.md) - лежит тут.

Использование:
    python tools/tint_preview.py <mask.png> "#RRGGBB" [output.png]

Без output.png результат сохраняется рядом с исходником как <name>_preview.png.
"""
import sys
from pathlib import Path

from PIL import Image


def luminance(r: int, g: int, b: int) -> int:
    return clamp_byte(0.2126 * r + 0.7152 * g + 0.0722 * b)


def clamp_byte(value: float) -> int:
    return max(0, min(255, round(value)))


def parse_hex_color(value: str) -> tuple[int, int, int]:
    cleaned = value.strip().lstrip("#")
    if len(cleaned) != 6:
        raise ValueError("color must be #RRGGBB")
    return tuple(int(cleaned[i:i + 2], 16) for i in (0, 2, 4))


def tint(mask_path: Path, color: tuple[int, int, int], output_path: Path) -> Path:
    mask = Image.open(mask_path).convert("RGBA")
    width, height = mask.size
    src = mask.load()

    out = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    dst = out.load()
    base_r, base_g, base_b = color

    for y in range(height):
        for x in range(width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            factor = luminance(r, g, b) / 128.0
            dst[x, y] = (
                clamp_byte(base_r * factor),
                clamp_byte(base_g * factor),
                clamp_byte(base_b * factor),
                a,
            )

    out.save(output_path)
    return output_path


def main() -> None:
    if len(sys.argv) < 3:
        print('Usage: python tools/tint_preview.py <mask.png> "#RRGGBB" [output.png]')
        sys.exit(1)

    mask_path = Path(sys.argv[1])
    color = parse_hex_color(sys.argv[2])
    output_path = Path(sys.argv[3]) if len(sys.argv) > 3 else mask_path.with_name(mask_path.stem + "_preview.png")

    result = tint(mask_path, color, output_path)
    print(f"Saved: {result}")


if __name__ == "__main__":
    main()

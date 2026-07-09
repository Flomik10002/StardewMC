package dev.flomik.stardew.client.character.skin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.flomik.stardew.common.module.character.CharacterProfile;

/**
 * Плоский "бюстовый" портрет персонажа (по пояс) для иконки мира
 * ({@code icon.png}, см. {@code ClientCharacterCreationOpener}) — не 3D-рендер
 * модели, а вырезка и увеличение фронтальных ({@link MinecraftSkinLayout#FACE_FRONT})
 * граней головы/тела/рук из УЖЕ готовой composed-текстуры скина
 * ({@link SkinComposer#compose}) — то же самое изображение, что уже надето на
 * персонажа (кожа, волосы, рубашка, глаза всё уже запечены туда), только
 * вырезано и укрупнено. В духе самой Stardew Valley — там портреты тоже
 * плоский пиксель-арт, а не 3D-рендер.
 *
 * <p>Итоговый холст ЖЁСТКО {@value #ICON_SIZE}×{@value #ICON_SIZE} — это не
 * произвольный выбор, а требование ванили: {@code FaviconTexture.upload()}
 * (см. {@code WorldSelectionList}) кидает {@code IllegalArgumentException},
 * если картинка не РОВНО 64×64, после чего {@code loadIcon()} эту ошибку
 * молча ловит и показывает фолбэк-иконку "unknown_server.png" — именно так
 * выглядел баг с более крупным (132×164) холстом первой версии этого класса:
 * генератор отрабатывал без единой ошибки в логе, файл на диске менялся, но
 * экран миров всё равно показывал заглушку. Контент (голова+рука+тело+рука)
 * при {@link #BASE_SCALE}=3 центрируется внутри 64×64 с запасом по краям под
 * "вылезающий" второй слой, а не растягивается на весь холст.
 *
 * <p>"Второй слой" каждой части (LAYER2 — волосы/шляпа у головы, "куртка"
 * рубашки/рукава у тела/рук) рисуется ПОВЕРХ первого КАЖДЫМ исходным пикселем
 * НЕМНОГО крупнее ({@link #OVERLAY_SCALE} > {@link #BASE_SCALE}), но
 * центрированным на той же самой позиции, а не тем же размером с того же
 * угла — иначе на плоской картинке второй слой был бы от первого визуально
 * неотличим (та же зона текстуры того же размера), тогда как в настоящей
 * 3D-модели он геометрически чуть "надут" наружу (волосы/рукав слегка торчат
 * за силуэт головы/тела). Увеличенный центрированный блок имитирует именно
 * этот "выступающий наружу" эффект в 2D.
 */
public final class WorldIconComposer {

    private static final int ICON_SIZE = 64;
    private static final int BASE_SCALE = 3;
    private static final int OVERLAY_SCALE = 5;

    // Композиция (в ИСХОДНЫХ пикселях текстуры, до умножения на BASE_SCALE):
    // голова 8x8 сверху по центру, под ней в ряд: рука+тело+рука.
    private static final int HEAD_H = 8;
    private static final int ARM_W = 4, BODY_W = 8, TORSO_H = 12;
    private static final int CONTENT_W = ARM_W + BODY_W + ARM_W;
    private static final int CONTENT_H = HEAD_H + TORSO_H;

    private WorldIconComposer() {
    }

    public static NativeImage compose(CharacterProfile profile) {
        NativeImage skin = SkinComposer.compose(profile);
        try (skin) {
            NativeImage icon = new NativeImage(NativeImage.Format.RGBA, ICON_SIZE, ICON_SIZE, true);

            int marginX = (ICON_SIZE - CONTENT_W * BASE_SCALE) / 2;
            int marginY = (ICON_SIZE - CONTENT_H * BASE_SCALE) / 2;

            int headX = marginX + ARM_W * BASE_SCALE;
            int torsoY = marginY + HEAD_H * BASE_SCALE;
            // "Правая"/"левая" рука тут - от самого игрока (как в UV-разметке
            // скина), а не от зрителя: во фронтальной позе они видны зрителю
            // ЗЕРКАЛЬНО (правая рука игрока - слева на экране), поэтому
            // RIGHT_ARM рисуется в ЛЕВОЙ части холста.
            int rightArmX = marginX;
            int bodyX = marginX + ARM_W * BASE_SCALE;
            int leftArmX = marginX + (ARM_W + BODY_W) * BASE_SCALE;

            drawPart(icon, skin, MinecraftSkinLayout.HEAD_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.HEAD_LAYER2[MinecraftSkinLayout.FACE_FRONT], headX, marginY);
            drawPart(icon, skin, MinecraftSkinLayout.RIGHT_ARM_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.RIGHT_ARM_LAYER2[MinecraftSkinLayout.FACE_FRONT], rightArmX, torsoY);
            drawPart(icon, skin, MinecraftSkinLayout.BODY_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.BODY_LAYER2[MinecraftSkinLayout.FACE_FRONT], bodyX, torsoY);
            drawPart(icon, skin, MinecraftSkinLayout.LEFT_ARM_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.LEFT_ARM_LAYER2[MinecraftSkinLayout.FACE_FRONT], leftArmX, torsoY);

            return icon;
        }
    }

    private static void drawPart(NativeImage icon, NativeImage skin, MinecraftSkinLayout.Rect base, MinecraftSkinLayout.Rect overlay, int destX, int destY) {
        blitScaled(icon, skin, base, destX, destY, BASE_SCALE);
        blitScaled(icon, skin, overlay, destX, destY, OVERLAY_SCALE);
    }

    /**
     * Рисует {@code rect} из {@code source}, каждый исходный пиксель — блоком
     * {@code blockSize}×{@code blockSize}. Позиция блока всегда считается по
     * СЕТКЕ {@link #BASE_SCALE} (не {@code blockSize}) — иначе при
     * {@code blockSize != BASE_SCALE} (второй слой) блоки расходились бы по
     * сетке с накопительным сдвигом вместо равномерного centered-запаса
     * вокруг позиции первого слоя.
     */
    private static void blitScaled(NativeImage dest, NativeImage source, MinecraftSkinLayout.Rect rect, int destX, int destY, int blockSize) {
        int centerOffset = (blockSize - BASE_SCALE) / 2;
        for (int sy = 0; sy < rect.h(); sy++) {
            for (int sx = 0; sx < rect.w(); sx++) {
                int pixel = source.getPixelRGBA(rect.x() + sx, rect.y() + sy);
                if (nativeAlpha(pixel) == 0) continue;

                int blockX = destX + sx * BASE_SCALE - centerOffset;
                int blockY = destY + sy * BASE_SCALE - centerOffset;
                for (int oy = 0; oy < blockSize; oy++) {
                    for (int ox = 0; ox < blockSize; ox++) {
                        blendPixel(dest, blockX + ox, blockY + oy, pixel);
                    }
                }
            }
        }
    }

    private static void blendPixel(NativeImage image, int x, int y, int source) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return;
        int sourceAlpha = nativeAlpha(source);
        if (sourceAlpha == 0) return;
        if (sourceAlpha == 255) {
            image.setPixelRGBA(x, y, source);
            return;
        }

        int destination = image.getPixelRGBA(x, y);
        double sa = sourceAlpha / 255.0D;
        double da = nativeAlpha(destination) / 255.0D;
        double outA = sa + da * (1.0D - sa);
        if (outA <= 0.0D) {
            image.setPixelRGBA(x, y, 0);
            return;
        }

        int r = clampByte((nativeRed(source) * sa + nativeRed(destination) * da * (1.0D - sa)) / outA);
        int g = clampByte((nativeGreen(source) * sa + nativeGreen(destination) * da * (1.0D - sa)) / outA);
        int b = clampByte((nativeBlue(source) * sa + nativeBlue(destination) * da * (1.0D - sa)) / outA);
        int a = clampByte(outA * 255.0D);
        image.setPixelRGBA(x, y, nativeColor(r, g, b, a));
    }

    private static int clampByte(double value) {
        return Math.max(0, Math.min(255, (int) Math.round(value)));
    }

    private static int nativeRed(int c) {
        return c & 0xFF;
    }

    private static int nativeGreen(int c) {
        return (c >> 8) & 0xFF;
    }

    private static int nativeBlue(int c) {
        return (c >> 16) & 0xFF;
    }

    private static int nativeAlpha(int c) {
        return (c >> 24) & 0xFF;
    }

    private static int nativeColor(int r, int g, int b, int a) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}

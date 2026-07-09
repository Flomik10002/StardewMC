package dev.flomik.stardew.client.character.skin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.flomik.stardew.common.module.character.CharacterProfile;

/**
 * Плоский портрет персонажа ПО ПОЯС (голова+руки+торс) для карточки мира в
 * {@code StardewLoadWorldScreen} (см. чат: "давай лучше сделаем портрет по
 * пояс, чтоб не было такого лютого искажения" - у стоячей фигуры итоговый
 * масштаб выходил дробным, у бюста сжатие ровно ×4 даёт ЦЕЛЫХ 2 финальных
 * пикселя на 1 пиксель скина - без искажения). От {@link WorldIconComposer}
 * (тоже бюст) отличается тем, что тот жёстко 64×64 под требование
 * ваниль-{@code FaviconTexture}, а тут размер свой, под карточку. Та же
 * техника вырезки фронтальных ({@link MinecraftSkinLayout#FACE_FRONT}) граней
 * из уже готовой composed-текстуры скина ({@link SkinComposer#compose}), не
 * 3D-рендер.
 *
 * Принцип отрисовки - как описан для иконки мира (см. чат: "берёшь
 * стандартный скин, делаешь в нём пиксель 8х8, а второй слой делаешь 10х10.
 * ВСЕ. А после уже сжимаешь до размеров иконки. НЕ НАДО СРАЗУ В МАСШТАБЕ
 * ИКОНКИ РИСОВАТЬ"):
 * 1. композиция на БОЛЬШОМ холсте: базовый слой - каждый пиксель скина блоком
 *    {@link #SUPER_BASE}×{@link #SUPER_BASE} (8×8), второй слой (layer2,
 *    волосы/рукава) - блоком {@link #SUPER_OVERLAY}×{@link #SUPER_OVERLAY}
 *    (10×10), центрированным на той же сетке - выступает на 1 большой пиксель
 *    (= 1/8 исходного) с каждой стороны;
 * 2. ГОТОВАЯ большая картинка сжимается box-фильтром (усреднение по площади)
 *    ровно в 4 раза до итоговых {@link #WIDTH}×{@link #HEIGHT} - выступ
 *    второго слоя становится тонкой субпиксельной каймой, а не целым торчащим
 *    пикселем.
 * Карточка рисует результат 1:1, без какого-либо масштабирования при blit.
 */
public final class CardPortraitComposer {

    /** Базовый слой на большом холсте: 1 пиксель скина = блок 8×8 (см. javadoc класса). */
    private static final int SUPER_BASE = 8;
    /** Второй слой: блок 10×10, центрирован на сетке базового (см. javadoc класса). */
    private static final int SUPER_OVERLAY = 10;
    /** Запас по краям большого холста (в ИСХОДНЫХ пикселях скина) под выступ второго слоя. */
    private static final int MARGIN_SRC = 1;
    /** Во сколько раз сжимается большой холст: 8px блок -> 2 финальных пикселя, целое число. */
    private static final int DOWNSCALE = 4;

    // Композиция (в исходных пикселях текстуры скина): голова 8x8 сверху по
    // центру, под ней в ряд рука+тело+рука - по пояс, без ног.
    private static final int HEAD_H = 8;
    private static final int ARM_W = 4, BODY_W = 8, TORSO_H = 12;
    private static final int CONTENT_W = ARM_W + BODY_W + ARM_W; // 16
    private static final int CONTENT_H = HEAD_H + TORSO_H;       // 20

    private static final int SUPER_W = (CONTENT_W + MARGIN_SRC * 2) * SUPER_BASE; // 144
    private static final int SUPER_H = (CONTENT_H + MARGIN_SRC * 2) * SUPER_BASE; // 176

    public static final int WIDTH = SUPER_W / DOWNSCALE;  // 36
    public static final int HEIGHT = SUPER_H / DOWNSCALE; // 44

    private CardPortraitComposer() {
    }

    public static NativeImage compose(CharacterProfile profile) {
        NativeImage skin = SkinComposer.compose(profile);
        try (skin; NativeImage big = new NativeImage(NativeImage.Format.RGBA, SUPER_W, SUPER_H, true)) {
            int headX = (MARGIN_SRC + ARM_W) * SUPER_BASE;
            int headY = MARGIN_SRC * SUPER_BASE;
            int torsoY = headY + HEAD_H * SUPER_BASE;

            // "Правая"/"левая" рука - от самого игрока (как в UV-разметке
            // скина), фронтально они видны зрителю зеркально (см. тот же
            // приём в WorldIconComposer).
            int rightArmX = MARGIN_SRC * SUPER_BASE;
            int bodyX = headX;
            int leftArmX = (MARGIN_SRC + ARM_W + BODY_W) * SUPER_BASE;

            drawPart(big, skin, MinecraftSkinLayout.HEAD_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.HEAD_LAYER2[MinecraftSkinLayout.FACE_FRONT], headX, headY);
            drawPart(big, skin, MinecraftSkinLayout.RIGHT_ARM_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.RIGHT_ARM_LAYER2[MinecraftSkinLayout.FACE_FRONT], rightArmX, torsoY);
            drawPart(big, skin, MinecraftSkinLayout.BODY_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.BODY_LAYER2[MinecraftSkinLayout.FACE_FRONT], bodyX, torsoY);
            drawPart(big, skin, MinecraftSkinLayout.LEFT_ARM_LAYER1[MinecraftSkinLayout.FACE_FRONT],
                    MinecraftSkinLayout.LEFT_ARM_LAYER2[MinecraftSkinLayout.FACE_FRONT], leftArmX, torsoY);

            return downscale(big, WIDTH, HEIGHT);
        }
    }

    private static void drawPart(NativeImage big, NativeImage skin, MinecraftSkinLayout.Rect base, MinecraftSkinLayout.Rect overlay, int destX, int destY) {
        blitScaled(big, skin, base, destX, destY, SUPER_BASE);
        blitScaled(big, skin, overlay, destX, destY, SUPER_OVERLAY);
    }

    /**
     * Рисует {@code rect} из {@code source}, каждый исходный пиксель - блоком
     * {@code blockSize}×{@code blockSize}. Позиция блока всегда считается по
     * СЕТКЕ {@link #SUPER_BASE} (не {@code blockSize}) - у второго слоя блок
     * крупнее и центрируется на позиции базового, имитируя "надутый" наружу
     * layer2 3D-модели (тот же приём, что в {@link WorldIconComposer}).
     */
    private static void blitScaled(NativeImage dest, NativeImage source, MinecraftSkinLayout.Rect rect, int destX, int destY, int blockSize) {
        int centerOffset = (blockSize - SUPER_BASE) / 2;
        for (int sy = 0; sy < rect.h(); sy++) {
            for (int sx = 0; sx < rect.w(); sx++) {
                int pixel = source.getPixelRGBA(rect.x() + sx, rect.y() + sy);
                if (nativeAlpha(pixel) == 0) continue;

                int blockX = destX + sx * SUPER_BASE - centerOffset;
                int blockY = destY + sy * SUPER_BASE - centerOffset;
                for (int oy = 0; oy < blockSize; oy++) {
                    for (int ox = 0; ox < blockSize; ox++) {
                        blendPixel(dest, blockX + ox, blockY + oy, pixel);
                    }
                }
            }
        }
    }

    /**
     * Сжатие большого холста до итоговой иконки box-фильтром: каждый итоговый
     * пиксель - средневзвешенное (по перекрытой площади) всех исходных
     * пикселей его зоны; цвет взвешивается ещё и по альфе, чтобы прозрачные
     * поля не "разбавляли" цвет краёв силуэта чёрным.
     */
    private static NativeImage downscale(NativeImage source, int targetW, int targetH) {
        NativeImage result = new NativeImage(NativeImage.Format.RGBA, targetW, targetH, true);
        double ratioX = source.getWidth() / (double) targetW;
        double ratioY = source.getHeight() / (double) targetH;
        for (int dy = 0; dy < targetH; dy++) {
            double top = dy * ratioY;
            double bottom = (dy + 1) * ratioY;
            int syEnd = Math.min((int) Math.ceil(bottom), source.getHeight());
            for (int dx = 0; dx < targetW; dx++) {
                double left = dx * ratioX;
                double right = (dx + 1) * ratioX;
                int sxEnd = Math.min((int) Math.ceil(right), source.getWidth());

                double area = 0, alphaSum = 0, redSum = 0, greenSum = 0, blueSum = 0;
                for (int sy = (int) top; sy < syEnd; sy++) {
                    double weightY = Math.min(bottom, sy + 1) - Math.max(top, sy);
                    for (int sx = (int) left; sx < sxEnd; sx++) {
                        double weight = weightY * (Math.min(right, sx + 1) - Math.max(left, sx));
                        int pixel = source.getPixelRGBA(sx, sy);
                        double alphaWeight = nativeAlpha(pixel) * weight;
                        area += weight;
                        alphaSum += alphaWeight;
                        redSum += nativeRed(pixel) * alphaWeight;
                        greenSum += nativeGreen(pixel) * alphaWeight;
                        blueSum += nativeBlue(pixel) * alphaWeight;
                    }
                }
                if (alphaSum <= 0.0D || area <= 0.0D) {
                    result.setPixelRGBA(dx, dy, 0);
                    continue;
                }
                int a = clampByte(alphaSum / area);
                int r = clampByte(redSum / alphaSum);
                int g = clampByte(greenSum / alphaSum);
                int b = clampByte(blueSum / alphaSum);
                result.setPixelRGBA(dx, dy, nativeColor(r, g, b, a));
            }
        }
        return result;
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

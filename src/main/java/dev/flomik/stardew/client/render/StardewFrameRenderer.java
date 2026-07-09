package dev.flomik.stardew.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/**
 * Общая трёхслойная рамка в стиле Stardew Valley (срезанные углы у внешнего
 * слоя, угловые акценты у среднего) — вынесена из {@code GuiGraphicsTooltipMixin}
 * (там был единственный, приватный экземпляр этой отрисовки под tooltip'ы),
 * чтобы одну и ту же рамку можно было использовать и для панелей
 * {@code CharacterCreationScreen}, и где угодно ещё, не копируя код.
 *
 * Толщина каждой из 3 полос параметризована ({@code thickness}) — у tooltip'ов
 * остаётся исходная 1px (см. перегрузки без параметра), а
 * {@code CharacterCreationScreen} использует более толстую (5px) рамку.
 *
 * Отдельно есть 4-линейный вариант с дополнительной внешней линией и своими
 * цветами тени/фона — {@link #drawPanel(GuiGraphics, int, int, int, int, int,
 * int, int, int, int, int, int)} — используется ТОЛЬКО Load-экраном (см. чат:
 * "нужно было создать отдельную для load экрана"), общие цвета/структура всех
 * остальных рамок не тронуты.
 */
public final class StardewFrameRenderer {

    public static final int BG_COLOR = 0xFFecaa67;
    public static final int BORDER_OUTER = 0xFF853605;
    public static final int BORDER_MIDDLE = 0xFFdc7b05;
    public static final int BORDER_INNER = 0xFFb14e05;
    /** Дополнительная строка-"тень" на самой внутренней границе рамки (между bgColor-полосами и заливкой). */
    public static final int SHADOW_COLOR = 0xFFd68f54;

    private static final int DEFAULT_THICKNESS = 1;

    private StardewFrameRenderer() {
    }

    /** Полная панель: 3-слойная рамка (1px, как раньше) + сплошная заливка внутри. */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        drawPanel(graphics, x, y, width, height, DEFAULT_THICKNESS, BG_COLOR, BORDER_OUTER, BORDER_MIDDLE, BORDER_INNER);
    }

    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height,
                                  int bgColor, int outer, int middle, int inner) {
        drawPanel(graphics, x, y, width, height, DEFAULT_THICKNESS, bgColor, outer, middle, inner);
    }

    /** Та же панель, но с полосами по {@code thickness} пикселей вместо 1 (см. класс javadoc). */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int thickness) {
        drawPanel(graphics, x, y, width, height, thickness, BG_COLOR, BORDER_OUTER, BORDER_MIDDLE, BORDER_INNER);
    }

    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int thickness,
                                  int bgColor, int outer, int middle, int inner) {
        int t = thickness;
        drawStardewOuterFrame(graphics, x - 5 * t, y - 5 * t, width + 10 * t, height + 10 * t, t, outer);
        drawStardewMiddleFrame(graphics, x - 4 * t, y - 4 * t, width + 8 * t, height + 8 * t, t, middle, inner);
        drawFrame(graphics, x - 3 * t, y - 3 * t, width + 6 * t, height + 6 * t, t, inner);
        // Тень - впритык к тройке outer/middle/inner (не после отступа bgColor).
        drawFrame(graphics, x - 2 * t, y - 2 * t, width + 4 * t, height + 4 * t, t, SHADOW_COLOR);
        drawFrame(graphics, x - t, y - t, width + 2 * t, height + 2 * t, t, bgColor);
        drawSolidRect(graphics, x, y, width, height, bgColor);
    }

    /**
     * ОТДЕЛЬНЫЙ вариант рамки ТОЛЬКО для Load-экрана (см. чат: "нужно было
     * создать отдельную для load экрана") - 4 линии обводки от края внутрь
     * ({@code edge}, {@code outer}, {@code middle}, {@code inner} - на одну
     * внешнюю линию больше стандартной), свой цвет тени и фона. Срезанные
     * углы - у самой внешней линии, угловые акценты - у следующей (тот же
     * стиль, что у стандартной рамки).
     */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int thickness,
                                  int bgColor, int edge, int outer, int middle, int inner, int shadow) {
        int t = thickness;
        drawStardewOuterFrame(graphics, x - 6 * t, y - 6 * t, width + 12 * t, height + 12 * t, t, edge);
        drawStardewMiddleFrame(graphics, x - 5 * t, y - 5 * t, width + 10 * t, height + 10 * t, t, outer, middle);
        drawFrame(graphics, x - 4 * t, y - 4 * t, width + 8 * t, height + 8 * t, t, middle);
        drawFrame(graphics, x - 3 * t, y - 3 * t, width + 6 * t, height + 6 * t, t, inner);
        drawFrame(graphics, x - 2 * t, y - 2 * t, width + 4 * t, height + 4 * t, t, shadow);
        drawFrame(graphics, x - t, y - t, width + 2 * t, height + 2 * t, t, bgColor);
        drawSolidRect(graphics, x, y, width, height, bgColor);
    }

    public static void drawSolidRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());

        buffer.vertex(matrix, x, y, 0).color(color).endVertex();
        buffer.vertex(matrix, x, y + height, 0).color(color).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(color).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(color).endVertex();
    }

    public static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        drawFrame(graphics, x, y, width, height, DEFAULT_THICKNESS, color);
    }

    public static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, int thickness, int color) {
        int t = thickness;
        drawSolidRect(graphics, x, y, width, t, color);
        drawSolidRect(graphics, x, y + height - t, width, t, color);
        drawSolidRect(graphics, x, y + t, t, height - 2 * t, color);
        drawSolidRect(graphics, x + width - t, y + t, t, height - 2 * t, color);
    }

    public static void drawStardewOuterFrame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        drawStardewOuterFrame(graphics, x, y, width, height, DEFAULT_THICKNESS, color);
    }

    public static void drawStardewOuterFrame(GuiGraphics graphics, int x, int y, int width, int height, int thickness, int color) {
        int t = thickness;
        drawSolidRect(graphics, x + 2 * t, y, width - 4 * t, t, color);
        drawSolidRect(graphics, x + 2 * t, y + height - t, width - 4 * t, t, color);
        drawSolidRect(graphics, x, y + 2 * t, t, height - 4 * t, color);
        drawSolidRect(graphics, x + width - t, y + 2 * t, t, height - 4 * t, color);

        drawSolidRect(graphics, x + t, y + t, t, t, color);
        drawSolidRect(graphics, x + width - 2 * t, y + t, t, t, color);
        drawSolidRect(graphics, x + t, y + height - 2 * t, t, t, color);
        drawSolidRect(graphics, x + width - 2 * t, y + height - 2 * t, t, t, color);
    }

    public static void drawStardewMiddleFrame(GuiGraphics graphics, int x, int y, int width, int height, int mainColor, int cornerColor) {
        drawStardewMiddleFrame(graphics, x, y, width, height, DEFAULT_THICKNESS, mainColor, cornerColor);
    }

    public static void drawStardewMiddleFrame(GuiGraphics graphics, int x, int y, int width, int height, int thickness, int mainColor, int cornerColor) {
        drawFrame(graphics, x, y, width, height, thickness, mainColor);

        int t = thickness;
        drawSolidRect(graphics, x, y, t, t, cornerColor);
        drawSolidRect(graphics, x + width - t, y, t, t, cornerColor);
        drawSolidRect(graphics, x, y + height - t, t, t, cornerColor);
        drawSolidRect(graphics, x + width - t, y + height - t, t, t, cornerColor);
    }
}

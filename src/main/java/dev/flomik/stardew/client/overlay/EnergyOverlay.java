package dev.flomik.stardew.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.ClientStardewData;
import dev.flomik.stardew.core.config.StardewConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Random;

public class EnergyOverlay {
    private static final ResourceLocation BAR_BG_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/energy_bar_bg.png");

    private static final ResourceLocation EXHAUSTED_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/exhausted_icon.png");
    private static final int EXHAUSTED_ICON_WIDTH = 14;
    private static final int EXHAUSTED_ICON_HEIGHT = 11;
    
    private static final Random RANDOM = new Random();

    // --- Параметры текстуры ФОНА ---
    private static final int BAR_WIDTH = 14;
    private static final int TOP_CAP_HEIGHT = 15;
    private static final int BOTTOM_CAP_HEIGHT = 4;
    private static final int MIDDLE_HEIGHT_IN_TEXTURE = 37;

    private static final int BG_U = 0;
    private static final int TOP_V = 0;
    private static final int MIDDLE_V = TOP_CAP_HEIGHT;
    private static final int BOTTOM_V = TOP_CAP_HEIGHT + MIDDLE_HEIGHT_IN_TEXTURE;

    // --- Отступы для цветного заполнения ---
    private static final int PADDING_TOP = 13;
    private static final int PADDING_BOTTOM = 2;
    private static final int PADDING_HORIZONTAL = 3;

    public static final IGuiOverlay HUD_ENERGY = (ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) return;

        float current = ClientStardewData.getCurrentEnergy();
        float max = ClientStardewData.getMaxEnergy();

        float scale = StardewConfig.CLIENT.energyBarScale.get().floatValue();
        
        // Расчет высоты бара (в экранных пикселях до применения scale)
        // Формула: y = 0.19x + 37
        // При 270 (мин): ~88px (было 67.5) -> увеличение на ~30%
        // При 588 (макс): ~148px (было 147) -> почти без изменений
        // Делим на scale, чтобы параметр конфига влиял только на "зум" текстуры, 
        // но итоговый размер на экране оставался фиксированным (или зависел от scale, если так задумано ранее?)
        
        // В предыдущем коде: pixelsPerEnergy = BASE / scale; total = max * pixelsPerEnergy;
        // Значит total = max * BASE / scale.
        // Итоговая высота на экране = total * scale = max * BASE.
        // То есть размер на экране БЫЛ ФИКСИРОВАННЫМ (не зависел от scale).
        
        // Сохраняем эту логику:
        float targetScreenHeight = (max * 0.19f + 37.0f);
        int totalBarHeight = (int)(targetScreenHeight / scale);

        // Позиция на экране
        int x = (int) (screenWidth - (BAR_WIDTH * scale) - 20);
        int y = (int) (screenHeight - 20 - (totalBarHeight * scale));
        
        // Тряска
        ClientStardewData.tick(); // Обновляем таймер тряски (лучше перенести в ClientTickEvent, но пока здесь сойдет для теста)
        if (ClientStardewData.getShakeTimer() > 0) {
            x += (RANDOM.nextInt(5) - 1); // -1, 0, 1
            y += (RANDOM.nextInt(5) - 1);
        }

        RenderSystem.enableBlend();

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);

        // A. Рисуем фон (3-slice scaling)
        renderBarBackground(graphics, 0, 0, totalBarHeight);

        // B. Рисуем заполнение цветом
        if (max > 0) {
            float fillRatio = Mth.clamp(current / max, 0.0f, 1.0f);

            if (fillRatio > 0) {
                int fillableAreaHeight = totalBarHeight - PADDING_TOP - PADDING_BOTTOM;
                if (fillableAreaHeight > 0) {
                    int fillHeight = (int)(fillableAreaHeight * fillRatio);

                    int fillX = PADDING_HORIZONTAL;
                    int fillWidth = BAR_WIDTH - (PADDING_HORIZONTAL * 2);
                    int fillY = totalBarHeight - PADDING_BOTTOM - fillHeight;

                    int color = getRedToGreenLerpColor(fillRatio);

                    graphics.fill(fillX, fillY, fillX + fillWidth, fillY + fillHeight, color);
                }
            }
        }

        // C. Иконка истощения (Exhausted)
        if (ClientStardewData.isExhausted()) {
            RenderSystem.setShaderTexture(0, EXHAUSTED_TEXTURE);
            // Рисуем ПРЯМО НАД баром
            // Координаты: x (такой же как у бара), y = -EXHAUSTED_ICON_HEIGHT (над 0, который является верхом бара после трансляции)
            // Но мы находимся в локальной системе координат, где (0,0) - это левый верхний угол бара.
            // Значит, рисуем в (0, -11).
            graphics.blit(EXHAUSTED_TEXTURE, 0, -EXHAUSTED_ICON_HEIGHT, 0, 0, EXHAUSTED_ICON_WIDTH, EXHAUSTED_ICON_HEIGHT, EXHAUSTED_ICON_WIDTH, EXHAUSTED_ICON_HEIGHT);
        }

        graphics.pose().popPose();

        RenderSystem.disableBlend();
    };

    /**
     * Рисует фон бара с динамической высотой.
     */
    private static void renderBarBackground(GuiGraphics graphics, int x, int y, int height) {
        RenderSystem.setShaderTexture(0, BAR_BG_TEXTURE);
        int totalCapHeight = TOP_CAP_HEIGHT + BOTTOM_CAP_HEIGHT;

        if (height < totalCapHeight) {
            graphics.blit(BAR_BG_TEXTURE, x, y, BG_U, TOP_V, BAR_WIDTH, height, BAR_WIDTH, 56);
            return;
        }

        graphics.blit(BAR_BG_TEXTURE, x, y, BG_U, TOP_V, BAR_WIDTH, TOP_CAP_HEIGHT, BAR_WIDTH, 56);

        int middleRenderHeight = height - totalCapHeight;
        if (middleRenderHeight > 0) {
            graphics.blit(BAR_BG_TEXTURE, x, y + TOP_CAP_HEIGHT,
                    BAR_WIDTH, middleRenderHeight,
                    BG_U, MIDDLE_V, BAR_WIDTH, MIDDLE_HEIGHT_IN_TEXTURE,
                    BAR_WIDTH, 56);
        }

        graphics.blit(BAR_BG_TEXTURE, x, y + height - BOTTOM_CAP_HEIGHT, BG_U, BOTTOM_V, BAR_WIDTH, BOTTOM_CAP_HEIGHT, BAR_WIDTH, 56);
    }

    /**
     * Вычисляет цвет от красного к зеленому (алгоритм Stardew Valley).
     */
    private static int getRedToGreenLerpColor(float power) {
        int r, g;
        int b = 0;
        int a = 255;

        if (power <= 0.5f) {
            r = 255;
            g = (int) (power * 2.0f * 255.0f);
        } else {
            r = (int) ((1.0f - power) * 2.0f * 255.0f);
            g = 255;
        }

        r = Mth.clamp(r, 0, 255);
        g = Mth.clamp(g, 0, 255);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

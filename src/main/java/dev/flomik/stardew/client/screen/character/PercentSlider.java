package dev.flomik.stardew.client.screen.character;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.render.StardewFrameRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntConsumer;

/**
 * Один слайдер 0-100 (ТЗ §17: "H / S / V" как отдельные ползунки). Три
 * экземпляра на цвет (Hue/Saturation/Value) — их результат наружу отдаёт
 * {@link CharacterCreationScreen}, который сам сводит H/S/V в RGB и кладёт в
 * draft-профиль; сам слайдер ничего не знает про цвет, только про число
 * 0-100 (ТЗ §21 — обновление стейта немедленно, запись файла только по OK).
 *
 * Дорожка — живой градиент ({@link TrackGradient}, задаётся снаружи и
 * пересчитывается КАЖДЫЙ кадр): для Hue это радуга от красного до красного,
 * для Saturation/Value — от серого/чёрного до текущего выбранного цвета,
 * поэтому при изменении Hue дорожки Saturation/Value перекрашиваются сами
 * (ТЗ: "чтоб всё менялось в лайфтайме"). Числовое значение (0-100) рисует
 * {@link CharacterCreationScreen} справа от слайдера, не сам слайдер (группа
 * из 3 слайдеров подписана один раз слева, см. её javadoc). Ручка —
 * assets/stardew/textures/gui/character_creation/buttons/slider.png, растянута на фиксированный
 * размер (см. {@link IconButton} javadoc про UV=[0,1] трюк) вне зависимости
 * от нативного 32×32 файла, и рисуется слоем выше дорожки (явный z-bump).
 */
class PercentSlider extends AbstractSliderButton {

    @FunctionalInterface
    interface TrackGradient {
        /** @param t позиция вдоль дорожки, 0..1 (0 = левый край, 1 = правый) */
        int colorAt(float t);
    }

    private static final ResourceLocation HANDLE_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/character_creation/buttons/slider.png");
    private static final int FALLBACK_TRACK_COLOR = 0xFF5B3A1E;

    private final String label;
    private final IntConsumer onChange;
    private final TrackGradient gradient;

    PercentSlider(int x, int y, int width, int height, String label, int initialPercent, IntConsumer onChange, TrackGradient gradient) {
        super(x, y, width, height, Component.empty(), clampToUnit(initialPercent));
        this.label = label;
        this.onChange = onChange;
        this.gradient = gradient;
        updateMessage();
    }

    private static double clampToUnit(int percent) {
        return Math.max(0, Math.min(100, percent)) / 100.0;
    }

    int getPercent() {
        return (int) Math.round(value * 100.0);
    }

    @Override
    protected void updateMessage() {
        // Только для narration (accessibility) - визуально этот текст больше
        // не рисуется, см. класс javadoc.
        setMessage(Component.literal(label + ": " + getPercent()));
    }

    @Override
    protected void applyValue() {
        onChange.accept(getPercent());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int trackHeight = Math.max(2, getHeight() / 5);
        int trackY = getY() + (getHeight() - trackHeight) / 2;
        drawGradientTrack(graphics, getX(), trackY, getWidth(), trackHeight);

        // Центр ручки должен ДОХОДИТЬ до самых краёв дорожки на value=0/1
        // (не оставаться внутри неё с отступом в половину ширины ручки) -
        // поэтому центр = getX() + value*width, а не зажатый в [x, x+width-handleSize].
        // Размер ручки увеличен (была getHeight() - слишком маленькая) -
        // теперь заметно больше самого слайдера, торчит сверху/снизу дорожки.
        int handleSize = getHeight() * 2;
        int handleCenterX = getX() + (int) Math.round(value * getWidth());
        int handleX = handleCenterX - handleSize / 2;
        int handleY = getY() + (getHeight() - handleSize) / 2;

        // Ручка обязана лежать слоем ВЫШЕ полосы-дорожки - явный z-bump вместо
        // надежды на порядок отрисовки/depth-test GL-стейта.
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 10);
        // handleSize,handleSize как texWidth/texHeight - UV=[0,1] вне
        // зависимости от нативного 32×32 файла (см. IconButton javadoc).
        graphics.blit(HANDLE_TEXTURE, handleX, handleY, 0, 0, handleSize, handleSize, handleSize, handleSize);
        graphics.pose().popPose();
    }

    private void drawGradientTrack(GuiGraphics graphics, int x, int y, int width, int height) {
        if (gradient == null) {
            StardewFrameRenderer.drawSolidRect(graphics, x, y, width, height, FALLBACK_TRACK_COLOR);
            return;
        }
        for (int dx = 0; dx < width; dx++) {
            float t = width <= 1 ? 0f : (float) dx / (width - 1);
            StardewFrameRenderer.drawSolidRect(graphics, x + dx, y, 1, height, gradient.colorAt(t));
        }
    }
}

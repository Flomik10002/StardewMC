package dev.flomik.stardew.client.screen.character;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Кнопка-иконка с текстурой, растянутой на весь bounding box кнопки —
 * независимо от НАТИВНОГО разрешения самой PNG (16×16, 32×32, 64×64 и т.п. в
 * assets/stardew/textures/gui/character_creation). Все такие кнопки на экране одного
 * видимого размера вне зависимости от исходной картинки.
 *
 * Ключевой момент: {@link GuiGraphics#blit(ResourceLocation, int, int, float, float, int, int, int, int)}
 * вычисляет UV как {@code ширина_региона / textureWidth}. Если передать
 * НАСТОЯЩИЙ размер PNG-файла как {@code textureWidth}/{@code textureHeight},
 * а размер кнопки отличается — получится ОБРЕЗКА (UV < 1), а не растяжение
 * (именно эта ошибка была в {@code client.screen.shipping.TexturedButton} и в
 * первой версии этого класса). Передавая вместо этого сам РАЗМЕР КНОПКИ и в
 * region-параметры, и в texWidth/texHeight, UV всегда получается [0,1] —
 * то есть рисуется ВСЯ картинка, растянутая под bounding box, а нативное
 * разрешение файла становится не важно вообще.
 *
 * Анимация масштаба — по образцу {@code ClickableTextureComponent}/
 * {@code CharacterCustomization.performHoverAction} из decompiled-исходника
 * vanilla: при наведении {@code scale} плавно растёт к {@code 1+hoverGrowth}
 * (по {@code hoverStep} за кадр), при уходе курсора - плавно падает обратно к
 * 1. При клике scale МГНОВЕННО подрезается на {@link #PRESS_SHRINK} (не
 * анимированно) - а дальше та же самая по-кадровая логика роста САМА тянет
 * его обратно вверх, отсюда и ощущение "просело и плавно восстановилось".
 * Хитбокс (getX/getY/getWidth/getHeight) не меняется - масштабируется только
 * ОТРИСОВКА, вокруг центра кнопки.
 */
class IconButton extends Button {

    private static final float DISABLED_TINT = 0.45f;
    /** Как у стрелок/OK в vanilla (+0.1 к baseScale=4 то есть +2.5%). */
    static final float HOVER_GROWTH_SMALL = 0.025f;
    /**
     * Кнопки пола - заметно, но НЕ чрезмерно больше, чем у стрелок (было
     * 0.35 - "слишком увеличиваются при приближении", поправлено).
     */
    static final float HOVER_GROWTH_LARGE = 0.12f;
    /** Минимальная просадка при клике - для маленького hoverGrowth (стрелки/OK) этого достаточно. */
    private static final float PRESS_SHRINK_BASE = 0.125f;

    private final ResourceLocation texture;
    private final float hoverGrowth;
    private final float hoverStep;
    /**
     * Просадка при клике пропорциональна росту при наведении (с полом ("плохо
     * уменьшаются при нажатии") иначе фиксированная PRESS_SHRINK_BASE
     * выглядела мелкой на фоне их более сильного роста) - минимум
     * PRESS_SHRINK_BASE, чтобы у мелких кнопок (стрелки/OK) поведение не изменилось.
     */
    private final float pressShrink;
    private float currentScale = 1.0f;

    IconButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress) {
        this(x, y, width, height, texture, onPress, HOVER_GROWTH_SMALL);
    }

    IconButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress, float hoverGrowth) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.hoverGrowth = hoverGrowth;
        // Тот же коэффициент "шаг/рост", что у стрелок в vanilla (0.02/0.1 = 0.2).
        this.hoverStep = hoverGrowth * 0.2f;
        this.pressShrink = Math.max(PRESS_SHRINK_BASE, hoverGrowth * 1.2f);
    }

    /**
     * Ванильный клик-звук ({@code SoundEvents.UI_BUTTON_CLICK}) не нужен -
     * у каждого действия этого экрана свой звук из оригинала (см.
     * {@code CharacterCreationScreen#playSound} и места вызова), играется
     * явно в onPress-колбэке; без этой перегрузки поверх него звучал бы ещё
     * и ванильный клик.
     */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        currentScale = Math.max(1.0f - pressShrink, currentScale - pressShrink);
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.isHovered) {
            currentScale = Math.min(currentScale + hoverStep, 1.0f + hoverGrowth);
        } else {
            currentScale = Math.max(currentScale - hoverStep, 1.0f);
        }

        // Серый оттенок, пока кнопка неактивна (ТЗ: OK не должен работать И
        // должен визуально выглядеть неактивным, пока не заполнены обязательные поля).
        if (!this.active) {
            graphics.setColor(DISABLED_TINT, DISABLED_TINT, DISABLED_TINT, 1f);
        }

        // ВАЖНО: масштаб применяется через GL-трансформ (PoseStack.scale),
        // а не пересчётом целочисленного size/x/y на каждый кадр. При росте
        // всего на 2.5-12.5% размер кнопки (обычно ~25-45 реальных px) меняется
        // на доли пикселя за кадр - Math.round() в таком случае почти всегда
        // округляет к ОДНОМУ И ТОМУ ЖЕ целому числу несколько кадров подряд, а
        // затем скачком прыгает на 1px разом - именно это ощущается как
        // "дёрганая" анимация с "зафиксированным центром" (сам центр
        // действительно не двигался - двигался/скакал только край). Масштаб
        // через pose-stack считается во float до последней GPU-стадии, без
        // промежуточного округления - это и есть тот самый плавный scale,
        // которым в оригинале рисует MonoGame SpriteBatch.
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0);
        graphics.pose().scale(currentScale, currentScale, 1f);
        graphics.pose().translate(-centerX, -centerY, 0);
        graphics.blit(texture, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        graphics.pose().popPose();

        if (!this.active) {
            graphics.setColor(1f, 1f, 1f, 1f);
        }
    }
}

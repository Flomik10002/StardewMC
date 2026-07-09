package dev.flomik.stardew.client.screen.title;

import dev.flomik.stardew.common.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Маленькие квадратные иконки-кнопки внизу справа (message/question, см.
 * {@code textures/gui/title/message.png}/{@code question.png}) — два
 * состояния БОК О БОК (в отличие от {@link TitleBackButton}, у которого они
 * стопкой), поэтому нативные размеры и раскладка листа задаются параметрами
 * конструктора, а не зашиты константой, как у более специфичных кнопок.
 */
class TitleIconButton extends Button {

    /** Крупнее, чем у остальных кнопок (1.05) - см. чат: "шестерёнке и вопросу ховер 1.1 делай". */
    private static final float HOVER_SCALE = 1.1f;

    private final ResourceLocation texture;
    private final int nativeW, nativeH;
    private boolean wasHovered = false;

    TitleIconButton(int x, int y, int width, int height, ResourceLocation texture, int nativeW, int nativeH, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.nativeW = nativeW;
        this.nativeH = nativeH;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        wasHovered = HoverSound.update(this.isHovered, wasHovered);
        int u = this.isHovered ? nativeW : 0;
        HoverScale.Rect rect = HoverScale.scaled(getX(), getY(), getWidth(), getHeight(), this.isHovered ? HOVER_SCALE : 1.0f);
        graphics.blit(texture, rect.x(), rect.y(), rect.width(), rect.height(), u, 0, nativeW, nativeH, nativeW * 2, nativeH);
    }

    /**
     * Клик "?" (открытие {@code StardewAboutScreen}, аналог ванильного
     * AboutMenu) - {@code TitleMenu.receiveLeftClick}: {@code Game1.playSound("newArtifact")}
     * (см. чат: "проверь все звуки по исходникам stardew" - раньше тут был
     * {@code CC_COIN}). "Настройки" переиспользует тот же cue - у неё нет
     * прямого аналога в оригинале (наша собственная кнопка).
     */
    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(ModSounds.MENU_ABOUT.get(), 1.0F));
    }
}

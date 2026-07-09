package dev.flomik.stardew.client.screen.title;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * "Back" — {@code textures/gui/title/back.png}, 66×54: два состояния СТОПКОЙ
 * по вертикали (normal сверху 0-27, hover снизу 27-54), не рядом, как у
 * {@link TitleMainButton}. Публичный - переиспользуется и на
 * {@code CharacterCreationScreen} (тот же "Back", что на Title/Load, см. чат).
 *
 * {@link #bottomRight} — единая точка, которая гарантирует ОДИНАКОВЫЙ размер
 * и позицию на ЛЮБОМ экране (см. чат: "их размер и расположение... ДОЛЖНЫ
 * БЫТЬ ОДИНАКОВЫ") - раньше каждый экран сам считал свой прямоугольник
 * (Load - от своей панели, character creation - через virtual-canvas
 * {@code layout.real()}), поэтому итоговые реальные пиксели расходились.
 * Оба экрана форсируют {@code guiScale=2} (см. {@code TitleScreenReplacer}/
 * {@code ClientCharacterCreationOpener}), так что {@code screenWidth/Height}
 * (логические, уже без guiScale) означают одно и то же в обоих местах -
 * привязка к ним напрямую, в обход virtual-canvas, даёт побитово идентичный
 * результат.
 */
public class TitleBackButton extends Button {

    private static final ResourceLocation TEX = new ResourceLocation(StardewMod.MODID, "textures/gui/title/back.png");
    private static final int NATIVE_W = 66, NATIVE_H = 27;

    private static final int WIDTH = 110;
    private static final int HEIGHT = WIDTH * NATIVE_H / NATIVE_W;
    private static final int MARGIN = 20;
    /** Тот же ховер-рост, что у {@link TitleMainButton} (см. чат: "кнопка назад не изменяется при наведении, хотя я попросил это"). */
    private static final float HOVER_SCALE = 1.05f;

    public static TitleBackButton bottomRight(int screenWidth, int screenHeight, OnPress onPress) {
        return new TitleBackButton(screenWidth - WIDTH - MARGIN, screenHeight - HEIGHT - MARGIN, WIDTH, HEIGHT, onPress);
    }

    private boolean wasHovered = false;

    public TitleBackButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        wasHovered = HoverSound.update(this.isHovered, wasHovered);
        int v = this.isHovered ? NATIVE_H : 0;
        HoverScale.Rect rect = HoverScale.scaled(getX(), getY(), getWidth(), getHeight(), this.isHovered ? HOVER_SCALE : 1.0f);
        graphics.blit(TEX, rect.x(), rect.y(), rect.width(), rect.height(), 0, v, NATIVE_W, NATIVE_H, NATIVE_W, NATIVE_H * 2);
    }

    /**
     * Свой звук клика вместо ванильного (см. чат: "и с кнопкой назад на всех
     * скринах") - "Назад" всегда закрывает текущий сабменю/экран, ровно как
     * закрытие сабменю в {@code TitleMenu.receiveLeftClick}:
     * {@code Game1.playSound("bigDeSelect")} (см. чат: "проверь все звуки по
     * исходникам stardew" - раньше тут был {@code CC_COIN}).
     */
    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(ModSounds.MENU_DESELECT.get(), 1.0F));
    }
}

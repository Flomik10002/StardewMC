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
 * New/Load/Co-op/Exit — один общий лист на все 4 (см.
 * {@code textures/gui/title/buttons.png}/{@code buttons_active.png}, каждый
 * 74×58, встык, без зазора между кадрами) — {@link #sliceIndex} выбирает
 * кадр, {@link #ACTIVE} подменяет весь кадр целиком на hover-вариант (не
 * тинт/масштаб, как {@code IconButton} у character creation — тут художник
 * нарисовал ДВА отдельных полных состояния кнопки).
 *
 * {@link #HOVER_SCALE} — при наведении кнопка ещё и физически подрастает
 * (см. чат: "как в оригинале" — в реальной Stardew Valley кнопки титульного
 * экрана увеличиваются под курсором), растёт СИММЕТРИЧНО от своего центра
 * (компенсируем сдвигом x/y на половину прироста), а не от угла — иначе
 * выглядело бы как смещение кнопки, а не рост.
 */
class TitleMainButton extends Button {

    private static final ResourceLocation NORMAL = tex("buttons.png");
    private static final ResourceLocation ACTIVE = tex("buttons_active.png");
    private static final int SLICE_W = 74, SLICE_H = 58;
    private static final int SHEET_W = SLICE_W * 4;
    private static final float DISABLED_TINT = 0.5f;
    /** Было 1.1 - "слишком увеличиваешь ховер" (см. чат), уменьшено до 1.05. */
    private static final float HOVER_SCALE = 1.05f;

    private final int sliceIndex;
    private boolean wasHovered = false;

    TitleMainButton(int x, int y, int width, int height, int sliceIndex, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.sliceIndex = sliceIndex;
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(StardewMod.MODID, "textures/gui/title/" + name);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.active && this.isHovered;
        wasHovered = HoverSound.update(hovered, wasHovered);
        ResourceLocation texture = hovered ? ACTIVE : NORMAL;
        HoverScale.Rect rect = HoverScale.scaled(getX(), getY(), getWidth(), getHeight(), hovered ? HOVER_SCALE : 1.0f);

        if (!this.active) {
            graphics.setColor(DISABLED_TINT, DISABLED_TINT, DISABLED_TINT, 1f);
        }
        graphics.blit(texture, rect.x(), rect.y(), rect.width(), rect.height(),
                sliceIndex * SLICE_W, 0, SLICE_W, SLICE_H, SHEET_W, SLICE_H);
        if (!this.active) {
            graphics.setColor(1f, 1f, 1f, 1f);
        }
    }

    /**
     * Свой звук клика вместо ванильного (см. чат: "добавь также звуки
     * клика"), сверено по {@code TitleMenu.performButtonAction} (см. чат:
     * "проверь все звуки по исходникам stardew" - раньше тут ВЕЗДЕ был
     * {@code CC_COIN}, что неверно): New/Co-op/Load - "select", Exit
     * (sliceIndex 3) - "bigDeSelect".
     */
    @Override
    public void playDownSound(SoundManager soundManager) {
        boolean exit = sliceIndex == 3;
        soundManager.play(SimpleSoundInstance.forUI((exit ? ModSounds.MENU_DESELECT : ModSounds.MENU_SELECT).get(), 1.0F));
    }
}

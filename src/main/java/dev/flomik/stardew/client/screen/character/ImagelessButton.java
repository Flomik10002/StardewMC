package dev.flomik.stardew.client.screen.character;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * {@link Button} без ванильной 9-slice текстуры фона — только клик/hover/
 * tooltip-логика базового класса, рендер полностью нулевой. Нужен, когда
 * визуал рисуется СНАРУЖИ полностью отдельно (farm type карточки: картинка
 * рисуется в {@code CharacterCreationScreen#renderFarmCardSelection}) - сама
 * кнопка там нужна только как кликабельная область, а не как визуальный
 * элемент (попросили: "там есть картинка, кнопка [текстура] там не нужна").
 */
class ImagelessButton extends Button {

    ImagelessButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Намеренно пусто - весь визуал рисуется снаружи.
    }

    /** См. {@link IconButton#playDownSound} - фермерские карточки тоже играют свой звук вручную (см. buildFarmTypeCards). */
    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
    }
}

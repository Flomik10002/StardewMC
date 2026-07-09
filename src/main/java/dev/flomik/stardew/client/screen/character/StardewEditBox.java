package dev.flomik.stardew.client.screen.character;

import dev.flomik.stardew.client.render.StardewFrameRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * {@link EditBox} с тем же обрамлением/цветом поля, что и у tooltip'ов
 * ({@link StardewFrameRenderer}), вместо ванильной чёрной заливки +
 * серой обводки.
 *
 * {@code setBordered(false)} отключает ванильный фон EditBox'а полностью, но
 * заодно убирает его же padding (текст без bordered рисуется вплотную к
 * {@code getX()/getY()}, без отступа в 4px и вертикального центрирования) —
 * приватные поля вроде {@code cursorPos}/{@code displayPos}, которые нужны
 * для честного повтора этого padding, недоступны без Mixin-аксессора.
 * Вместо этого сдвигаем через {@code pose().translate(...)} ПЕРЕД вызовом
 * {@code super.renderWidget} - vanilla-логика курсора/текста/выделения
 * рендерится как обычно, просто со сдвинутым началом координат. Единственный
 * известный побочный эффект: {@code onClick()} всё ещё меряет позицию клика
 * от {@code getX()} без этого сдвига, так что клик по конкретному символу
 * может промахнуться на те же несколько пикселей - для фокуса/набора текста
 * не критично.
 */
public class StardewEditBox extends EditBox {

    private static final int TEXT_COLOR = 0xFF221122;
    private static final int PAD_X = 4;

    StardewEditBox(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, narration);
        setBordered(false);
        setTextColor(TEXT_COLOR);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StardewFrameRenderer.drawPanel(graphics, getX(), getY(), getWidth(), getHeight());

        graphics.pose().pushPose();
        graphics.pose().translate(PAD_X, (getHeight() - 8) / 2f, 0);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
    }
}

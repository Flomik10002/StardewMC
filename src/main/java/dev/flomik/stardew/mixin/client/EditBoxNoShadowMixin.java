package dev.flomik.stardew.mixin.client;

import dev.flomik.stardew.client.screen.character.StardewEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@link EditBox#renderWidget} рисует свой текст/подсказку/курсор через
 * 5-арг {@code GuiGraphics.drawString(...)} перегрузки, которые ВСЕГДА рисуют
 * тень (жёстко зашитый {@code true}, см. исходник GuiGraphics) — приватные
 * поля метода (cursorPos/displayPos/formatter и т.п.) недоступны для честного
 * переопределения рендера в {@link StardewEditBox}, поэтому тень убирается
 * точечным {@code @Redirect} внутри {@code EditBox.renderWidget}.
 *
 * Мixin технически висит на БАЗОВОМ {@code EditBox} (subclass не помогает -
 * {@code renderWidget} выполняется как код родителя через {@code super.renderWidget()},
 * не переопределяется), поэтому каждый redirect проверяет
 * {@code instanceof StardewEditBox} и отдаёт исходное поведение (тень) для
 * ЛЮБОГО другого EditBox в игре (чат, наковальня, поиск в JEI и т.п.) -
 * затрагивать их не нужно и не должно.
 */
@Mixin(EditBox.class)
public abstract class EditBoxNoShadowMixin {

    @Redirect(
            method = "renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I")
    )
    private int stardew_drawTextNoShadow(GuiGraphics graphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        return graphics.drawString(font, text, x, y, color, !((Object) this instanceof StardewEditBox));
    }

    @Redirect(
            method = "renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I")
    )
    private int stardew_drawStringNoShadow(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        return graphics.drawString(font, text, x, y, color, !((Object) this instanceof StardewEditBox));
    }

    @Redirect(
            method = "renderWidget(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I")
    )
    private int stardew_drawComponentNoShadow(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        return graphics.drawString(font, text, x, y, color, !((Object) this instanceof StardewEditBox));
    }
}

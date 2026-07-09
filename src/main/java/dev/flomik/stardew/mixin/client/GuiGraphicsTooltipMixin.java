package dev.flomik.stardew.mixin.client;

import dev.flomik.stardew.client.render.StardewFontRenderer;
import dev.flomik.stardew.client.render.StardewFrameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin для кастомного рендеринга tooltip в стиле Stardew Valley.
 * Полностью перехватывает рендеринг tooltip и рисует с кастомными цветами.
 * 
 * Подход взят из Obscure Tooltips mod.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsTooltipMixin {

    @Shadow
    public abstract MultiBufferSource.BufferSource bufferSource();

    /**
     * Перехватывает рендеринг tooltip и заменяет на кастомный.
     */
    @Inject(method = "renderTooltipInternal", at = @At("HEAD"), cancellable = true)
    private void renderStardewTooltip(
        Font font,
        List<ClientTooltipComponent> components,
        int mouseX,
        int mouseY,
        ClientTooltipPositioner positioner,
        CallbackInfo ci
    ) {
        if (components.isEmpty()) return;

        GuiGraphics self = (GuiGraphics) (Object) this;
        
        // ПРИНУДИТЕЛЬНО используем кастомный шрифт для tooltip
        Font customFont = Minecraft.getInstance().font;
        if (!(customFont instanceof StardewFontRenderer)) {
            customFont = new StardewFontRenderer(customFont);
        }
        
        // Включаем принудительный режим кастомного рендеринга
        StardewFontRenderer.setForceCustomRendering(true);
        
        // Вычисляем размеры tooltip с кастомным шрифтом
        int tooltipWidth = 0;
        int tooltipHeight = components.size() == 1 ? -2 : 0;
        
        for (ClientTooltipComponent component : components) {
            int componentWidth = component.getWidth(customFont);
            if (componentWidth > tooltipWidth) {
                tooltipWidth = componentWidth;
            }
            tooltipHeight += component.getHeight();
        }

        // Позиционируем tooltip
        var pos = positioner.positionTooltip(
            self.guiWidth(),
            self.guiHeight(),
            mouseX,
            mouseY,
            tooltipWidth,
            tooltipHeight
        );
        
        int x = pos.x();
        int y = pos.y();

        // Рендерим кастомный tooltip
        self.pose().pushPose();
        self.pose().translate(0.0F, 0.0F, 400.0F);

        // Общая 3-слойная Stardew-рамка (см. StardewFrameRenderer) - используется
        // и здесь, и в CharacterCreationScreen, одна реализация на оба места.
        StardewFrameRenderer.drawPanel(self, x, y, tooltipWidth, tooltipHeight);

        // Рендерим компоненты tooltip с кастомным шрифтом
        self.pose().translate(0.0F, 0.0F, 400.0F);
        int yPos = y;
        for (ClientTooltipComponent component : components) {
            component.renderText(customFont, x, yPos, self.pose().last().pose(), self.bufferSource());
            component.renderImage(customFont, x, yPos, self);
            yPos += component.getHeight();
        }

        self.pose().popPose();
        
        StardewFontRenderer.setForceCustomRendering(false);
        
        ci.cancel(); // Отменяем стандартный рендеринг
    }
}


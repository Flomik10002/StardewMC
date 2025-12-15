package dev.flomik.stardew.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.render.StardewFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
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

    // Stardew Valley tooltip colors (ARGB format)
    // Фон: светлый бежевый без прозрачности
    private static final int BG_COLOR = 0xFFfdbc6e;
    
    // Три обводки Stardew Valley (впритык друг к другу)
    private static final int BORDER_OUTER = 0xFF853605;   // Внешняя (позиция -5)
    private static final int BORDER_MIDDLE = 0xFFdc7b05;  // Средняя (позиция -4)
    private static final int BORDER_INNER = 0xFFb14e05;   // Внутренняя (позиция -3)

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

        // Рисуем 3 обводки впритык друг к другу НАРУЖУ от фона
        // Внешняя рамка (самая темная) - самая дальняя, со срезанными углами (Stardew Valley style)
        drawStardewOuterFrame(self, x - 5, y - 5, tooltipWidth + 10, tooltipHeight + 10, BORDER_OUTER);
        
        // Средняя рамка (светлая) - на 1 пиксель ближе, с угловыми акцентами
        drawStardewMiddleFrame(self, x - 4, y - 4, tooltipWidth + 8, tooltipHeight + 8, BORDER_MIDDLE, BORDER_INNER);
        
        // Внутренняя рамка (темная) - еще на 1 пиксель ближе, обычная
        drawFrame(self, x - 3, y - 3, tooltipWidth + 6, tooltipHeight + 6, BORDER_INNER);
        
        // Обводка фоновым цветом от -2 до 0 (переход между обводкой и фоном)
        drawFrame(self, x - 2, y - 2, tooltipWidth + 4, tooltipHeight + 4, BG_COLOR);
        drawFrame(self, x - 1, y - 1, tooltipWidth + 2, tooltipHeight + 2, BG_COLOR);
        
        // Рисуем фон (один цвет, без градиента) - в центре
        drawSolidRect(self, x, y, tooltipWidth, tooltipHeight, BG_COLOR);

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

    /**
     * Рисует прямоугольник одного цвета (без градиента).
     */
    private void drawSolidRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        
        buffer.vertex(matrix, x, y, 0).color(color).endVertex();
        buffer.vertex(matrix, x, y + height, 0).color(color).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(color).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(color).endVertex();
    }

    /**
     * Рисует рамку одного цвета (4 линии по периметру).
     */
    private void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        drawSolidRect(graphics, x, y, width, 1, color);                    // Верх
        drawSolidRect(graphics, x, y + height - 1, width, 1, color);       // Низ
        drawSolidRect(graphics, x, y + 1, 1, height - 2, color);           // Лево
        drawSolidRect(graphics, x + width - 1, y + 1, 1, height - 2, color); // Право
    }
    
    /**
     * Рисует внешнюю рамку в стиле Stardew Valley: со срезанными углами (2x2 пикселя).
     */
    private void drawStardewOuterFrame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // Верхняя линия (без углов 2px)
        drawSolidRect(graphics, x + 2, y, width - 4, 1, color);
        
        // Нижняя линия (без углов 2px)
        drawSolidRect(graphics, x + 2, y + height - 1, width - 4, 1, color);
        
        // Левая линия (без углов 2px)
        drawSolidRect(graphics, x, y + 2, 1, height - 4, color);
        
        // Правая линия (без углов 2px)
        drawSolidRect(graphics, x + width - 1, y + 2, 1, height - 4, color);
        
        // Угловые переходы (диагональные элементы 1px)
        // Верхний левый
        drawSolidRect(graphics, x + 1, y + 1, 1, 1, color);
        
        // Верхний правый
        drawSolidRect(graphics, x + width - 2, y + 1, 1, 1, color);
        
        // Нижний левый
        drawSolidRect(graphics, x + 1, y + height - 2, 1, 1, color);
        
        // Нижний правый
        drawSolidRect(graphics, x + width - 2, y + height - 2, 1, 1, color);
    }
    
    /**
     * Рисует среднюю рамку: обычная линия, но с угловыми акцентами другого цвета.
     */
    private void drawStardewMiddleFrame(GuiGraphics graphics, int x, int y, int width, int height, int mainColor, int cornerColor) {
        // Основная рамка
        drawFrame(graphics, x, y, width, height, mainColor);
        
        // Угловые акценты (1x1 пиксель в каждом углу)
        drawSolidRect(graphics, x, y, 1, 1, cornerColor);                           // Верхний левый
        drawSolidRect(graphics, x + width - 1, y, 1, 1, cornerColor);              // Верхний правый
        drawSolidRect(graphics, x, y + height - 1, 1, 1, cornerColor);             // Нижний левый
        drawSolidRect(graphics, x + width - 1, y + height - 1, 1, 1, cornerColor); // Нижний правый
    }
}


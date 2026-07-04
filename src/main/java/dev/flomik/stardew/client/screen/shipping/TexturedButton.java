package dev.flomik.stardew.client.screen.shipping;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Кнопка с текстурой (без текста).
 * Поддерживает разные текстуры для нормального состояния и наведения.
 */
public class TexturedButton extends Button {
    
    private final ResourceLocation texture;
    private final ResourceLocation hoverTexture;
    private final int texWidth;
    private final int texHeight;
    
    public TexturedButton(int x, int y, int width, int height, 
                          ResourceLocation texture, ResourceLocation hoverTexture,
                          OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.hoverTexture = hoverTexture;
        this.texWidth = width;
        this.texHeight = height;
    }
    
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation tex = isHovered ? hoverTexture : texture;
        graphics.blit(tex, getX(), getY(), 0, 0, width, height, texWidth, texHeight);
    }
}


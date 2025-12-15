package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.mixin.client.EntityRenderDispatcherAccessor;
import dev.flomik.stardew.mixin.client.MinecraftAccessor;
import dev.flomik.stardew.mixin.client.SignRendererAccessor;
import dev.flomik.stardew.client.render.StardewFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Обработчик замены Font Renderer на кастомный
 * (используя Mixin Accessors)
 */
public class StardewFontHandler {
    
    public static Font originalFont;
    private static boolean initialized = false;

    public static void setup() {
        if (initialized) {
            StardewMod.LOGGER.warn("StardewFontHandler already initialized!");
            return;
        }
        
        try {
            Minecraft mc = Minecraft.getInstance();
            MinecraftAccessor accessor = (MinecraftAccessor) mc;
            
            originalFont = accessor.getFont();
            
            StardewMod.LOGGER.info("Replacing Font Renderer: {} -> StardewFontRenderer", 
                                   originalFont.getClass().getName());
            
            StardewFontRenderer customFont = new StardewFontRenderer(originalFont);
            accessor.setFont(customFont);
            
            EntityRenderDispatcherAccessor dispatcherAccessor = (EntityRenderDispatcherAccessor) mc.getEntityRenderDispatcher();
            dispatcherAccessor.setFont(customFont);
            
            BlockEntityRenderers.register(BlockEntityType.SIGN, context -> {
                SignRenderer signRenderer = new SignRenderer(context);
                ((SignRendererAccessor) signRenderer).setFont(customFont);
                return signRenderer;
            });
            
            BlockEntityRenderers.register(BlockEntityType.HANGING_SIGN, context -> {
                SignRenderer signRenderer = new SignRenderer(context);
                ((SignRendererAccessor) signRenderer).setFont(customFont);
                return signRenderer;
            });
            
            StardewMod.LOGGER.info("Font Renderer successfully replaced! New font: {}", 
                                   accessor.getFont().getClass().getName());
            
            initialized = true;
        } catch (Exception e) {
            StardewMod.LOGGER.error("Failed to replace Font Renderer!", e);
        }
    }

    public static Font getOriginalFont() {
        return originalFont;
    }
}


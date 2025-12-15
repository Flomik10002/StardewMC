package dev.flomik.stardew.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignRenderer.class)
public interface SignRendererAccessor {
    
    @Accessor("font")
    @Mutable
    void setFont(Font font);
}


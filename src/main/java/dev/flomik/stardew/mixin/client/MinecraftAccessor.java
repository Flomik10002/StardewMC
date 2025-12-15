package dev.flomik.stardew.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor для доступа к приватным полям Minecraft
 */
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    
    @Accessor("font")
    Font getFont();
    
    @Accessor("font")
    @Mutable
    void setFont(Font font);
}


package dev.flomik.stardew.mixin.client;

import dev.flomik.stardew.client.StardewFontHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin для замены Font Renderer при инициализации Minecraft
 * (подход из Emojiful)
 */
@Mixin(Minecraft.class)
public abstract class MinecraftStardewMixin {
    
    @Inject(method = "<init>*", at = @At(value = "RETURN"))
    private void stardew_initCustomFont(CallbackInfo ci) {
        StardewFontHandler.setup();
    }
}


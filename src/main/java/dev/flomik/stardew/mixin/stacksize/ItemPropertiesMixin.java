package dev.flomik.stardew.mixin.stacksize;

import dev.flomik.stardew.common.stacksize.StackConfig;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Отслеживает максимальный зарегистрированный размер стака
 */
@Mixin(Item.Properties.class)
public class ItemPropertiesMixin {
    
    @Inject(method = "stacksTo", at = @At(value = "RETURN"))
    private void recordMaxRegisteredItemStackSize(int stackSize, CallbackInfoReturnable<Item.Properties> cir) {
        StackConfig.maxRegisteredItemStackSize = Math.max(StackConfig.maxRegisteredItemStackSize, stackSize);
    }
}


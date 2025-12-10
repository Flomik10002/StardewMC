package dev.flomik.stardew.mixin.stacksize;

import dev.flomik.stardew.common.stacksize.StackSizeHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Увеличивает лимит слотов в ItemStackHandler (Forge)
 */
@Mixin(ItemStackHandler.class)
public class ItemStackHandlerMixin {
    
    @Inject(method = "getSlotLimit", at = @At("RETURN"), cancellable = true, remap = false)
    private void increaseStackLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        StackSizeHelper.scaleSlotLimit(cir);
    }
}


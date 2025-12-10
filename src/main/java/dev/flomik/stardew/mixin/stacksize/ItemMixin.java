package dev.flomik.stardew.mixin.stacksize;

import dev.flomik.stardew.common.stacksize.StackSizeHelper;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Увеличивает максимальный размер стака для Item
 */
@Mixin(Item.class)
public class ItemMixin {
    
    /**
     * Увеличивает максимальный размер стака
     */
    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void increaseStackLimit(CallbackInfoReturnable<Integer> cir) {
        int original = cir.getReturnValue();
        // Не трогаем unstackable предметы (размер 1)
        if (original > 1) {
            cir.setReturnValue(StackSizeHelper.getNewStackSize());
        }
    }
}

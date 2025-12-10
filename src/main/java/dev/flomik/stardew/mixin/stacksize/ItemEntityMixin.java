package dev.flomik.stardew.mixin.stacksize;

import dev.flomik.stardew.common.stacksize.StackSizeHelper;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Позволяет предметам на земле сливаться в стаки больше 64
 */
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    
    /**
     * Увеличивает лимит слияния предметов на земле.
     */
    @ModifyConstant(method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
                    constant = @Constant(intValue = 64))
    private static int increaseStackLimit(int val) {
        return StackSizeHelper.getNewStackSize();
    }
}


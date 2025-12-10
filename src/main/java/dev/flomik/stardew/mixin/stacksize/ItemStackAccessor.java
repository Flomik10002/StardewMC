package dev.flomik.stardew.mixin.stacksize;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor для прямого доступа к полю count в ItemStack
 */
@Mixin(ItemStack.class)
public interface ItemStackAccessor {
    
    @Accessor("count")
    void stardew$setCount(int value);
}


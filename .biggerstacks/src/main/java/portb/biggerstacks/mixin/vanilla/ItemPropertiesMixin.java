/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.vanilla;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;

@Mixin(Item.Properties.class)
public class ItemPropertiesMixin
{
    @Inject(method = "stacksTo", at = @At(value = "RETURN"))
    private void recordMaxRegisteredItemStackSize(int stackSize, CallbackInfoReturnable<Item.Properties> cir)
    {
        StackSizeRules.maxRegisteredItemStackSize = Math.max(StackSizeRules.maxRegisteredItemStackSize, stackSize);
    }
}

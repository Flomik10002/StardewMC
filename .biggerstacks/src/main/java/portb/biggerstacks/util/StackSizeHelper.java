/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.util;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.biggerstacks.config.StackSizeRules;

public class StackSizeHelper
{
    /**
     * Scales the slot limit based on the original value
     */
    public static void scaleSlotLimit(CallbackInfoReturnable<Integer> callbackInfoReturnable)
    {
        callbackInfoReturnable.cancel();
        callbackInfoReturnable.setReturnValue(scaleSlotLimit(callbackInfoReturnable.getReturnValue()));
    }
    
    /**
     * Scales the slot limit based on the original limit
     *
     * @param original The original stack size
     * @return 64 if stack size has been lowered (to account for possible blacklisted/whitelisted items) or the scaled stack size. If the original slot has a limit of 1, 1 is returned.
     */
    public static int scaleSlotLimit(int original)
    {
        int newStackSize = StackSizeRules.getMaxStackSize();
        
        //don't scale slots that are only meant to hold a single item
        if (original == 1)
            return 1;
        else if (newStackSize < 64) //can't trust original to be the actual original value
            return 64;
        
        return Math.max(original, original * newStackSize / 64);
    }
    
    /**
     * Increases slot limit without regard for the original size
     *
     * @return The new stack size with a minimum value of 64
     */
    public static int getNewStackSize()
    {
        return Math.max(StackSizeRules.getMaxStackSize(), 64);
    }

    public static void scaleTransferRate(CallbackInfoReturnable<Integer> callbackInfoReturnable, boolean respectSingle)
    {
        callbackInfoReturnable.cancel();
        callbackInfoReturnable.setReturnValue(scaleTransferRate(callbackInfoReturnable.getReturnValue(),
                respectSingle
        ));
    }

    public static int scaleTransferRate(int originalRate, boolean respectSingle)
    {
        if (originalRate == 1 && respectSingle)
            return 1;
        
        return Math.max(1, originalRate * StackSizeRules.getMaxStackSize() / 64);
    }
    
    public static int increaseTransferRate(int originalRate)
    {
        if (originalRate == 1)
            return 1;
        
        return Math.max(1, StackSizeRules.getMaxStackSize());
    }
}

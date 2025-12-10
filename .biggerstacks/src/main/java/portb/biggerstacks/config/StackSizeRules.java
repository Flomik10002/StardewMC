/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.config;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import portb.configlib.xml.RuleSet;

public class StackSizeRules
{
    /**
     * Tracks the maximum stack size from registered item (see {@link portb.biggerstacks.mixin.vanilla.ItemPropertiesMixin#recordMaxRegisteredItemStackSize(int, CallbackInfoReturnable)} (int, CallbackInfoReturnable)})
     */
    public static int maxRegisteredItemStackSize = 64; //start at 64 because items that want to stack to 64 don't have to call Item.Property.stacksTo() to set its stack size
    
    /**
     * The global ruleset that is either received from the server or read from the config file
     */
    private static RuleSet ruleSet;
    
    public static RuleSet getRuleSet()
    {
        return ruleSet;
    }
    
    public static void setRuleSet(RuleSet ruleSet)
    {
        StackSizeRules.ruleSet = ruleSet;
    }
    
    public static int getMaxStackSize()
    {
        if (ruleSet != null)
            return Math.max(ruleSet.getMaxStacksize(), maxRegisteredItemStackSize);
        else
            return maxRegisteredItemStackSize;
    }
}

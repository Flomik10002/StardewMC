/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.mixin.vanilla.stacksize;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import portb.biggerstacks.util.StackSizeHelper;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
    /**
     * Removes the hard coded limit to disallow giving more than 64 items in creative mode.
     */
    @ModifyConstant(method = "handleSetCreativeModeSlot", constant = @Constant(intValue = 64))
    private int increaseStackLimit(int value)
    {
        return StackSizeHelper.getNewStackSize();
    }
}

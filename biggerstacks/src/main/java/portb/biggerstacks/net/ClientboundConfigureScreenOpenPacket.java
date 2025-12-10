/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.FriendlyByteBuf;

public class ClientboundConfigureScreenOpenPacket extends GenericTemplateOptionsPacket
{
    private final boolean isAlreadyUsingCustomFile;
    
    public ClientboundConfigureScreenOpenPacket(boolean hasExistingCustomFile, int normalStackLimit, int potionsStackLimit, int enchantedBooksStackLimit)
    {
        super(normalStackLimit, potionsStackLimit, enchantedBooksStackLimit);
        
        this.isAlreadyUsingCustomFile = hasExistingCustomFile;
    }
    
    public ClientboundConfigureScreenOpenPacket(FriendlyByteBuf buf)
    {
        super(buf);
        
        isAlreadyUsingCustomFile = buf.readBoolean();
    }
    
    public void encode(FriendlyByteBuf buf)
    {
        super.encode(buf);
        
        buf.writeBoolean(isAlreadyUsingCustomFile);
    }
    
    public boolean isAlreadyUsingCustomFile()
    {
        return isAlreadyUsingCustomFile;
    }
}


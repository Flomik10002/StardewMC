/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.network.FriendlyByteBuf;
import portb.biggerstacks.config.StackSizeRules;
import portb.configlib.xml.RuleSet;

/**
 * Packet sent to clients in login handshake to ensure that the client gets stack size rules before they are needed
 */
public class ClientboundRulesHandshakePacket
{
    private final RuleSet rules;
    
    /**
     * Decodes a packet from a byte buffer
     */
    public ClientboundRulesHandshakePacket(FriendlyByteBuf buf)
    {
        rules = RuleSet.fromBytes(buf.readByteArray());
    }
    
    /**
     * Construct a packet which contains the loaded ruleset
     */
    @SuppressWarnings("unused")
    public ClientboundRulesHandshakePacket()
    {
        rules = StackSizeRules.getRuleSet();
    }
    
    public void encode(FriendlyByteBuf buf)
    {
        buf.writeByteArray(rules.toBytes());
    }
    
    public RuleSet getRules()
    {
        return rules;
    }
}

/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import portb.biggerstacks.Constants;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.gui.ConfigureScreen;

import java.util.function.Supplier;

/**
 * Handles packets for rulset config
 */
public class PacketHandler
{
    public final static  ResourceLocation CHANNEL_NAME     = new ResourceLocation(Constants.MOD_ID, "rules");
    private static final String           PROTOCOL_VERSION = "1";
    public static final  SimpleChannel    INSTANCE         = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    /**
     * Registers packets
     */
    public static void register()
    {
        int index = 0;
        INSTANCE.messageBuilder(ClientboundRulesHandshakePacket.class, index++, NetworkDirection.LOGIN_TO_CLIENT)
                .encoder(ClientboundRulesHandshakePacket::encode)
                .decoder(ClientboundRulesHandshakePacket::new)
                .markAsLoginPacket()
                .noResponse()
                .consumerMainThread(PacketHandler::handleHandshake)
                .add();
        
        INSTANCE.messageBuilder(ClientboundRulesUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundRulesUpdatePacket::encode)
                .decoder(ClientboundRulesUpdatePacket::new)
                .noResponse()
                .consumerMainThread(PacketHandler::handleUpdate)
                .add();
        
        //these have to exist on the server or the mod will not be "compatible" with it according to forge
        
        INSTANCE.messageBuilder(ClientboundConfigureScreenOpenPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundConfigureScreenOpenPacket::encode)
                .decoder(ClientboundConfigureScreenOpenPacket::new)
                .consumerMainThread(PacketHandler::handleOpenScreenPacket)
                .add();
        
        INSTANCE.messageBuilder(ServerboundCreateConfigTemplatePacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundCreateConfigTemplatePacket::encode)
                .decoder(ServerboundCreateConfigTemplatePacket::new)
                .consumerNetworkThread(ServerboundCreateConfigTemplatePacket::handleCreateConfigTemplate)
                .add();
    }
    
    
    private static void handleHandshake(ClientboundRulesHandshakePacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        StackSizeRules.setRuleSet(packet.getRules());
        ctx.get().setPacketHandled(true);
    }
    
    private static void handleUpdate(ClientboundRulesUpdatePacket packet, Supplier<NetworkEvent.Context> ctx)
    {
        StackSizeRules.setRuleSet(packet.getRules());
        ctx.get().setPacketHandled(true);
    }
    
    private static boolean handleOpenScreenPacket(ClientboundConfigureScreenOpenPacket clientboundConfigureScreenOpenPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        //open the screen
        ConfigureScreen.open(clientboundConfigureScreenOpenPacket);
        return true;
    }
}

package dev.flomik.stardew.core.network;

import dev.flomik.stardew.StardewMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StardewMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, S2CSeasonSync.class, S2CSeasonSync::encode, S2CSeasonSync::decode, S2CSeasonSync::handle);
        CHANNEL.registerMessage(id++, PacketChangeChestVariant.class, PacketChangeChestVariant::encode, PacketChangeChestVariant::decode, PacketChangeChestVariant::handle);
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
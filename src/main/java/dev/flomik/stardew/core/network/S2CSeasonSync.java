package dev.flomik.stardew.core.network;

import dev.flomik.stardew.client.ClientSeasonManager;
import dev.flomik.stardew.core.time.Season;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSeasonSync {
    private final Season season;

    public S2CSeasonSync(Season season) {
        this.season = season;
    }

    public static void encode(S2CSeasonSync msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.season);
    }

    public static S2CSeasonSync decode(FriendlyByteBuf buf) {
        return new S2CSeasonSync(buf.readEnum(Season.class));
    }

    public static void handle(S2CSeasonSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                ClientHandler.handle(msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientHandler {
        public static void handle(S2CSeasonSync msg) {
            ClientSeasonManager.setSeason(msg.season);
        }
    }
}
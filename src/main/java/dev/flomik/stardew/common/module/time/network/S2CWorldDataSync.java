package dev.flomik.stardew.common.module.time.network;

import dev.flomik.stardew.client.ClientStardewData;
import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.Weather;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CWorldDataSync {
    private final Season season;
    private final Weather weather;
    private final int day;

    public S2CWorldDataSync(Season season, Weather weather, int day) {
        this.season = season;
        this.weather = weather;
        this.day = day;
    }

    public static void encode(S2CWorldDataSync msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.season);
        buf.writeEnum(msg.weather);
        buf.writeInt(msg.day);
    }

    public static S2CWorldDataSync decode(FriendlyByteBuf buf) {
        return new S2CWorldDataSync(buf.readEnum(Season.class), buf.readEnum(Weather.class), buf.readInt());
    }

    public static void handle(S2CWorldDataSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientStardewData.updateWorldData(msg.season, msg.weather, msg.day);
        });
        ctx.get().setPacketHandled(true);
    }
}
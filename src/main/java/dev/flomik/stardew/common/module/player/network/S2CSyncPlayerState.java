package dev.flomik.stardew.common.module.player.network;

import dev.flomik.stardew.client.ClientStardewData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncPlayerState {
    private final float currentEnergy;
    private final float maxEnergy;
    private final boolean isExhausted;
    private final long money;
    private final long totalEarnings;

    public S2CSyncPlayerState(float current, float max, boolean exhausted, long money, long totalEarnings) {
        this.currentEnergy = current;
        this.maxEnergy = max;
        this.isExhausted = exhausted;
        this.money = money;
        this.totalEarnings = totalEarnings;
    }

    public static void encode(S2CSyncPlayerState msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.currentEnergy);
        buf.writeFloat(msg.maxEnergy);
        buf.writeBoolean(msg.isExhausted);
        buf.writeLong(msg.money);
        buf.writeLong(msg.totalEarnings);
    }

    public static S2CSyncPlayerState decode(FriendlyByteBuf buf) {
        return new S2CSyncPlayerState(
                buf.readFloat(), 
                buf.readFloat(), 
                buf.readBoolean(),
                buf.readLong(),
                buf.readLong()
        );
    }

    public static void handle(S2CSyncPlayerState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientStardewData.update(msg.currentEnergy, msg.maxEnergy, msg.isExhausted, msg.money);
        });
        ctx.get().setPacketHandled(true);
    }
}

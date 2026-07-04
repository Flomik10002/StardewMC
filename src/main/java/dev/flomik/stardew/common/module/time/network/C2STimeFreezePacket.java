package dev.flomik.stardew.common.module.time.network;

import dev.flomik.stardew.common.module.time.TimeFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Пакет от клиента к серверу для управления заморозкой времени.
 * Отправляется когда игрок открывает/закрывает меню, которое должно останавливать время.
 */
public class C2STimeFreezePacket {

    private final boolean freeze;

    public C2STimeFreezePacket(boolean freeze) {
        this.freeze = freeze;
    }

    public C2STimeFreezePacket(FriendlyByteBuf buf) {
        this.freeze = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(freeze);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Проверяем, что это singleplayer (только там должна работать пауза)
            if (player.getServer() != null && player.getServer().isSingleplayer()) {
                if (freeze) {
                    TimeFreezeManager.freeze();
                } else {
                    TimeFreezeManager.unfreeze();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}


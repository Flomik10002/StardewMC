package dev.flomik.stardew.common.module.character.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Сервер просит клиента открыть Character Creation Screen (первый вход в
 * подготовленный мир, ТЗ §4). Пока без данных полезной нагрузки — singleplayer
 * -only v1 (см. чат ТЗ), founder/joiner различие для мультиплеера из
 * docs/world-template.md сюда ещё не перенесено.
 */
public class S2COpenCharacterCreation {

    public static void encode(S2COpenCharacterCreation msg, FriendlyByteBuf buf) {
    }

    public static S2COpenCharacterCreation decode(FriendlyByteBuf buf) {
        return new S2COpenCharacterCreation();
    }

    public static void handle(S2COpenCharacterCreation msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> dev.flomik.stardew.client.character.ClientCharacterCreationOpener::open));
        ctx.get().setPacketHandled(true);
    }
}

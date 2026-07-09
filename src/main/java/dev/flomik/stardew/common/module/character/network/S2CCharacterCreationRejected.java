package dev.flomik.stardew.common.module.character.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Серверная валидация (ТЗ §33) не прошла — экран остаётся открытым, ошибка
 * показывается внутри интерфейса (ТЗ §42 "Не использовать только chat
 * message"), {@code characterCreated} не выставляется.
 */
public class S2CCharacterCreationRejected {

    private final String reasonKey;

    public S2CCharacterCreationRejected(String reasonKey) {
        this.reasonKey = reasonKey;
    }

    public static void encode(S2CCharacterCreationRejected msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.reasonKey);
    }

    public static S2CCharacterCreationRejected decode(FriendlyByteBuf buf) {
        return new S2CCharacterCreationRejected(buf.readUtf());
    }

    public static void handle(S2CCharacterCreationRejected msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> dev.flomik.stardew.client.character.ClientCharacterCreationOpener.onRejected(msg.reasonKey)));
        ctx.get().setPacketHandled(true);
    }
}

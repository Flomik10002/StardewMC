package dev.flomik.stardew.common.module.character.event;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.character.StardewWorldMarker;
import dev.flomik.stardew.common.module.character.capability.CharacterProfileProvider;
import dev.flomik.stardew.common.module.character.network.S2CCharacterCreationAccepted;
import dev.flomik.stardew.common.module.character.network.S2COpenCharacterCreation;
import dev.flomik.stardew.core.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Решает, нужно ли открывать Character Creator (ТЗ §65 "FirstJoinController").
 * Обязательные условия (ТЗ §3, §4) — ОБА:
 * <ol>
 *   <li>это подготовленный Stardew-мир ({@link StardewWorldMarker});</li>
 *   <li>у игрока ещё нет завершённого профиля.</li>
 * </ol>
 * Без первого условия экран открывался бы в любом случайном vanilla-мире —
 * именно то, что ТЗ §3 прямо запрещает.
 *
 * Если профиль УЖЕ завершён, всё равно отправляем
 * {@link S2CCharacterCreationAccepted} — тот же пакет, что подтверждает
 * только что созданный профиль, но здесь он просто заставляет клиента
 * пересобрать и заново применить скин к локальному игроку (ТЗ §31 "skin
 * восстанавливается при загрузке мира", AC-20). Без этого скин на клиенте —
 * чисто runtime-состояние (DynamicTexture + PlayerInfo override), которое
 * ничем не гарантировано пережить новое подключение.
 */
@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class FirstJoinController {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean isStardewWorld = StardewWorldMarker.get(player.serverLevel()).isStardewWorld();
        if (!isStardewWorld) return;

        player.getCapability(CharacterProfileProvider.CHARACTER_PROFILE_CAPABILITY).ifPresent(profile -> {
            if (!profile.isCharacterCreated()) {
                PacketHandler.sendToPlayer(new S2COpenCharacterCreation(), player);
            } else {
                PacketHandler.sendToPlayer(new S2CCharacterCreationAccepted(profile), player);
            }
        });
    }
}

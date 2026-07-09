package dev.flomik.stardew.common.module.character.event;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.character.StardewWorldMarker;
import dev.flomik.stardew.common.module.character.capability.CharacterProfileProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Серверная сторона блокировки (ТЗ §4-5). Движение/открытие инвентаря
 * отдельно НЕ обрабатываются здесь: пока на клиенте открыт модальный
 * {@code CharacterCreationScreen} с {@code shouldCloseOnEsc() == false},
 * Minecraft сам не опрашивает keybind'ы движения/хотбара/инвентаря (это же
 * верно для ЛЮБОГО открытого экрана — инвентарь ванильно не листается, пока
 * открыт другой Screen). Здесь блокируется то, что клиентский Screen не может
 * перехватить сам: атака и взаимодействие с миром.
 *
 * Тот же {@code !profile.isCharacterCreated() && isStardewWorld} guard, что и
 * у {@link FirstJoinController} — блок снимается сам собой в момент, когда
 * профиль помечается COMPLETED.
 */
@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class PlayerLockController {

    private static boolean isLocked(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (!StardewWorldMarker.get(serverPlayer.serverLevel()).isStardewWorld()) return false;

        return serverPlayer.getCapability(CharacterProfileProvider.CHARACTER_PROFILE_CAPABILITY)
                .map(profile -> !profile.isCharacterCreated())
                .orElse(false);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (isLocked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent event) {
        if (isLocked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player && isLocked(player)) {
            event.setCanceled(true);
        }
    }

    // Временная неуязвимость (ТЗ §5, допустимая доп. мера) - не хочется,
    // чтобы игрок умер посреди заполнения имени фермы.
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isLocked(player)) {
            event.setCanceled(true);
        }
    }
}

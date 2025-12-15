package dev.flomik.stardew.common.module.player.event;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import dev.flomik.stardew.common.module.player.capability.PlayerStardewState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(PlayerProvider.STARDEW_CAPABILITY).isPresent()) {
                // ИСПРАВЛЕНО: Создаем новый State и передаем его в Provider
                event.addCapability(new ResourceLocation(StardewMod.MODID, "player_state"),
                        new PlayerProvider(new PlayerStardewState(player)));
            }
        }
    }

    // Сохранение данных при смерти
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(oldStore -> {
            event.getEntity().getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(newStore -> {
                newStore.copyFrom(oldStore);
                // При смерти обычно энергия восстанавливается, но в SV теряются деньги/предметы.
                // Если хочешь восстановить энергию при респавне:
                if (event.isWasDeath()) {
                    // ИСПРАВЛЕНО: restoreEnergy
                    newStore.restoreEnergy(newStore.getMaxEnergy());
                }
            });
        });
    }

    // Синхронизация при входе в мир
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(PlayerStardewState::sync);
    }
}
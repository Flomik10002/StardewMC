package dev.flomik.stardew.common.module.nature;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.admin.StardewDebug;
import dev.flomik.stardew.common.module.nature.blockentity.LargeStumpBlockEntity;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Перехватывает попытку сломать большой пень: пока ХП не дойдёт до 0,
 * отменяет разрушение блока, лишь снимая ХП за удар. Каждый результативный
 * удар тратит энергию игрока; попытка недостаточным (базовым) топором тоже
 * тратит немного энергии, хоть урона и не наносит.
 */
@Mod.EventBusSubscriber(modid = StardewMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LargeStumpEvents {

    private static final float ENERGY_PER_HIT = 2.0f;
    private static final float ENERGY_PER_FAILED_ATTEMPT = 1.0f;

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (!(be instanceof LargeStumpBlockEntity stump)) return;

        Player player = event.getPlayer();
        if (player != null && player.isCreative()) return;

        ItemStack tool = player != null ? player.getMainHandItem() : ItemStack.EMPTY;

        if (!LargeStumpBlockEntity.isAxe(tool)) {
            // Не должно происходить в обычной игре (getDestroyProgress уже
            // отсекает не-топоры), но перестрахуемся от чужих принудительных break'ов.
            event.setCanceled(true);
            return;
        }

        boolean canDamage = LargeStumpBlockEntity.canDamage(tool);
        boolean destroyed = canDamage && stump.hit(tool);

        if (player != null) {
            drainEnergy(player, canDamage ? ENERGY_PER_HIT : ENERGY_PER_FAILED_ATTEMPT);
        }

        if (!destroyed) {
            event.setCanceled(true);

            // ХП игроку не показываем — только при включённом /stardew debug.
            if (player != null && StardewDebug.isEnabled()) {
                player.displayClientMessage(
                        Component.literal("[debug] large_stump HP: " + stump.getHp() + "/" + LargeStumpBlockEntity.MAX_HP),
                        true);
            }
        }
    }

    private static void drainEnergy(Player player, float amount) {
        player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> state.consumeEnergy(amount));
    }
}

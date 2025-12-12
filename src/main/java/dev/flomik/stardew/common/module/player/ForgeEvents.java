package dev.flomik.stardew.common.module.player;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.network.PacketHandler;
import dev.flomik.stardew.common.module.time.network.S2CSeasonSync;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityChest;
import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.StardewDateData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StardewDateData dateData = StardewDateData.get(player.serverLevel());
            Season currentSeason = dateData.getSeason();

            PacketHandler.sendToPlayer(new S2CSeasonSync(currentSeason), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StardewDateData dateData = StardewDateData.get(player.serverLevel());
            PacketHandler.sendToPlayer(new S2CSeasonSync(dateData.getSeason()), player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be instanceof BlockEntityChest chest) {
            if (!chest.isEmpty()) {
                event.setCanceled(true);

                if (event.getPlayer() != null && !event.getPlayer().level().isClientSide) {
                    event.getPlayer().displayClientMessage(Component.literal("Chest is not empty!"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().level().isClientSide()) return;

        // Проверяем позицию блока из события
        event.getPosition().ifPresent(pos -> {
            BlockEntity be = event.getEntity().level().getBlockEntity(pos);
            if (be instanceof BlockEntityChest chest) {
                if (!chest.isEmpty()) {
                    // Полностью останавливаем разрушение, устанавливая скорость 0
                    // Это делает блок неуязвимым, как bedrock
                    event.setNewSpeed(0.0f);
                    event.setCanceled(true);
                }
            }
        });
    }
}

package dev.flomik.stardew.core.time;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import static dev.flomik.stardew.core.time.ScheduleManager.*;

@Mod.EventBusSubscriber
public class StardewClock {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) return;
        if (event.side != LogicalSide.SERVER) return;

        Level level = event.level;
        if (!(level instanceof ServerLevel serverLevel)) return;

        long ticks = serverLevel.getDayTime();

        tick(ticks);

        if (StardewTimeUtils.shouldPassOut(ticks)) {
            for (ServerPlayer player : serverLevel.players()) {
                player.sendSystemMessage(Component.literal("§cВы потеряли сознание от усталости..."));
                player.teleportTo(serverLevel,
                        serverLevel.getSharedSpawnPos().getX(),
                        serverLevel.getSharedSpawnPos().getY(),
                        serverLevel.getSharedSpawnPos().getZ(),
                        player.getYRot(), player.getXRot());
                forceNextDay(((ServerLevel) level).getLevel());
            }
        }
    }
}

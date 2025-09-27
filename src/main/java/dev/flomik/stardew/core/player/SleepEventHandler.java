package dev.flomik.stardew.core.player;


import dev.flomik.stardew.core.time.ScheduleManager;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SleepEventHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = player.level().getBlockState(pos);

        if (!(state.getBlock() instanceof BedBlock)) return;

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        ServerLevel level = (ServerLevel) player.level();

        player.sendSystemMessage(Component.literal("§bВы ложитесь спать..."));

        ScheduleManager.forceNextDay(level);
    }
}
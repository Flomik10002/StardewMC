package dev.flomik.stardew.common.module.player;

import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import dev.flomik.stardew.common.module.shipping.ShippingManager;
import dev.flomik.stardew.common.module.shipping.network.S2COpenShippingResultScreen;
import dev.flomik.stardew.common.module.time.ScheduleManager;
import dev.flomik.stardew.common.module.time.StardewDateData;
import dev.flomik.stardew.common.module.time.StardewTimeUtils;
import dev.flomik.stardew.core.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SleepEventHandler {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = player.level().getBlockState(pos);

        // Проверяем, кровать ли это
        if (!(state.getBlock() instanceof BedBlock)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        // Отменяем ванильный сон (чтобы не было экрана пропуска ночи и ванильной логики)
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        ServerLevel level = (ServerLevel) player.level();
        long currentTick = level.getDayTime();

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 1. Рассчитываем восстановление энергии
        serverPlayer.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(stardewState -> {
            stardewState.calculateSleepRestoration(currentTick);
        });

        serverPlayer.setRespawnPosition(level.dimension(), pos, 0.0f, true, false);

        // 2. Получаем данные для ShippingResultScreen
        ShippingManager shippingManager = ShippingManager.get(level);
        List<ItemStack> shippedItems = shippingManager.getPlayerShipments(serverPlayer.getUUID());
        
        // 3. Формируем даты
        StardewDateData dateData = StardewDateData.get(level);
        int currentTotalDays = dateData.getTotalDays();
        String yesterdayDate = formatDate(dateData.getDay(), dateData.getSeason(), currentTotalDays);
        
        // Получаем завтрашнюю дату (после перехода дня)
        int nextDay = dateData.getDay() + 1;
        var nextSeason = dateData.getSeason();
        if (nextDay > 28) {
            nextDay = 1;
            nextSeason = nextSeason.next();
        }
        String todayDate = formatDate(nextDay, nextSeason, currentTotalDays + 1);

        // 4. Обрабатываем продажи и получаем заработок
        Map<UUID, Long> earnings = shippingManager.processEndOfDay(level.getServer());
        long playerEarnings = earnings.getOrDefault(serverPlayer.getUUID(), 0L);

        // 5. Отправляем пакет клиенту для открытия ShippingResultScreen
        if (!shippedItems.isEmpty()) {
            PacketHandler.sendToPlayer(
                    new S2COpenShippingResultScreen(shippedItems, yesterdayDate, todayDate, playerEarnings),
                    serverPlayer
            );
        } else {
            // Если нет проданных предметов, просто сообщаем и переходим к следующему дню
            serverPlayer.sendSystemMessage(Component.literal("§bВы ложитесь спать..."));
        }

        // 6. Переходим к следующему дню
        ScheduleManager.forceNextDay(level);
    }

    private static String formatDate(int day, dev.flomik.stardew.common.module.time.Season season, int totalDays) {
        String seasonName = switch (season) {
            case SPRING -> "Spring";
            case SUMMER -> "Summer";
            case FALL -> "Fall";
            case WINTER -> "Winter";
        };
        int year = totalDays / (dev.flomik.stardew.common.module.time.Season.values().length * 28) + 1;
        return "Day " + day + " of " + seasonName + ", Year " + year;
    }
}

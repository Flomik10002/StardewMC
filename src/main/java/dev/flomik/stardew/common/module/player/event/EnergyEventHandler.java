package dev.flomik.stardew.common.module.player.event;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import dev.flomik.stardew.common.module.time.ScheduleManager;
import dev.flomik.stardew.common.module.time.StardewTimeUtils;
import dev.flomik.stardew.core.config.StardewConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class EnergyEventHandler {

    // ... (onBlockBreak и onRightClickBlock без изменений) ...
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative()) return;
        ItemStack stack = player.getMainHandItem();
        if (isTool(stack)) consume(player, 2.0f);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof HoeItem || stack.getItem() instanceof ShovelItem) {
            consume(player, 2.0f);
        }
    }

    // 3. Восстановление энергии едой
    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        // ИСПРАВЛЕНО: event.getEntity().level()
        if (event.getEntity() instanceof Player player && !event.getEntity().level().isClientSide) {
            ItemStack stack = event.getItem();
            if (stack.isEdible() && stack.getItem().getFoodProperties() != null) {
                int nutrition = stack.getItem().getFoodProperties().getNutrition();
                float energyRestored = nutrition * 10.0f;

                player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                    // ИСПРАВЛЕНО: restoreEnergy
                    state.restoreEnergy(energyRestored);
                    player.displayClientMessage(Component.literal("Energy +" + (int)energyRestored), true);
                });
            }
        }
    }

    // ... (onPlayerTick и хелперы без изменений, кроме вызова consume) ...
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        long gameTime = player.serverLevel().getDayTime();
        
        // Проверка на 2:00 AM - игрок должен лечь спать
        // Проверяем момент достижения 2:00 (hour == 2 и minute == 0)
        // В новой системе (1000 тиков/час), 2:00 AM = 20000 тиков.
        if (gameTime % 24000 == 20000) {
            passOutAt2AM(player);
            return;
        }

        if (StardewTimeUtils.shouldPassOut(gameTime)) {
            passOut(player, "You pass out from exhaustion...");
            return;
        }

        player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
            float currentEnergy = state.getCurrentEnergy();
            
            if (currentEnergy <= -15.0f) {
                passOut(player, "You pass out from over-exertion...");
                return;
            }
            
            // Блокируем спринт если он отключен в конфиге
            if (!StardewConfig.COMMON.allowSprinting.get() && player.isSprinting()) {
                player.setSprinting(false);
            }
            
            // По вики: "Increasing energy back above zero will restore player movement"
            // Эффекты замедления применяются только когда энергия <= 0
            // Когда энергия > 0, эффекты исчезают (но флаг isExhausted остается)
            if (currentEnergy <= 0) {
                player.setSprinting(false);
                if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
                }
                if (!player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false, true));
                }
            } else {
                // Убираем эффекты замедления когда энергия > 0
                // НО флаг isExhausted НЕ сбрасываем (он остается до конца дня)
                if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                }
                if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
                    player.removeEffect(MobEffects.DIG_SLOWDOWN);
                }
            }
        });
    }

    private static void consume(Player player, float amount) {
        // ИСПРАВЛЕНО: consumeEnergy
        player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> state.consumeEnergy(amount));
    }

    private static boolean isTool(ItemStack stack) {
        return stack.getItem() instanceof DiggerItem || stack.getItem() instanceof SwordItem;
    }

    private static void passOut(ServerPlayer player, String message) {
        ServerLevel serverLevel = player.serverLevel();
        player.sendSystemMessage(Component.literal("§c" + message));
        player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> state.passOutFromExhaustion());
        ScheduleManager.forceNextDay(serverLevel);
        player.teleportTo(serverLevel,
                serverLevel.getSharedSpawnPos().getX(),
                serverLevel.getSharedSpawnPos().getY(),
                serverLevel.getSharedSpawnPos().getZ(),
                player.getYRot(), player.getXRot());
    }

    /**
     * Обрабатывает обморок в 2:00 AM, если игрок не лег спать.
     * Игрок просыпается на кровати. Если обморок произошел на улице,
     * теряется 10% денег (максимум 1000g).
     */
    private static void passOutAt2AM(ServerPlayer player) {
        ServerLevel serverLevel = player.serverLevel();
        
        // Заглушка: проверка дома ли игрок (по умолчанию - на улице)
        boolean isAtHome = isPlayerAtHome(player);
        
        // Заглушка: потеря денег если обморок на улице
        if (!isAtHome) {
            int moneyLost = calculateMoneyLoss(player);
            // TODO: Реализовать фактическое списание денег
            // player.removeMoney(moneyLost);
            player.sendSystemMessage(Component.literal(
                    String.format("§cВы потеряли §f%d§c золота из-за обморока на улице", moneyLost)
            ));
        }
        
        player.sendSystemMessage(Component.literal("§cВы уснули на улице в 2:00 AM..."));
        player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> state.passOutFromExhaustion());
        
        // Телепортация на кровать (используем respawn позицию)
        if (player.getRespawnPosition() != null) {
            player.teleportTo(serverLevel,
                    player.getRespawnPosition().getX(),
                    player.getRespawnPosition().getY(),
                    player.getRespawnPosition().getZ(),
                    player.getYRot(), player.getXRot());
        } else {
            // Если нет respawn позиции, телепортируем на спавн мира
            player.teleportTo(serverLevel,
                    serverLevel.getSharedSpawnPos().getX(),
                    serverLevel.getSharedSpawnPos().getY(),
                    serverLevel.getSharedSpawnPos().getZ(),
                    player.getYRot(), player.getXRot());
        }
        
        ScheduleManager.forceNextDay(serverLevel);
    }

    /**
     * Заглушка: проверка, дома ли игрок.
     * TODO: Реализовать реальную проверку нахождения игрока в доме.
     * По умолчанию возвращает false (на улице).
     */
    private static boolean isPlayerAtHome(ServerPlayer player) {
        // TODO: Реализовать проверку:
        // - Проверить, находится ли игрок в структуре дома
        // - Проверить координаты относительно спавна кровати
        // - Проверить, в правильном биоме/зоне
        return false; // Заглушка: всегда на улице
    }

    /**
     * Заглушка: расчет потери денег при обмороке на улице.
     * По правилам Stardew Valley: 10% денег, максимум 1000g.
     * TODO: Реализовать получение реального баланса игрока.
     */
    private static int calculateMoneyLoss(ServerPlayer player) {
        // TODO: Реализовать получение баланса игрока
        // int playerMoney = player.getMoney();
        int playerMoney = 0; // Заглушка: предполагаем 0 денег для демонстрации
        
        if (playerMoney <= 0) {
            return 0;
        }
        
        // 10% от баланса, максимум 1000g
        int moneyLost = Math.max(1, (int)(playerMoney * 0.1));
        return Math.min(moneyLost, 1000);
    }
}
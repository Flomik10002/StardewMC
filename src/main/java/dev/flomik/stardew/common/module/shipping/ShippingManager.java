package dev.flomik.stardew.common.module.shipping;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import dev.flomik.stardew.common.registry.framework.tooltip.TooltipPresets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Глобальный менеджер для системы Shipping Bin.
 * Хранит все предметы, отправленные на продажу всеми игроками со всех Shipping Bin на карте.
 * Выплата золота происходит при засыпании (конец дня).
 * 
 * Особенности:
 * - Один глобальный менеджер на мир (SavedData)
 * - Каждый игрок имеет свою очередь предметов
 * - Последний добавленный предмет каждого игрока может быть извлечен обратно
 * - При засыпании все предметы конвертируются в золото
 */
public class ShippingManager extends SavedData {

    private static final String DATA_NAME = StardewMod.MODID + "_shipping";

    // UUID игрока -> Список предметов на продажу
    private final Map<UUID, List<ItemStack>> playerShipments = new HashMap<>();

    // UUID игрока -> Последний добавленный предмет (для возможности отмены)
    private final Map<UUID, ItemStack> lastShippedItem = new HashMap<>();

    public ShippingManager() {
    }

    public static ShippingManager get(Level level) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("ShippingManager can only be accessed on the server side!");
        }

        DimensionDataStorage storage = serverLevel.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(ShippingManager::load, ShippingManager::new, DATA_NAME);
    }

    /**
     * Добавляет предмет в очередь на продажу для указанного игрока.
     * 
     * @param playerId UUID игрока
     * @param stack Предмет для продажи (будет скопирован)
     */
    public void shipItem(UUID playerId, ItemStack stack) {
        if (stack.isEmpty()) return;

        ItemStack toShip = stack.copy();

        playerShipments.computeIfAbsent(playerId, k -> new ArrayList<>()).add(toShip);
        lastShippedItem.put(playerId, toShip.copy());

        setDirty();
    }

    /**
     * Возвращает последний добавленный предмет игроку (если есть).
     * После извлечения предмет удаляется из очереди и lastShippedItem очищается.
     * 
     * @param playerId UUID игрока
     * @return Последний отправленный предмет или ItemStack.EMPTY
     */
    public ItemStack retrieveLastItem(UUID playerId) {
        ItemStack last = lastShippedItem.remove(playerId);
        if (last == null || last.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> shipments = playerShipments.get(playerId);
        if (shipments != null && !shipments.isEmpty()) {
            // Удаляем последний добавленный предмет из списка
            for (int i = shipments.size() - 1; i >= 0; i--) {
                ItemStack inList = shipments.get(i);
                if (ItemStack.isSameItemSameTags(inList, last) && inList.getCount() == last.getCount()) {
                    shipments.remove(i);
                    break;
                }
            }
        }

        setDirty();
        return last;
    }

    /**
     * Проверяет, есть ли у игрока предмет для извлечения.
     */
    public boolean hasLastItem(UUID playerId) {
        ItemStack last = lastShippedItem.get(playerId);
        return last != null && !last.isEmpty();
    }

    /**
     * Получает копию последнего предмета для отображения (без удаления).
     */
    @Nullable
    public ItemStack peekLastItem(UUID playerId) {
        ItemStack last = lastShippedItem.get(playerId);
        return last != null ? last.copy() : null;
    }

    /**
     * Получает копию всех предметов игрока для отображения в ShippingResultScreen.
     * Не удаляет предметы из очереди.
     */
    public List<ItemStack> getPlayerShipments(UUID playerId) {
        List<ItemStack> items = playerShipments.get(playerId);
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        // Возвращаем копии
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack stack : items) {
            copies.add(stack.copy());
        }
        return copies;
    }

    /**
     * Обрабатывает конец дня: продает все предметы и выплачивает золото игрокам.
     * Вызывается при засыпании.
     * @return Map игрок -> заработок
     */
    public Map<UUID, Long> processEndOfDay(MinecraftServer server) {
        Map<UUID, Long> earnings = new HashMap<>();

        // Подсчитываем заработок для каждого игрока
        for (Map.Entry<UUID, List<ItemStack>> entry : playerShipments.entrySet()) {
            UUID playerId = entry.getKey();
            List<ItemStack> items = entry.getValue();

            long totalGold = 0;
            for (ItemStack stack : items) {
                totalGold += calculateSellPrice(stack);
            }

            if (totalGold > 0) {
                earnings.put(playerId, totalGold);
            }
        }

        // Выплачиваем золото онлайн игрокам (тихо, без сообщений - ShippingResultScreen покажет)
        for (Map.Entry<UUID, Long> entry : earnings.entrySet()) {
            UUID playerId = entry.getKey();
            long gold = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                    state.addMoney(gold);
                });
            }
            // TODO: Для офлайн игроков сохранять pending earnings
        }

        // Очищаем все shipments
        playerShipments.clear();
        lastShippedItem.clear();
        setDirty();
        
        return earnings;
    }

    /**
     * Рассчитывает цену продажи предмета с учетом качества и количества.
     */
    private long calculateSellPrice(ItemStack stack) {
        // Получаем базовую цену из тултипа PriceTooltip
        int basePrice = getBasePrice(stack);
        if (basePrice <= 0) return 0;

        Quality quality = Quality.get(stack);
        float priceWithQuality = quality.calculatePrice(basePrice);

        return (long) (priceWithQuality * stack.getCount());
    }

    /**
     * Получает базовую цену предмета.
     * Ищет PriceTooltip среди тултипов предмета.
     */
    private int getBasePrice(ItemStack stack) {
        if (stack.getItem() instanceof dev.flomik.stardew.common.registry.framework.IStardewItem stardewItem) {
            var tooltips = stardewItem.getTooltips();
            if (tooltips != null) {
                for (var tooltip : tooltips) {
                    if (tooltip instanceof TooltipPresets.PriceTooltip priceTooltip) {
                        return priceTooltip.getBasePrice();
                    }
                }
            }
        }
        return 0; // Предмет без цены нельзя продать
    }

    /**
     * Получает общее количество предметов в очереди игрока.
     */
    public int getShipmentCount(UUID playerId) {
        List<ItemStack> items = playerShipments.get(playerId);
        return items != null ? items.size() : 0;
    }

    /**
     * Получает предварительную сумму заработка для игрока.
     */
    public long getEstimatedEarnings(UUID playerId) {
        List<ItemStack> items = playerShipments.get(playerId);
        if (items == null || items.isEmpty()) return 0;

        long total = 0;
        for (ItemStack stack : items) {
            total += calculateSellPrice(stack);
        }
        return total;
    }

    // --- SavedData ---

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag shipmentsTag = new ListTag();

        for (Map.Entry<UUID, List<ItemStack>> entry : playerShipments.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());

            ListTag itemsTag = new ListTag();
            for (ItemStack stack : entry.getValue()) {
                itemsTag.add(stack.save(new CompoundTag()));
            }
            playerTag.put("Items", itemsTag);

            // Сохраняем lastShippedItem
            ItemStack last = lastShippedItem.get(entry.getKey());
            if (last != null && !last.isEmpty()) {
                playerTag.put("LastItem", last.save(new CompoundTag()));
            }

            shipmentsTag.add(playerTag);
        }

        tag.put("Shipments", shipmentsTag);
        return tag;
    }

    public static ShippingManager load(CompoundTag tag) {
        ShippingManager manager = new ShippingManager();

        ListTag shipmentsTag = tag.getList("Shipments", Tag.TAG_COMPOUND);
        for (int i = 0; i < shipmentsTag.size(); i++) {
            CompoundTag playerTag = shipmentsTag.getCompound(i);
            UUID playerId = playerTag.getUUID("Player");

            List<ItemStack> items = new ArrayList<>();
            ListTag itemsTag = playerTag.getList("Items", Tag.TAG_COMPOUND);
            for (int j = 0; j < itemsTag.size(); j++) {
                ItemStack stack = ItemStack.of(itemsTag.getCompound(j));
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
            manager.playerShipments.put(playerId, items);

            if (playerTag.contains("LastItem")) {
                ItemStack last = ItemStack.of(playerTag.getCompound("LastItem"));
                if (!last.isEmpty()) {
                    manager.lastShippedItem.put(playerId, last);
                }
            }
        }

        return manager;
    }
}


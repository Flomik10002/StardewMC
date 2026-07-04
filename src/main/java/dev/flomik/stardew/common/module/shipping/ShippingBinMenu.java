package dev.flomik.stardew.common.module.shipping;

import dev.flomik.stardew.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Меню для Shipping Bin.
 * Содержит один слот для предмета на продажу.
 */
public class ShippingBinMenu extends AbstractContainerMenu {

    private final ShippingBinBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer displayContainer = new SimpleContainer(1);
    private final ShippingSlot shippingSlot;

    // Конструктор для клиента (из сети)
    public ShippingBinMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, 
            (ShippingBinBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    // Конструктор для сервера
    public ShippingBinMenu(int containerId, Inventory playerInventory, ShippingBinBlockEntity blockEntity) {
        super(ModMenuTypes.SHIPPING_BIN.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // Единственный слот показывает только последний отправленный предмет.
        // Новый предмет, положенный сюда, сразу отправляется в ShippingManager и заменяет этот preview.
        this.shippingSlot = new ShippingSlot(displayContainer, 0, 80, 35, this);
        this.addSlot(shippingSlot);
        refreshLastSlot();

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Хотбар
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {
                ItemStack retrieved = takeLastShippedItem();
                if (retrieved.isEmpty()) return ItemStack.EMPTY;

                result = retrieved.copy();
                if (!player.getInventory().add(retrieved)) {
                    player.drop(retrieved, false);
                }
            } else {
                shipInsertedStack(stackInSlot.copy());
                stackInSlot.setCount(0);
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        
        if (!player.level().isClientSide()) {
            blockEntity.stopOpen(player);
        }
    }

    public ShippingBinBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private void shipInsertedStack(ItemStack stack) {
        if (player.level().isClientSide() || stack.isEmpty()) return;

        blockEntity.shipItem(player, stack);
        refreshLastSlot();
    }

    private ItemStack takeLastShippedItem() {
        if (player.level().isClientSide()) return ItemStack.EMPTY;

        ItemStack retrieved = blockEntity.takeLastShippedItem(player);
        refreshLastSlot();
        return retrieved;
    }

    private void refreshLastSlot() {
        if (player.level().isClientSide()) return;

        shippingSlot.setDisplayItem(blockEntity.getLastShippedItem(player));
        broadcastChanges();
    }

    /**
     * Специальный слот для Shipping Bin.
     * Вставка отправляет предмет, извлечение возвращает только текущий last item.
     */
    private static class ShippingSlot extends Slot {
        private final ShippingBinMenu menu;
        private boolean updatingDisplay;

        public ShippingSlot(SimpleContainer container, int slot, int x, int y, ShippingBinMenu menu) {
            super(container, slot, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // TODO: Фильтрация - только предметы с ценой можно продавать
            // Пока разрешаем все предметы
            return true;
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.hasItem();
        }

        @Override
        public ItemStack safeInsert(ItemStack stack, int increment) {
            if (stack.isEmpty() || !mayPlace(stack)) {
                return stack;
            }
            if (menu.player.level().isClientSide()) {
                return super.safeInsert(stack, increment);
            }

            int amount = Math.min(stack.getCount(), increment);
            ItemStack toShip = stack.split(amount);
            menu.shipInsertedStack(toShip);
            return stack;
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            if (updatingDisplay || menu.player.level().isClientSide() || stack.isEmpty()) {
                super.setByPlayer(stack);
                return;
            }

            menu.shipInsertedStack(stack.copy());
            stack.setCount(0);
        }

        @Override
        public void set(ItemStack stack) {
            if (updatingDisplay || menu.player.level().isClientSide() || stack.isEmpty()) {
                super.set(stack);
                return;
            }

            menu.shipInsertedStack(stack.copy());
            stack.setCount(0);
        }

        @Override
        public ItemStack remove(int amount) {
            if (menu.player.level().isClientSide()) {
                return super.remove(amount);
            }

            return menu.takeLastShippedItem();
        }

        public void setDisplayItem(ItemStack stack) {
            updatingDisplay = true;
            super.set(stack.copy());
            updatingDisplay = false;
        }
    }
}

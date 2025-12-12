package dev.flomik.stardew.common.module.machinery.menu;

import dev.flomik.stardew.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class ModBigChestMenu extends AbstractContainerMenu implements IChestMenu {
    private final Container container;
    private final ContainerData data;
    private final BlockPos pos;

    private static final int SLOTS_X = 10;
    private static final int SLOTS_Y = 7;

    private static final int GUI_WIDTH = 196;

    public ModBigChestMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, new SimpleContainer(70), new SimpleContainerData(1), extraData.readBlockPos());
    }

    public ModBigChestMenu(int id, Inventory playerInventory, Container container, ContainerData data, BlockPos pos) {
        super(ModMenuTypes.BIG_CHEST_MENU.get(), id);
        checkContainerSize(container, 70);
        this.container = container;
        this.data = data;
        this.pos = pos;

        container.startOpen(playerInventory.player);
        this.addDataSlots(data);

        int chestStartX = 8; // Стандартный отступ слева
        int chestStartY = 18; // Отступ сверху (под заголовком)

        for (int row = 0; row < SLOTS_Y; ++row) {
            for (int col = 0; col < SLOTS_X; ++col) {
                this.addSlot(new Slot(container, col + row * SLOTS_X,
                        chestStartX + col * 18,
                        chestStartY + row * 18));
            }
        }

        // (196 - 162) / 2 = 17 пикселей отступа слева
        int playerX = (GUI_WIDTH - 162) / 2;
        // Y позиция: StartY + (7 * 18) + отступ (обычно 14px) = 18 + 126 + 14 = 158
        int playerY = chestStartY + (SLOTS_Y * 18) + 14;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        playerX + col * 18,
                        playerY + row * 18));
            }
        }

        // --- ХОТБАР ---
        int hotbarY = playerY + 58;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col,
                    playerX + col * 18,
                    hotbarY));
        }
    }

    @Override
    public int getVariant() { return data.get(0); }

    @Override
    public BlockPos getPos() { return pos; }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 70) {
                if (!this.moveItemStackTo(itemstack1, 70, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 70, false)) return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
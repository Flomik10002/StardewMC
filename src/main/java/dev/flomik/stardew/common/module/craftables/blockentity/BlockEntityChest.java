package dev.flomik.stardew.common.module.craftables.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.module.craftables.menu.ModChestMenu;
import dev.flomik.stardew.common.module.craftables.block.BlockChest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityChest extends RandomizableContainerBlockEntity implements LidBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);

    private final ChestLidController chestLidController = new ChestLidController();

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldVal, int newVal) {
            level.blockEvent(pos, state.getBlock(), 1, newVal);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ModChestMenu menu) {
                return menu.getContainer() == BlockEntityChest.this;
            }
            return false;
        }
    };

    public BlockEntityChest(BlockPos pos, BlockState state) {
        super(ModBlocks.CHEST.getTypeValue(), pos, state);
    }

    @Override
    protected NonNullList<ItemStack> getItems() { return this.items; }

    @Override
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    protected Component getDefaultName() { return Component.literal("Chest"); }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv) {
        ContainerData data = new ContainerData() {
            @Override
            public int get(int index) {
                BlockState state = getLevel().getBlockState(getBlockPos());
                if (state.getBlock() instanceof BlockChest) {
                    return state.getValue(BlockChest.VARIANT);
                }
                return 0;
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 1; }
        };

        return new ModChestMenu(id, playerInv, this, data, this.worldPosition);
    }

    @Override
    public int getContainerSize() { return 36; }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, BlockEntityChest blockEntity) {
        blockEntity.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.chestLidController.shouldBeOpen(type > 0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public float getOpenNess(float partialTick) {
        return this.chestLidController.getOpenness(partialTick);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        boolean wasEmpty = this.isEmpty();
        super.setItem(slot, stack);
        boolean isEmptyNow = this.isEmpty();
        
        // Если состояние "пустоты" изменилось, обновляем BlockState
        if (wasEmpty != isEmptyNow) {
            updateBlockState(!isEmptyNow);
        }
    }
    
    @Override
    public void setChanged() {
        boolean wasEmpty = this.isEmpty();
        super.setChanged();
        boolean isEmptyNow = this.isEmpty();
        
        // В setChanged() тоже проверяем изменение состояния, на всякий случай
        if (this.level != null && !this.level.isClientSide) {
             BlockState state = this.level.getBlockState(this.worldPosition);
             if (state.hasProperty(BlockChest.HAS_ITEMS) && state.getValue(BlockChest.HAS_ITEMS) != !isEmptyNow) {
                 updateBlockState(!isEmptyNow);
             }
        }
    }

    private void updateBlockState(boolean hasItems) {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(BlockChest.HAS_ITEMS)) {
                this.level.setBlock(this.worldPosition, state.setValue(BlockChest.HAS_ITEMS, hasItems), 3);
            }
        }
    }

}
package dev.flomik.stardew.common.module.craftables.blockentity;

import dev.flomik.stardew.common.module.craftables.block.BlockBigChest;
import dev.flomik.stardew.common.module.craftables.menu.ModBigChestMenu;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBigChest extends BlockEntityChest {

    public BlockEntityBigChest(BlockPos pos, BlockState state) {
        this(ModBlocks.BIG_CHEST.getTypeValue(), pos, state);
    }

    public BlockEntityBigChest(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = NonNullList.withSize(70, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return 70;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        ContainerData data = new ContainerData() {
            @Override
            public int get(int index) {
                BlockState state = getLevel().getBlockState(getBlockPos());
                if (state.getBlock() instanceof BlockBigChest) {
                    return state.getValue(BlockBigChest.VARIANT);
                }
                return 0;
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 1; }
        };
        return new ModBigChestMenu(id, player, this, data, this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.stardew.big_chest");
    }
}
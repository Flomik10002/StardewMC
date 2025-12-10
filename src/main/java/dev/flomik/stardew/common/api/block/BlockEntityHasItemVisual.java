package dev.flomik.stardew.common.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityHasItemVisual extends BlockEntity {

    public BlockEntityHasItemVisual(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean shouldRenderItem() {
        return false;
    }

    public ItemStack getVisualItem() {
        return ItemStack.EMPTY;
    }

    public void onPlace() {
    }
}
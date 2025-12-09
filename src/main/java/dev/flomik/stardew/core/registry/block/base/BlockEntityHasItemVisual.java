package dev.flomik.stardew.core.registry.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityHasItemVisual extends BlockEntity {

    public BlockEntityHasItemVisual(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Условие, при котором нужно отрисовать предмет
     */
    public boolean shouldRenderItem() {
        return false;
    }

    /**
     * Что отображать
     */
    public ItemStack getVisualItem() {
        return ItemStack.EMPTY;
    }

    public void onPlace() {
    }
}
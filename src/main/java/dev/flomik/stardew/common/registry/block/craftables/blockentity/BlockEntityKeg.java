package dev.flomik.stardew.common.registry.block.craftables.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.registry.block.base.BlockEntityHasItemVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityKeg extends BlockEntityHasItemVisual {

    private ItemStack visualItem = ItemStack.EMPTY;

    public BlockEntityKeg(BlockPos pos, BlockState state) {
        super(ModBlocks.KEG.getTypeValue(), pos, state);
    }

    @Override
    public boolean shouldRenderItem() {
        return !visualItem.isEmpty();
    }

    @Override
    public ItemStack getVisualItem() {
        return visualItem;
    }

    public void setVisualItem(ItemStack item) {
        this.visualItem = item;
        setChanged();
    }

    @Override
    public void onPlace() {
    }
}

package dev.flomik.stardew.common.module.craftables.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.api.block.BlockEntityHasItemVisual;
import dev.flomik.stardew.common.module.craftables.block.BlockBeeHouse;
import dev.flomik.stardew.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBeeHouse extends BlockEntityHasItemVisual {
    public BlockEntityBeeHouse(BlockPos pos, BlockState state) {
        super(ModBlocks.BEE_HOUSE.getTypeValue(), pos, state);
    }

    @Override
    public boolean shouldRenderItem() {
        return getBlockState().hasProperty(BlockBeeHouse.HAS_HONEY) && getBlockState().getValue(BlockBeeHouse.HAS_HONEY);
    }

    @Override
    public ItemStack getVisualItem() {
        return new ItemStack(ModItems.HONEY.get());
    }

    @Override
    public void onPlace() {
    }
}

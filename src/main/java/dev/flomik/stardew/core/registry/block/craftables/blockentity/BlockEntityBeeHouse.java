package dev.flomik.stardew.core.registry.block.craftables.blockentity;

import dev.flomik.stardew.core.registry.block.base.BlockEntityHasItemVisual;
import dev.flomik.stardew.core.registry.block.craftables.BlockBeeHouse;
import dev.flomik.stardew.core.registry.blockentity.ModBlockEntities;
import dev.flomik.stardew.core.registry.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBeeHouse extends BlockEntityHasItemVisual {
    public BlockEntityBeeHouse(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BEE_HOUSE.get(), pos, state);
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

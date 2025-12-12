package dev.flomik.stardew.common.module.machinery.blockentity;

import dev.flomik.stardew.common.module.machinery.base.AbstractProcessingBlockEntity;
import dev.flomik.stardew.common.module.machinery.recipe.MachineRecipe;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.api.block.BlockEntityHasItemVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public class BlockEntityKeg extends AbstractProcessingBlockEntity {

    public BlockEntityKeg(BlockPos pos, BlockState state) {
        super(ModBlocks.KEG.getTypeValue(), pos, state);
    }

    @Override
    protected List<MachineRecipe> getRecipes() {
        return Collections.emptyList();
    }

    @Override
    public void onPlace() {
    }
}

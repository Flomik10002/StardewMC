package dev.flomik.stardew.common.module.machinery.blockentity;

import dev.flomik.stardew.common.module.machinery.base.AbstractProcessingBlockEntity;
import dev.flomik.stardew.common.module.machinery.recipe.CheesePressRecipes;
import dev.flomik.stardew.common.module.machinery.recipe.MachineRecipe;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockEntityCheesePress extends AbstractProcessingBlockEntity {

    public BlockEntityCheesePress(BlockPos pos, BlockState state) {
        super(ModBlocks.CHEESE_PRESS.getTypeValue(), pos, state);
    }

    @Override
    protected List<MachineRecipe> getRecipes() {
        return List.of(
                CheesePressRecipes.MILK,
                CheesePressRecipes.LARGE_MILK
        );
    }

    @Override
    public void onPlace() {
    }
}

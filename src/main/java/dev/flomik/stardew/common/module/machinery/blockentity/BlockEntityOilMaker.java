package dev.flomik.stardew.common.module.machinery.blockentity;

import dev.flomik.stardew.common.module.machinery.base.AbstractProcessingBlockEntity;
import dev.flomik.stardew.common.module.machinery.recipe.MachineRecipe;
import dev.flomik.stardew.common.module.machinery.recipe.OilMakerRecipes;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockEntityOilMaker extends AbstractProcessingBlockEntity {

    public BlockEntityOilMaker(BlockPos pos, BlockState state) {
        super(ModBlocks.OIL_MAKER.getTypeValue(), pos, state);
    }

    @Override
    protected List<MachineRecipe> getRecipes() {
        return List.of(
                OilMakerRecipes.OIL_FROM_CORN,
                OilMakerRecipes.OIL_FROM_SUNFLOWER,
                OilMakerRecipes.OIL_FROM_SUNFLOWER_SEEDS,
                OilMakerRecipes.TRUFFLE_OIL
        );
    }

    @Override
    public void onPlace() {
    }
}

package dev.flomik.stardew.core.registry.block.surface;

import dev.flomik.stardew.core.registry.block.shape.Shape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class BlockGrassFull extends BlockGrassSurface {

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction dir,
                                  BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SHAPE, Shape.CENTER)
                .setValue(SEASON, getCurrentSeason(level));
    }
}

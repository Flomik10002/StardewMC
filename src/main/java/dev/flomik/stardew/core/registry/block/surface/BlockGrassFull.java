package dev.flomik.stardew.core.registry.block.surface;

import dev.flomik.stardew.core.registry.block.shape.Shape;
import dev.flomik.stardew.core.time.Season;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public class BlockGrassFull extends BlockGrassSurface {

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(SHAPE) != Shape.CENTER) {
            return state.setValue(SHAPE, Shape.CENTER);
        }
        return state;
    }
}

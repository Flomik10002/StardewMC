package dev.flomik.stardew.common.module.nature.block;

import dev.flomik.stardew.common.api.block.Shape;
import dev.flomik.stardew.core.util.ShapeCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class BlockGrassSurface extends Block {

    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

    public BlockGrassSurface(Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, Shape.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(SHAPE,
                ShapeCalculator.calculateGrassShape(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SHAPE, ShapeCalculator.calculateGrassShape(level, pos));
    }
}
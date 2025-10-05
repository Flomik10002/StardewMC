package dev.flomik.stardew.core.block.craftables;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCheesePress extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7.000, 18.000, 7.000, 9.000, 26.000, 9.000),
            Block.box(2.000, 0.000, 2.000, 14.000, 12.000, 14.000),
            Block.box(4.000, 12.000, 4.000, 12.000, 16.000, 12.000),
            Block.box(6.000, 16.000, 6.000, 10.000, 18.000, 10.000)
    );
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockCheesePress(Properties props) {
        super(props);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}

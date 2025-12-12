package dev.flomik.stardew.common.module.machinery.block;

import dev.flomik.stardew.common.api.block.BlockEntityHasItemVisual;
import dev.flomik.stardew.common.module.machinery.base.AbstractProcessingBlockEntity;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityCheesePress;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityOilMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockOilMaker extends Block implements EntityBlock {
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(1.000, 1.000, 1.000, 15.000, 9.000, 15.000),
            Block.box(2.000, 0.000, 2.000, 4.000, 1.000, 4.000),
            Block.box(12.000, 0.000, 12.000, 14.000, 1.000, 14.000),
            Block.box(2.000, 0.000, 12.000, 4.000, 1.000, 14.000),
            Block.box(12.000, 0.000, 2.000, 14.000, 1.000, 4.000),
            Block.box(5.000, 4.000, 0.000, 11.000, 6.000, 1.000),
            Block.box(9.000, 17.000, 8.000, 10.000, 22.000, 9.000),
            Block.box(4.000, 14.000, 6.000, 5.000, 22.000, 7.000),
            Block.box(5.000, 22.000, 8.000, 10.000, 23.000, 9.000),
            Block.box(4.000, 22.000, 6.000, 5.000, 23.000, 9.000),
            Block.box(9.000, 9.000, 8.000, 12.000, 11.000, 11.000),
            Block.box(10.000, 11.000, 9.000, 11.000, 12.000, 10.000),
            Block.box(9.000, 16.000, 8.000, 12.000, 17.000, 11.000),
            Block.box(9.000, 12.000, 8.000, 12.000, 13.000, 11.000),
            Block.box(3.000, 9.000, 5.000, 6.000, 14.000, 8.000),
            Block.box(4.000, 10.000, 6.000, 5.000, 13.000, 7.000),
            Block.box(3.000, 20.000, 5.000, 6.000, 21.000, 11.000),
            Block.box(7.000, 18.000, 7.000, 11.000, 19.000, 11.000),
            Block.box(9.000, 12.000, 7.000, 12.000, 17.000, 8.000),
            Block.box(8.000, 12.000, 8.000, 9.000, 17.000, 11.000),
            Block.box(12.000, 12.000, 8.000, 13.000, 17.000, 11.000),
            Block.box(12.000, 13.000, 7.000, 13.000, 16.000, 8.000),
            Block.box(12.000, 13.000, 11.000, 13.000, 16.000, 12.000),
            Block.box(8.000, 13.000, 7.000, 9.000, 16.000, 8.000),
            Block.box(8.000, 13.000, 11.000, 9.000, 16.000, 12.000),
            Block.box(9.000, 12.000, 11.000, 12.000, 17.000, 12.000),
            Block.box(6.000, 9.000, 10.000, 7.000, 23.000, 11.000)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(1.000, 1.000, 1.000, 15.000, 9.000, 15.000),
            Block.box(12.000, 0.000, 2.000, 14.000, 1.000, 4.000),
            Block.box(2.000, 0.000, 12.000, 4.000, 1.000, 14.000),
            Block.box(2.000, 0.000, 2.000, 4.000, 1.000, 4.000),
            Block.box(12.000, 0.000, 12.000, 14.000, 1.000, 14.000),
            Block.box(15.000, 4.000, 5.000, 16.000, 6.000, 11.000),
            Block.box(7.000, 17.000, 9.000, 8.000, 22.000, 10.000),
            Block.box(9.000, 14.000, 4.000, 10.000, 22.000, 5.000),
            Block.box(7.000, 22.000, 5.000, 8.000, 23.000, 10.000),
            Block.box(7.000, 22.000, 4.000, 10.000, 23.000, 5.000),
            Block.box(5.000, 9.000, 9.000, 8.000, 11.000, 12.000),
            Block.box(6.000, 11.000, 10.000, 7.000, 12.000, 11.000),
            Block.box(5.000, 16.000, 9.000, 8.000, 17.000, 12.000),
            Block.box(5.000, 12.000, 9.000, 8.000, 13.000, 12.000),
            Block.box(8.000, 9.000, 3.000, 11.000, 14.000, 6.000),
            Block.box(9.000, 10.000, 4.000, 10.000, 13.000, 5.000),
            Block.box(5.000, 20.000, 3.000, 11.000, 21.000, 6.000),
            Block.box(5.000, 18.000, 7.000, 9.000, 19.000, 11.000),
            Block.box(8.000, 12.000, 9.000, 9.000, 17.000, 12.000),
            Block.box(5.000, 12.000, 8.000, 8.000, 17.000, 9.000),
            Block.box(5.000, 12.000, 12.000, 8.000, 17.000, 13.000),
            Block.box(8.000, 13.000, 12.000, 9.000, 16.000, 13.000),
            Block.box(4.000, 13.000, 12.000, 5.000, 16.000, 13.000),
            Block.box(8.000, 13.000, 8.000, 9.000, 16.000, 9.000),
            Block.box(4.000, 13.000, 8.000, 5.000, 16.000, 9.000),
            Block.box(4.000, 12.000, 9.000, 5.000, 17.000, 12.000),
            Block.box(5.000, 9.000, 6.000, 6.000, 23.000, 7.000)
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(1.000, 1.000, 1.000, 15.000, 9.000, 15.000),
            Block.box(12.000, 0.000, 12.000, 14.000, 1.000, 14.000),
            Block.box(2.000, 0.000, 2.000, 4.000, 1.000, 4.000),
            Block.box(12.000, 0.000, 2.000, 14.000, 1.000, 4.000),
            Block.box(2.000, 0.000, 12.000, 4.000, 1.000, 14.000),
            Block.box(5.000, 4.000, 15.000, 11.000, 6.000, 16.000),
            Block.box(6.000, 17.000, 7.000, 7.000, 22.000, 8.000),
            Block.box(11.000, 14.000, 9.000, 12.000, 22.000, 10.000),
            Block.box(6.000, 22.000, 7.000, 11.000, 23.000, 8.000),
            Block.box(11.000, 22.000, 7.000, 12.000, 23.000, 10.000),
            Block.box(4.000, 9.000, 5.000, 7.000, 11.000, 8.000),
            Block.box(5.000, 11.000, 6.000, 6.000, 12.000, 7.000),
            Block.box(4.000, 16.000, 5.000, 7.000, 17.000, 8.000),
            Block.box(4.000, 12.000, 5.000, 7.000, 13.000, 8.000),
            Block.box(10.000, 9.000, 8.000, 13.000, 14.000, 11.000),
            Block.box(11.000, 10.000, 9.000, 12.000, 13.000, 10.000),
            Block.box(10.000, 20.000, 5.000, 13.000, 21.000, 11.000),
            Block.box(5.000, 18.000, 5.000, 9.000, 19.000, 9.000),
            Block.box(4.000, 12.000, 8.000, 7.000, 17.000, 9.000),
            Block.box(7.000, 12.000, 5.000, 8.000, 17.000, 8.000),
            Block.box(3.000, 12.000, 5.000, 4.000, 17.000, 8.000),
            Block.box(3.000, 13.000, 8.000, 4.000, 16.000, 9.000),
            Block.box(3.000, 13.000, 4.000, 4.000, 16.000, 5.000),
            Block.box(7.000, 13.000, 8.000, 8.000, 16.000, 9.000),
            Block.box(7.000, 13.000, 4.000, 8.000, 16.000, 5.000),
            Block.box(4.000, 12.000, 4.000, 7.000, 17.000, 5.000),
            Block.box(9.000, 9.000, 5.000, 10.000, 23.000, 6.000)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(1.000, 1.000, 1.000, 15.000, 9.000, 15.000),
            Block.box(2.000, 0.000, 12.000, 4.000, 1.000, 14.000),
            Block.box(12.000, 0.000, 2.000, 14.000, 1.000, 4.000),
            Block.box(12.000, 0.000, 12.000, 14.000, 1.000, 14.000),
            Block.box(2.000, 0.000, 2.000, 4.000, 1.000, 4.000),
            Block.box(0.000, 4.000, 5.000, 1.000, 6.000, 11.000),
            Block.box(8.000, 17.000, 6.000, 9.000, 22.000, 7.000),
            Block.box(6.000, 14.000, 11.000, 7.000, 22.000, 12.000),
            Block.box(8.000, 22.000, 6.000, 9.000, 23.000, 11.000),
            Block.box(6.000, 22.000, 11.000, 9.000, 23.000, 12.000),
            Block.box(8.000, 9.000, 4.000, 11.000, 11.000, 7.000),
            Block.box(9.000, 11.000, 5.000, 10.000, 12.000, 6.000),
            Block.box(8.000, 16.000, 4.000, 11.000, 17.000, 7.000),
            Block.box(8.000, 12.000, 4.000, 11.000, 13.000, 7.000),
            Block.box(5.000, 9.000, 10.000, 8.000, 14.000, 13.000),
            Block.box(6.000, 10.000, 11.000, 7.000, 13.000, 12.000),
            Block.box(5.000, 20.000, 10.000, 11.000, 21.000, 13.000),
            Block.box(7.000, 18.000, 5.000, 11.000, 19.000, 9.000),
            Block.box(7.000, 12.000, 4.000, 8.000, 17.000, 7.000),
            Block.box(8.000, 12.000, 7.000, 11.000, 17.000, 8.000),
            Block.box(8.000, 12.000, 3.000, 11.000, 17.000, 4.000),
            Block.box(7.000, 13.000, 3.000, 8.000, 16.000, 4.000),
            Block.box(11.000, 13.000, 3.000, 12.000, 16.000, 4.000),
            Block.box(7.000, 13.000, 7.000, 8.000, 16.000, 8.000),
            Block.box(11.000, 13.000, 7.000, 12.000, 16.000, 8.000),
            Block.box(11.000, 12.000, 4.000, 12.000, 17.000, 7.000),
            Block.box(10.000, 9.000, 9.000, 11.000, 23.000, 10.000)
    );

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockOilMaker(Properties props) {
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
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractProcessingBlockEntity processor) {
                if (processor.interact(player, hand)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityOilMaker(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BlockEntityHasItemVisual be) {
            be.onPlace();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos1, st, be) -> {
            if (be instanceof AbstractProcessingBlockEntity processor) processor.tick();
        };
    }
}

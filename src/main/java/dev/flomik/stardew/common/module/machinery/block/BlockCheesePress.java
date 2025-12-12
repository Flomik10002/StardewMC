package dev.flomik.stardew.common.module.machinery.block;

import dev.flomik.stardew.common.api.block.BlockEntityHasItemVisual;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityCheesePress;
import dev.flomik.stardew.common.module.machinery.base.AbstractProcessingBlockEntity;
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

public class BlockCheesePress extends Block implements EntityBlock {
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
        return new BlockEntityCheesePress(pos, state);
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

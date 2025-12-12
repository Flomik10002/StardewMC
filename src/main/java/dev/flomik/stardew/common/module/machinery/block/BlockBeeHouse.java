package dev.flomik.stardew.common.module.machinery.block;

import dev.flomik.stardew.common.api.block.BlockEntityHasItemVisual;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityBeeHouse;
import dev.flomik.stardew.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockBeeHouse extends Block implements EntityBlock {
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(1.000, 0.000, 12.000, 4.000, 8.000, 15.000),
            Block.box(12.000, 0.000, 12.000, 15.000, 8.000, 15.000),
            Block.box(12.000, 0.000, 1.000, 15.000, 8.000, 4.000),
            Block.box(1.000, 0.000, 1.000, 4.000, 8.000, 4.000),
            Block.box(1.000, 8.000, 1.000, 15.000, 12.000, 15.000),
            Block.box(2.000, 18.000, 1.000, 14.000, 19.000, 15.000),
            Block.box(1.000, 17.000, 1.000, 15.000, 18.000, 15.000),
            Block.box(3.000, 19.000, 1.000, 13.000, 20.000, 15.000),
            Block.box(5.000, 21.000, 1.000, 11.000, 22.000, 15.000),
            Block.box(6.000, 22.000, 1.000, 10.000, 23.000, 15.000),
            Block.box(7.000, 23.000, 1.000, 9.000, 24.000, 15.000),
            Block.box(4.000, 20.000, 1.000, 12.000, 21.000, 15.000),
            Block.box(1.000, 12.000, 1.000, 4.000, 17.000, 15.000),
            Block.box(3.000, 17.000, 0.000, 13.000, 18.000, 1.000),
            Block.box(4.000, 11.000, 0.000, 12.000, 12.000, 1.000),
            Block.box(4.000, 12.000, 2.000, 12.000, 17.000, 3.000),
            Block.box(4.000, 12.000, 13.000, 12.000, 17.000, 15.000),
            Block.box(12.000, 12.000, 1.000, 15.000, 17.000, 15.000),
            Block.box(14.000, 18.000, 0.000, 16.000, 19.000, 16.000),
            Block.box(-1.000, 17.000, 0.000, 1.000, 18.000, 16.000),
            Block.box(0.000, 18.000, 0.000, 2.000, 19.000, 16.000),
            Block.box(15.000, 17.000, 0.000, 17.000, 18.000, 16.000),
            Block.box(16.000, 16.000, 0.000, 18.000, 17.000, 16.000),
            Block.box(-2.000, 16.000, 0.000, 0.000, 17.000, 16.000),
            Block.box(3.000, 21.000, 0.000, 5.000, 22.000, 16.000),
            Block.box(2.000, 20.000, 0.000, 4.000, 21.000, 16.000),
            Block.box(1.000, 19.000, 0.000, 3.000, 20.000, 16.000),
            Block.box(6.000, 24.000, 0.000, 8.000, 25.000, 16.000),
            Block.box(5.000, 23.000, 0.000, 7.000, 24.000, 16.000),
            Block.box(4.000, 22.000, 0.000, 6.000, 23.000, 16.000),
            Block.box(13.000, 19.000, 0.000, 15.000, 20.000, 16.000),
            Block.box(12.000, 20.000, 0.000, 14.000, 21.000, 16.000),
            Block.box(11.000, 21.000, 0.000, 13.000, 22.000, 16.000),
            Block.box(8.000, 24.000, 0.000, 10.000, 25.000, 16.000),
            Block.box(9.000, 23.000, 0.000, 11.000, 24.000, 16.000),
            Block.box(10.000, 22.000, 0.000, 12.000, 23.000, 16.000),
            Block.box(7.000, 25.000, 0.000, 9.000, 26.000, 16.000)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(1.000, 0.000, 1.000, 4.000, 8.000, 4.000),
            Block.box(1.000, 0.000, 12.000, 4.000, 8.000, 15.000),
            Block.box(12.000, 0.000, 12.000, 15.000, 8.000, 15.000),
            Block.box(12.000, 0.000, 1.000, 15.000, 8.000, 4.000),
            Block.box(1.000, 8.000, 1.000, 15.000, 12.000, 15.000),
            Block.box(1.000, 18.000, 2.000, 15.000, 19.000, 14.000),
            Block.box(1.000, 17.000, 1.000, 15.000, 18.000, 15.000),
            Block.box(1.000, 19.000, 3.000, 15.000, 20.000, 13.000),
            Block.box(1.000, 21.000, 5.000, 15.000, 22.000, 11.000),
            Block.box(1.000, 22.000, 6.000, 15.000, 23.000, 10.000),
            Block.box(1.000, 23.000, 7.000, 15.000, 24.000, 9.000),
            Block.box(1.000, 20.000, 4.000, 15.000, 21.000, 12.000),
            Block.box(1.000, 12.000, 1.000, 15.000, 17.000, 4.000),
            Block.box(15.000, 17.000, 3.000, 16.000, 18.000, 13.000),
            Block.box(15.000, 11.000, 4.000, 16.000, 12.000, 12.000),
            Block.box(13.000, 12.000, 4.000, 14.000, 17.000, 12.000),
            Block.box(1.000, 12.000, 4.000, 3.000, 17.000, 12.000),
            Block.box(1.000, 12.000, 12.000, 15.000, 17.000, 15.000),
            Block.box(0.000, 18.000, 14.000, 16.000, 19.000, 16.000),
            Block.box(0.000, 17.000, -1.000, 16.000, 18.000, 1.000),
            Block.box(0.000, 18.000, 0.000, 16.000, 19.000, 2.000),
            Block.box(0.000, 17.000, 15.000, 16.000, 18.000, 17.000),
            Block.box(0.000, 16.000, 16.000, 16.000, 17.000, 18.000),
            Block.box(0.000, 16.000, -2.000, 16.000, 17.000, 0.000),
            Block.box(0.000, 21.000, 3.000, 16.000, 22.000, 5.000),
            Block.box(0.000, 20.000, 2.000, 16.000, 21.000, 4.000),
            Block.box(0.000, 19.000, 1.000, 16.000, 20.000, 3.000),
            Block.box(0.000, 24.000, 6.000, 16.000, 25.000, 8.000),
            Block.box(0.000, 23.000, 5.000, 16.000, 24.000, 7.000),
            Block.box(0.000, 22.000, 4.000, 16.000, 23.000, 6.000),
            Block.box(0.000, 19.000, 13.000, 16.000, 20.000, 15.000),
            Block.box(0.000, 20.000, 12.000, 16.000, 21.000, 14.000),
            Block.box(0.000, 21.000, 11.000, 16.000, 22.000, 13.000),
            Block.box(0.000, 24.000, 8.000, 16.000, 25.000, 10.000),
            Block.box(0.000, 23.000, 9.000, 16.000, 24.000, 11.000),
            Block.box(0.000, 22.000, 10.000, 16.000, 23.000, 12.000),
            Block.box(0.000, 25.000, 7.000, 16.000, 26.000, 9.000)
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(12.000, 0.000, 1.000, 15.000, 8.000, 4.000),
            Block.box(1.000, 0.000, 1.000, 4.000, 8.000, 4.000),
            Block.box(1.000, 0.000, 12.000, 4.000, 8.000, 15.000),
            Block.box(12.000, 0.000, 12.000, 15.000, 8.000, 15.000),
            Block.box(1.000, 8.000, 1.000, 15.000, 12.000, 15.000),
            Block.box(2.000, 18.000, 1.000, 14.000, 19.000, 15.000),
            Block.box(1.000, 17.000, 1.000, 15.000, 18.000, 15.000),
            Block.box(3.000, 19.000, 1.000, 13.000, 20.000, 15.000),
            Block.box(5.000, 21.000, 1.000, 11.000, 22.000, 15.000),
            Block.box(6.000, 22.000, 1.000, 10.000, 23.000, 15.000),
            Block.box(7.000, 23.000, 1.000, 9.000, 24.000, 15.000),
            Block.box(4.000, 20.000, 1.000, 12.000, 21.000, 15.000),
            Block.box(12.000, 12.000, 1.000, 15.000, 17.000, 15.000),
            Block.box(3.000, 17.000, 15.000, 13.000, 18.000, 16.000),
            Block.box(4.000, 11.000, 15.000, 12.000, 12.000, 16.000),
            Block.box(4.000, 12.000, 13.000, 12.000, 17.000, 14.000),
            Block.box(4.000, 12.000, 1.000, 12.000, 17.000, 3.000),
            Block.box(1.000, 12.000, 1.000, 4.000, 17.000, 15.000),
            Block.box(0.000, 18.000, 0.000, 2.000, 19.000, 16.000),
            Block.box(15.000, 17.000, 0.000, 17.000, 18.000, 16.000),
            Block.box(14.000, 18.000, 0.000, 16.000, 19.000, 16.000),
            Block.box(-1.000, 17.000, 0.000, 1.000, 18.000, 16.000),
            Block.box(-2.000, 16.000, 0.000, 0.000, 17.000, 16.000),
            Block.box(16.000, 16.000, 0.000, 18.000, 17.000, 16.000),
            Block.box(11.000, 21.000, 0.000, 13.000, 22.000, 16.000),
            Block.box(12.000, 20.000, 0.000, 14.000, 21.000, 16.000),
            Block.box(13.000, 19.000, 0.000, 15.000, 20.000, 16.000),
            Block.box(8.000, 24.000, 0.000, 10.000, 25.000, 16.000),
            Block.box(9.000, 23.000, 0.000, 11.000, 24.000, 16.000),
            Block.box(10.000, 22.000, 0.000, 12.000, 23.000, 16.000),
            Block.box(1.000, 19.000, 0.000, 3.000, 20.000, 16.000),
            Block.box(2.000, 20.000, 0.000, 4.000, 21.000, 16.000),
            Block.box(3.000, 21.000, 0.000, 5.000, 22.000, 16.000),
            Block.box(6.000, 24.000, 0.000, 8.000, 25.000, 16.000),
            Block.box(5.000, 23.000, 0.000, 7.000, 24.000, 16.000),
            Block.box(4.000, 22.000, 0.000, 6.000, 23.000, 16.000),
            Block.box(7.000, 25.000, 0.000, 9.000, 26.000, 16.000)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(12.000, 0.000, 12.000, 15.000, 8.000, 15.000),
            Block.box(12.000, 0.000, 1.000, 15.000, 8.000, 4.000),
            Block.box(1.000, 0.000, 1.000, 4.000, 8.000, 4.000),
            Block.box(1.000, 0.000, 12.000, 4.000, 8.000, 15.000),
            Block.box(1.000, 8.000, 1.000, 15.000, 12.000, 15.000),
            Block.box(1.000, 18.000, 2.000, 15.000, 19.000, 14.000),
            Block.box(1.000, 17.000, 1.000, 15.000, 18.000, 15.000),
            Block.box(1.000, 19.000, 3.000, 15.000, 20.000, 13.000),
            Block.box(1.000, 21.000, 5.000, 15.000, 22.000, 11.000),
            Block.box(1.000, 22.000, 6.000, 15.000, 23.000, 10.000),
            Block.box(1.000, 23.000, 7.000, 15.000, 24.000, 9.000),
            Block.box(1.000, 20.000, 4.000, 15.000, 21.000, 12.000),
            Block.box(1.000, 12.000, 12.000, 15.000, 17.000, 15.000),
            Block.box(0.000, 17.000, 3.000, 1.000, 18.000, 13.000),
            Block.box(0.000, 11.000, 4.000, 1.000, 12.000, 12.000),
            Block.box(2.000, 12.000, 4.000, 3.000, 17.000, 12.000),
            Block.box(13.000, 12.000, 4.000, 15.000, 17.000, 12.000),
            Block.box(1.000, 12.000, 1.000, 15.000, 17.000, 4.000),
            Block.box(0.000, 18.000, 0.000, 16.000, 19.000, 2.000),
            Block.box(0.000, 17.000, 15.000, 16.000, 18.000, 17.000),
            Block.box(0.000, 18.000, 14.000, 16.000, 19.000, 16.000),
            Block.box(0.000, 17.000, -1.000, 16.000, 18.000, 1.000),
            Block.box(0.000, 16.000, -2.000, 16.000, 17.000, 0.000),
            Block.box(0.000, 16.000, 16.000, 16.000, 17.000, 18.000),
            Block.box(0.000, 21.000, 11.000, 16.000, 22.000, 13.000),
            Block.box(0.000, 20.000, 12.000, 16.000, 21.000, 14.000),
            Block.box(0.000, 19.000, 13.000, 16.000, 20.000, 15.000),
            Block.box(0.000, 24.000, 8.000, 16.000, 25.000, 10.000),
            Block.box(0.000, 23.000, 9.000, 16.000, 24.000, 11.000),
            Block.box(0.000, 22.000, 10.000, 16.000, 23.000, 12.000),
            Block.box(0.000, 19.000, 1.000, 16.000, 20.000, 3.000),
            Block.box(0.000, 20.000, 2.000, 16.000, 21.000, 4.000),
            Block.box(0.000, 21.000, 3.000, 16.000, 22.000, 5.000),
            Block.box(0.000, 24.000, 6.000, 16.000, 25.000, 8.000),
            Block.box(0.000, 23.000, 5.000, 16.000, 24.000, 7.000),
            Block.box(0.000, 22.000, 4.000, 16.000, 23.000, 6.000),
            Block.box(0.000, 25.000, 7.000, 16.000, 26.000, 9.000)
    );

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_HONEY = BooleanProperty.create("has_honey");


    public BlockBeeHouse(Properties props) {
        super(props);
        registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_HONEY, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_HONEY);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && state.getValue(HAS_HONEY)) {
            ItemStack honey = new ItemStack(ModItems.HONEY.get());
            boolean added = player.getInventory().add(honey);

            if (!added) {
                player.drop(honey, false);
            }

            level.setBlock(pos, state.setValue(HAS_HONEY, false), 3);

            level.playSound(null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0f, 1.0f);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBeeHouse(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BlockEntityHasItemVisual be) {
            be.onPlace();
        }
    }

}
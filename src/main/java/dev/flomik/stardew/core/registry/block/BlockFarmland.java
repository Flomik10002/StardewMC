package dev.flomik.stardew.core.registry.block;

import dev.flomik.stardew.core.block.shape.PlotShape;
import dev.flomik.stardew.core.crop.FertilizerType;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BlockFarmland extends Block implements EntityBlock {

    public static final BooleanProperty HYDRATED = BooleanProperty.create("hydrated");

    public static final EnumProperty<FertilizerType> FERTILIZER = EnumProperty.create(
            "fertilizer", FertilizerType.class, Arrays.stream(FertilizerType.values())
                    .filter(f -> f != FertilizerType.TREE_FERTILIZER)
                    .toArray(FertilizerType[]::new)
    );

    public static final EnumProperty<PlotShape> SHAPE = EnumProperty.create("shape", PlotShape.class);
    public static final EnumProperty<PlotShape> WET_SHAPE = EnumProperty.create("wet_shape", PlotShape.class);

    public BlockFarmland() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .dynamicShape()
                .strength(0.5F, 0.5F)
                .sound(SoundType.GRAVEL)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(HYDRATED, false).setValue(FERTILIZER, FertilizerType.NONE).setValue(SHAPE, PlotShape.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HYDRATED, FERTILIZER, SHAPE, WET_SHAPE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FarmlandBlockEntity(pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SHAPE, calculateShape(level, pos))
                .setValue(WET_SHAPE, calculateWetShape(level, pos));
    }

    private PlotShape calculateShape(LevelAccessor level, BlockPos pos) {
        boolean up    = isSameFarmland(level.getBlockState(pos.north()));
        boolean down  = isSameFarmland(level.getBlockState(pos.south()));
        boolean left  = isSameFarmland(level.getBlockState(pos.west()));
        boolean right = isSameFarmland(level.getBlockState(pos.east()));

        if (up && down && left && right) return PlotShape.CENTER;

        if (!up   && down && left && right) return PlotShape.TOP;
        if (up    && !down && left && right) return PlotShape.BOTTOM;
        if (up    && down && !left && right) return PlotShape.LEFT;
        if (up    && down && left && !right) return PlotShape.RIGHT;

        if (left && right && !up && !down) return PlotShape.HORIZONTAL_MID;
        if (up && down && !left && !right) return PlotShape.VERTICAL_MID;

        if (up && right && !down && !left)   return PlotShape.BOTTOM_LEFT;
        if (up && left  && !down && !right)  return PlotShape.BOTTOM_RIGHT;
        if (down && right && !up && !left)   return PlotShape.TOP_LEFT;
        if (down && left  && !up && !right)  return PlotShape.TOP_RIGHT;

        if (right && !left && !up && !down)  return PlotShape.HORIZONTAL_LEFT;
        if (left && !right && !up && !down)  return PlotShape.HORIZONTAL_RIGHT;
        if (down && !up && !left && !right)  return PlotShape.VERTICAL_TOP;
        if (up && !down && !left && !right)  return PlotShape.VERTICAL_BOTTOM;

        return PlotShape.SINGLE;
    }

    private PlotShape calculateWetShape(LevelAccessor level, BlockPos pos) {
        boolean up    = isHydratedFarmland(level.getBlockState(pos.north()));
        boolean down  = isHydratedFarmland(level.getBlockState(pos.south()));
        boolean left  = isHydratedFarmland(level.getBlockState(pos.west()));
        boolean right = isHydratedFarmland(level.getBlockState(pos.east()));

        if (up && down && left && right) return PlotShape.CENTER;

        if (!up   && down && left && right) return PlotShape.TOP;
        if (up    && !down && left && right) return PlotShape.BOTTOM;
        if (up    && down && !left && right) return PlotShape.LEFT;
        if (up    && down && left && !right) return PlotShape.RIGHT;

        if (left && right && !up && !down) return PlotShape.HORIZONTAL_MID;
        if (up && down && !left && !right) return PlotShape.VERTICAL_MID;

        if (up && right && !down && !left)   return PlotShape.BOTTOM_LEFT;
        if (up && left  && !down && !right)  return PlotShape.BOTTOM_RIGHT;
        if (down && right && !up && !left)   return PlotShape.TOP_LEFT;
        if (down && left  && !up && !right)  return PlotShape.TOP_RIGHT;

        if (right && !left && !up && !down)  return PlotShape.HORIZONTAL_LEFT;
        if (left && !right && !up && !down)  return PlotShape.HORIZONTAL_RIGHT;
        if (down && !up && !left && !right)  return PlotShape.VERTICAL_TOP;
        if (up && !down && !left && !right)  return PlotShape.VERTICAL_BOTTOM;

        return PlotShape.SINGLE;
    }

    private boolean isSameFarmland(BlockState state) {
        return state.getBlock() instanceof BlockFarmland;
    }

    private boolean isHydratedFarmland(BlockState state) {
        return isSameFarmland(state) && state.getValue(HYDRATED);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FarmlandBlockEntity be) {
            be.onPlace();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof FarmlandBlockEntity fbe) FarmlandBlockEntity.serverTick(lvl, pos, st, fbe);
        };
    }
}

package dev.flomik.stardew.core.registry.block.surface;

import dev.flomik.stardew.core.registry.block.shape.Shape;
import dev.flomik.stardew.core.crop.FertilizerType;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final EnumProperty<Shape> WET_SHAPE = EnumProperty.create("wet_shape", Shape.class);
    public static final EnumProperty<Season> SEASON = EnumProperty.create("season", Season.class);

    public BlockFarmland() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .dynamicShape()
                .strength(0.5F, 0.5F)
                .sound(SoundType.GRAVEL)
                .randomTicks()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HYDRATED, false)
                .setValue(FERTILIZER, FertilizerType.NONE)
                .setValue(SHAPE, Shape.SINGLE)
                .setValue(SEASON, Season.SPRING));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Season current = getCurrentSeason(level);
        Shape currentShape = state.getValue(SHAPE);
        Shape recalculated = calculateShape(level, pos);

        if (state.getValue(SEASON) != current || currentShape != recalculated) {
            level.setBlock(pos, state.setValue(SEASON, current).setValue(SHAPE, recalculated), Block.UPDATE_CLIENTS);
        }
//        if (state.getValue(SEASON) != current) {
//            level.setBlockAndUpdate(pos, state.setValue(SEASON, current));
//        }

        if (random.nextFloat() < 0.15f) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos np = pos.relative(dir);
                BlockState ns = level.getBlockState(np);
                if (ns.getBlock() instanceof BlockFarmland farmland) {
                    level.scheduleTick(np, farmland, 20 + random.nextInt(20));
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HYDRATED, FERTILIZER, SHAPE, WET_SHAPE, SEASON);
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

    public Shape calculateShape(LevelAccessor level, BlockPos pos) {
        boolean up    = isSameFarmland(level.getBlockState(pos.north()));
        boolean down  = isSameFarmland(level.getBlockState(pos.south()));
        boolean left  = isSameFarmland(level.getBlockState(pos.west()));
        boolean right = isSameFarmland(level.getBlockState(pos.east()));

        if (up && down && left && right) return Shape.CENTER;

        if (!up   && down && left && right) return Shape.TOP;
        if (up    && !down && left && right) return Shape.BOTTOM;
        if (up    && down && !left && right) return Shape.LEFT;
        if (up    && down && left && !right) return Shape.RIGHT;

        if (left && right && !up && !down) return Shape.HORIZONTAL_MID;
        if (up && down && !left && !right) return Shape.VERTICAL_MID;

        if (up && right && !down && !left)   return Shape.BOTTOM_LEFT;
        if (up && left  && !down && !right)  return Shape.BOTTOM_RIGHT;
        if (down && right && !up && !left)   return Shape.TOP_LEFT;
        if (down && left  && !up && !right)  return Shape.TOP_RIGHT;

        if (right && !left && !up && !down)  return Shape.HORIZONTAL_LEFT;
        if (left && !right && !up && !down)  return Shape.HORIZONTAL_RIGHT;
        if (down && !up && !left && !right)  return Shape.VERTICAL_TOP;
        if (up && !down && !left && !right)  return Shape.VERTICAL_BOTTOM;

        return Shape.SINGLE;
    }

    public Shape calculateWetShape(LevelAccessor level, BlockPos pos) {
        boolean up    = isHydratedFarmland(level.getBlockState(pos.north()));
        boolean down  = isHydratedFarmland(level.getBlockState(pos.south()));
        boolean left  = isHydratedFarmland(level.getBlockState(pos.west()));
        boolean right = isHydratedFarmland(level.getBlockState(pos.east()));

        if (up && down && left && right) return Shape.CENTER;

        if (!up   && down && left && right) return Shape.TOP;
        if (up    && !down && left && right) return Shape.BOTTOM;
        if (up    && down && !left && right) return Shape.LEFT;
        if (up    && down && left && !right) return Shape.RIGHT;

        if (left && right && !up && !down) return Shape.HORIZONTAL_MID;
        if (up && down && !left && !right) return Shape.VERTICAL_MID;

        if (up && right && !down && !left)   return Shape.BOTTOM_LEFT;
        if (up && left  && !down && !right)  return Shape.BOTTOM_RIGHT;
        if (down && right && !up && !left)   return Shape.TOP_LEFT;
        if (down && left  && !up && !right)  return Shape.TOP_RIGHT;

        if (right && !left && !up && !down)  return Shape.HORIZONTAL_LEFT;
        if (left && !right && !up && !down)  return Shape.HORIZONTAL_RIGHT;
        if (down && !up && !left && !right)  return Shape.VERTICAL_TOP;
        if (up && !down && !left && !right)  return Shape.VERTICAL_BOTTOM;

        return Shape.SINGLE;
    }

    private boolean isSameFarmland(BlockState state) {
        return state.getBlock() instanceof BlockFarmland;
    }

    private boolean isHydratedFarmland(BlockState state) {
        return isSameFarmland(state) && state.getValue(HYDRATED);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) return;

        Season current = getCurrentSeason(level);
        BlockState newState = state.setValue(SEASON, current);

        if (!state.equals(newState)) {
            level.setBlockAndUpdate(pos, newState);
        }

        if (level.getBlockEntity(pos) instanceof FarmlandBlockEntity be) {
            be.onPlace();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof FarmlandBlockEntity fbe) FarmlandBlockEntity.serverTick(lvl, pos, st, fbe);
        };
    }

    public Season getCurrentSeason(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return StardewDateData.get(serverLevel).getSeason();
        }
        return Season.SPRING;
    }
}

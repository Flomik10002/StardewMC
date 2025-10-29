package dev.flomik.stardew.core.block.surface;

import dev.flomik.stardew.core.block.shape.Shape;
import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

public class BlockGrassSurface extends Block {

    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
    public static final EnumProperty<Season> SEASON = EnumProperty.create("season", Season.class);

    public BlockGrassSurface() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .strength(0.3F, 0.4F)
                .sound(SoundType.GRASS)
                .dynamicShape()
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, Shape.SINGLE)
                .setValue(SEASON, Season.SPRING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, SEASON);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        level.scheduleTick(pos, this, 20);

        if (level.isClientSide) return;

        Season currentSeason = getCurrentSeason(level);
        BlockState newState = state
                .setValue(SEASON, currentSeason)
                .setValue(SHAPE, calculateShape(level, pos));

        if (!state.equals(newState)) {
            level.setBlockAndUpdate(pos, newState);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state
                .setValue(SHAPE, calculateShape(level, pos))
                .setValue(SEASON, getCurrentSeason(level));
    }

    Shape calculateShape(LevelAccessor level, BlockPos pos) {
        boolean up    = isSameGrass(level, pos.north());
        boolean down  = isSameGrass(level, pos.south());
        boolean left  = isSameGrass(level, pos.west());
        boolean right = isSameGrass(level, pos.east());
        boolean upLeft    = isSameGrass(level, pos.north().west());
        boolean upRight   = isSameGrass(level, pos.north().east());
        boolean downLeft  = isSameGrass(level, pos.south().west());
        boolean downRight = isSameGrass(level, pos.south().east());

        if (up && down && left && right) {
            if (!upLeft)    return Shape.INNER_TOP_LEFT;
            if (!upRight)   return Shape.INNER_TOP_RIGHT;
            if (!downLeft)  return Shape.INNER_BOTTOM_LEFT;
            if (!downRight) return Shape.INNER_BOTTOM_RIGHT;
            return Shape.CENTER;
        }

        if (!up &&  down && left && right) return Shape.TOP;
        if ( up && !down && left && right) return Shape.BOTTOM;
        if ( up &&  down && !left && right) return Shape.LEFT;
        if ( up &&  down && left && !right) return Shape.RIGHT;

        if ( up &&  right && !down && !left) return Shape.BOTTOM_LEFT;
        if ( up &&  left  && !down && !right) return Shape.BOTTOM_RIGHT;
        if (!up &&  right &&  down && !left) return Shape.TOP_LEFT;
        if (!up &&  left  &&  down && !right) return Shape.TOP_RIGHT;

        return Shape.SINGLE;
    }

    private boolean isSameGrass(LevelAccessor level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof BlockGrassSurface || block instanceof BlockGrassFull;
    }

    public Season getCurrentSeason(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return StardewDateData.get(serverLevel).getSeason();
        }
        return Season.SPRING;
    }

    public static Shape calculateShapeStatic(LevelAccessor level, BlockPos pos) {
        BlockGrassSurface instance = new BlockGrassSurface();
        return instance.calculateShape(level, pos);
    }
}

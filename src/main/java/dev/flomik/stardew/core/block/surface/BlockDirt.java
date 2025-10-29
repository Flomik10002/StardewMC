package dev.flomik.stardew.core.block.surface;

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

public class BlockDirt extends Block {

    public static final EnumProperty<Season> SEASON = EnumProperty.create("season", Season.class);

    public BlockDirt() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(3.0f)
                .sound(SoundType.GRAVEL)
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SEASON, Season.SPRING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SEASON);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide) return;
        Season current = getCurrentSeason(level);
        level.setBlock(pos, state.setValue(SEASON, current), 2);
    }


    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SEASON, getCurrentSeason(level));
    }

    public Season getCurrentSeason(LevelAccessor level) {
        if (level instanceof ServerLevel serverLevel) {
            return StardewDateData.get(serverLevel).getSeason();
        }
        return Season.SPRING;
    }
}


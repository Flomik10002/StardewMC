package dev.flomik.stardew.core.registry.block.surface;

import dev.flomik.stardew.core.registry.block.shape.Shape;
import dev.flomik.stardew.core.crop.FertilizerType;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;
import dev.flomik.stardew.core.util.ShapeCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
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

    public BlockFarmland() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .dynamicShape()
                .strength(0.5F, 0.5F)
                .sound(SoundType.GRAVEL)
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HYDRATED, false)
                .setValue(FERTILIZER, FertilizerType.NONE)
                .setValue(SHAPE, Shape.SINGLE));
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Shape dry = ShapeCalculator.calculateFarmlandShape(context.getLevel(), context.getClickedPos(), false);
        Shape wet = ShapeCalculator.calculateFarmlandShape(context.getLevel(), context.getClickedPos(), true);

        return this.defaultBlockState()
                .setValue(SHAPE, dry)
                .setValue(WET_SHAPE, wet);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state
                .setValue(SHAPE, ShapeCalculator.calculateFarmlandShape(level, pos, false))
                .setValue(WET_SHAPE, ShapeCalculator.calculateFarmlandShape(level, pos, true));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) return;

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
}
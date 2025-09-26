package dev.flomik.stardew.core.registry.block;

import dev.flomik.stardew.core.crop.FertilizerType;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
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

    public BlockFarmland() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .dynamicShape()
                .strength(0.5F, 0.5F)
                .sound(SoundType.GRAVEL)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(HYDRATED, false).setValue(FERTILIZER, FertilizerType.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HYDRATED, FERTILIZER);
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof FarmlandBlockEntity be) {
            be.onPlace();
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (level.getBlockEntity(pos) instanceof FarmlandBlockEntity be) {
            be.tickServer();
        }
    }
}

package dev.flomik.stardew.common.module.nature.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class BlockDirt extends Block {

    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 13);

    public BlockDirt(Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        RandomSource random = context.getLevel().getRandom();
        float chance = random.nextFloat();
        int variant;

        if (chance < 0.45f) {
            variant = 0;
        } else if (chance < 0.90f) {
            variant = 1;
        } else {
            variant = 2 + random.nextInt(12);
        }

        return this.defaultBlockState().setValue(VARIANT, variant);
    }
}
package dev.flomik.stardew.common.module.craftables.block;

import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityBigChest;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityBigStoneChest;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityChest;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockBigStoneChest extends BlockBigChest {
    public BlockBigStoneChest(Properties p) {
        super(p);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityBigStoneChest(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, ModBlocks.BIG_STONE_CHEST.getTypeValue(), BlockEntityChest::lidAnimateTick) : null;
    }
}


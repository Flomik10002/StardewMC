package dev.flomik.stardew.common.module.machinery.block;

import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityChest;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityStoneChest;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockStoneChest extends BlockChest {
    public BlockStoneChest(Properties p) {
        super(p);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityStoneChest(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, ModBlocks.STONE_CHEST.getTypeValue(), BlockEntityChest::lidAnimateTick) : null;
    }
}


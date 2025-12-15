package dev.flomik.stardew.common.module.machinery.block;

import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityBigStoneChest;
import dev.flomik.stardew.common.module.machinery.blockentity.BlockEntityChest;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockBigStoneChest extends BlockBigChest {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15); // Большой каменный сундук: высота 16
    
    public BlockBigStoneChest(Properties p) {
        super(p);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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


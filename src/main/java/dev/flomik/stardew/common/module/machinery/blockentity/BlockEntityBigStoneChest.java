package dev.flomik.stardew.common.module.machinery.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBigStoneChest extends BlockEntityBigChest {
    public BlockEntityBigStoneChest(BlockPos pos, BlockState state) {
        super(ModBlocks.BIG_STONE_CHEST.getTypeValue(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.stardew.big_stone_chest");
    }
}


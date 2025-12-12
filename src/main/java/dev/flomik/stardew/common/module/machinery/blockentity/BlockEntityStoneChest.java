package dev.flomik.stardew.common.module.machinery.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityStoneChest extends BlockEntityChest {
    public BlockEntityStoneChest(BlockPos pos, BlockState state) {
        super(ModBlocks.STONE_CHEST.getTypeValue(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.stardew.stone_chest");
    }
}


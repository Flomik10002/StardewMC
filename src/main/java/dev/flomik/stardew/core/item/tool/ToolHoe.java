package dev.flomik.stardew.core.item.tool;

import dev.flomik.stardew.core.registry.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ToolHoe extends Item {

    private final Pattern pattern;

    public ToolHoe(Properties properties, Pattern pattern) {
        super(properties);
        this.pattern = pattern;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        Player player = context.getPlayer();

        BlockState targetState = level.getBlockState(clicked);
        if (!targetState.is(ModBlocks.DIRT.get())) return InteractionResult.PASS;

        for (BlockPos pos : pattern.getAffectedPositions(level, clicked, context.getHorizontalDirection(), player)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.DIRT.get())) {
                level.setBlock(pos, ModBlocks.FARMLAND.get().defaultBlockState(), 3);
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        return InteractionResult.SUCCESS;
    }
}

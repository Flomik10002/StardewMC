package dev.flomik.stardew.core.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

@FunctionalInterface
public interface HoePattern {
    List<BlockPos> getAffectedPositions(Level level, BlockPos origin, Direction facing, Player player);
}
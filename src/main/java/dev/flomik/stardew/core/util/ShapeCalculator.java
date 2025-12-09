package dev.flomik.stardew.core.util;

import dev.flomik.stardew.core.registry.block.shape.Shape;
import dev.flomik.stardew.core.registry.block.surface.BlockFarmland;
import dev.flomik.stardew.core.registry.block.surface.BlockGrassFull;
import dev.flomik.stardew.core.registry.block.surface.BlockGrassSurface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class ShapeCalculator {

    public static Shape calculateGrassShape(BlockAndTintGetter level, BlockPos pos) {
        boolean up    = isSameGrass(level, pos.north());
        boolean down  = isSameGrass(level, pos.south());
        boolean left  = isSameGrass(level, pos.west());
        boolean right = isSameGrass(level, pos.east());

        if (up && down && left && right) {
            boolean upLeft    = isSameGrass(level, pos.north().west());
            boolean upRight   = isSameGrass(level, pos.north().east());
            boolean downLeft  = isSameGrass(level, pos.south().west());
            boolean downRight = isSameGrass(level, pos.south().east());

            if (!upLeft)    return Shape.INNER_TOP_LEFT;
            if (!upRight)   return Shape.INNER_TOP_RIGHT;
            if (!downLeft)  return Shape.INNER_BOTTOM_LEFT;
            if (!downRight) return Shape.INNER_BOTTOM_RIGHT;
            return Shape.CENTER;
        }

        if (!up &&  down && left && right) return Shape.TOP;
        if ( up && !down && left && right) return Shape.BOTTOM;
        if ( up &&  down && !left && right) return Shape.LEFT;
        if ( up &&  down && left && !right) return Shape.RIGHT;

        if ( up &&  right && !down && !left) return Shape.BOTTOM_LEFT;
        if ( up &&  left  && !down && !right) return Shape.BOTTOM_RIGHT;
        if (!up &&  right &&  down && !left) return Shape.TOP_LEFT;
        if (!up &&  left  &&  down && !right) return Shape.TOP_RIGHT;

        return Shape.SINGLE;
    }

    public static Shape calculateFarmlandShape(BlockAndTintGetter level, BlockPos pos, boolean requireWet) {
        boolean up    = checkFarmland(level, pos.north(), requireWet);
        boolean down  = checkFarmland(level, pos.south(), requireWet);
        boolean left  = checkFarmland(level, pos.west(), requireWet);
        boolean right = checkFarmland(level, pos.east(), requireWet);

        if (up && down && left && right) return Shape.CENTER;

        if (!up   && down && left && right) return Shape.TOP;
        if (up    && !down && left && right) return Shape.BOTTOM;
        if (up    && down && !left && right) return Shape.LEFT;
        if (up    && down && left && !right) return Shape.RIGHT;

        if (left && right && !up && !down) return Shape.HORIZONTAL_MID;
        if (up && down && !left && !right) return Shape.VERTICAL_MID;

        if (up && right && !down && !left)   return Shape.BOTTOM_LEFT;
        if (up && left  && !down && !right)  return Shape.BOTTOM_RIGHT;
        if (down && right && !up && !left)   return Shape.TOP_LEFT;
        if (down && left  && !up && !right)  return Shape.TOP_RIGHT;

        if (right && !left && !up && !down)  return Shape.HORIZONTAL_LEFT;
        if (left && !right && !up && !down)  return Shape.HORIZONTAL_RIGHT;
        if (down && !up && !left && !right)  return Shape.VERTICAL_TOP;
        if (up && !down && !left && !right)  return Shape.VERTICAL_BOTTOM;

        return Shape.SINGLE;
    }

    private static boolean isSameGrass(BlockAndTintGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BlockGrassSurface || state.getBlock() instanceof BlockGrassFull;
    }

    private static boolean checkFarmland(BlockAndTintGetter level, BlockPos pos, boolean requireWet) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockFarmland)) return false;
        if (requireWet) return state.getValue(BlockFarmland.HYDRATED);
        return true;
    }
}
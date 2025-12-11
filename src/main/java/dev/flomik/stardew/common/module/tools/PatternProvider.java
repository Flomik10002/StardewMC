package dev.flomik.stardew.common.module.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides common patterns for tool area-of-effect.
 */
public class PatternProvider {

    /**
     * Single block - только тот блок, на который кликнул игрок.
     */
    public static final Pattern SINGLE = (level, origin, facing, player) -> List.of(origin);

    /**
     * Three blocks in a line - 3 блока в линию по направлению взгляда игрока.
     * Блок, на который кликнул игрок, является точкой отсчета.
     */
    public static final Pattern THREE = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(origin);
        positions.add(origin.relative(facing));
        positions.add(origin.relative(facing, 2));
        return positions;
    };

    /**
     * Five blocks in a line - 5 блоков в линию по направлению взгляда игрока.
     * Блок, на который кликнул игрок, является точкой отсчета.
     */
    public static final Pattern FIVE = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            positions.add(origin.relative(facing, i));
        }
        return positions;
    };

    /**
     * 3x3 grid - квадрат 3x3, выровненный по направлению взгляда игрока.
     * origin находится в центре ближнего к игроку ряда.
     * 000
     * 000
     * 010 (1 = origin)
     */
    public static final Pattern GRID_3X3 = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        Direction horizontal = facing;
        if (!horizontal.getAxis().isHorizontal()) {
            horizontal = player.getDirection();
        }
        Direction perpendicular = horizontal.getClockWise();

        for (int depth = 0; depth < 3; depth++) {
            for (int width = -1; width <= 1; width++) {
                BlockPos pos = origin.relative(horizontal, depth).relative(perpendicular, width);
                positions.add(pos);
            }
        }

        return positions;
    };

    /**
     * 6x3 grid - прямоугольник 6 (вглубь) x 3 (ширина).
     * origin находится в центре ближнего к игроку ряда.
     * 000/000/000/000/000/010 (1 = origin)
     */
    public static final Pattern GRID_6X3 = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        Direction horizontal = facing;
        if (!horizontal.getAxis().isHorizontal()) {
            horizontal = player.getDirection();
        }
        Direction perpendicular = horizontal.getClockWise();

        // depth = 0..5
        // width = -1..1
        for (int depth = 0; depth < 6; depth++) {
            for (int width = -1; width <= 1; width++) {
                BlockPos pos = origin.relative(horizontal, depth).relative(perpendicular, width);
                positions.add(pos);
            }
        }

        return positions;
    };

    /**
     * 5x5 grid - квадрат 5x5, origin находится в центре ближнего к игроку ряда.
     */

    public static final Pattern GRID_5X5 = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        Direction horizontal = facing;
        if (!horizontal.getAxis().isHorizontal()) {
            horizontal = player.getDirection();
        }
        Direction perpendicular = horizontal.getClockWise();

        for (int depth = 0; depth < 5; depth++) {
            for (int width = -2; width <= 2; width++) {
                BlockPos pos = origin.relative(horizontal, depth).relative(perpendicular, width);
                positions.add(pos);
            }
        }

        return positions;
    };
}


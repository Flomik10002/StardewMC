package dev.flomik.stardew.core.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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
     * 3x3 grid - квадрат 3x3, центр - блок на который кликнул игрок.
     */
    public static final Pattern GRID_3X3 = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        Direction horizontal = getHorizontalDirection(facing);
        Direction perpendicular = horizontal.getClockWise();

        // 3x3 сетка с origin в центре
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos pos = origin.relative(horizontal, x).relative(perpendicular, z);
                positions.add(pos);
            }
        }

        return positions;
    };

    /**
     * 5x5 grid - квадрат 5x5, центр - блок на который кликнул игрок.
     */
    public static final Pattern GRID_5X5 = (level, origin, facing, player) -> {
        List<BlockPos> positions = new ArrayList<>();
        Direction horizontal = getHorizontalDirection(facing);
        Direction perpendicular = horizontal.getClockWise();

        // 5x5 сетка с origin в центре
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = origin.relative(horizontal, x).relative(perpendicular, z);
                positions.add(pos);
            }
        }

        return positions;
    };

    /**
     * Получает горизонтальное направление из любого направления.
     * Если направление UP или DOWN, используется NORTH по умолчанию.
     */
    private static Direction getHorizontalDirection(Direction direction) {
        if (direction.getAxis().isHorizontal()) {
            return direction;
        }
        // Для UP/DOWN используем NORTH как базовое направление
        return Direction.NORTH;
    }
}


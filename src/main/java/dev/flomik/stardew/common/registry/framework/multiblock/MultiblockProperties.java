package dev.flomik.stardew.common.registry.framework.multiblock;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Общие blockstate-свойства для всех мультиблоков: относительная позиция клетки
 * (0,0) = исток (origin), вплоть до (4,4) — верхняя граница поддерживаемого
 * размера 5x5 (см. {@link MultiblockBlock#MAX_SIZE}).
 *
 * Свойства общие (переиспользуются всеми классами мультиблоков), как и
 * BlockStateProperties.FACING в ванили.
 */
public final class MultiblockProperties {

    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, MultiblockBlock.MAX_SIZE - 1);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, MultiblockBlock.MAX_SIZE - 1);

    private MultiblockProperties() {
    }
}

package dev.flomik.stardew.common.registry.framework.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * BlockItem мультиблока: при установке дополнительно проверяет и заполняет
 * все остальные клетки прямоугольника (не только ту, по которой кликнули).
 * Вся остальная ванильная логика (звук, shrink стака, NBT и т.д.) остаётся
 * стандартной — переопределены только проверка места и сама установка.
 */
public class MultiblockBlockItem extends BlockItem {

    private final MultiblockBlock multiblock;

    public MultiblockBlockItem(MultiblockBlock block, Properties properties) {
        super(block, properties);
        this.multiblock = block;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        if (!super.canPlace(context, state)) return false;

        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        CollisionContext collision = context.getPlayer() == null
                ? CollisionContext.empty() : CollisionContext.of(context.getPlayer());

        for (int dx = 0; dx < multiblock.getWidth(); dx++) {
            for (int dz = 0; dz < multiblock.getHeight(); dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos pos = origin.offset(dx, 0, dz);
                BlockState partState = partState(dx, dz);

                if (!level.getBlockState(pos).canBeReplaced(context)
                        || !level.isUnobstructed(partState, pos, collision)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!super.placeBlock(context, state)) return false;

        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();

        for (int dx = 0; dx < multiblock.getWidth(); dx++) {
            for (int dz = 0; dz < multiblock.getHeight(); dz++) {
                if (dx == 0 && dz == 0) continue;
                level.setBlock(origin.offset(dx, 0, dz), partState(dx, dz), 11);
            }
        }
        return true;
    }

    private BlockState partState(int dx, int dz) {
        return multiblock.defaultBlockState()
                .setValue(MultiblockProperties.PART_X, dx)
                .setValue(MultiblockProperties.PART_Y, dz);
    }
}

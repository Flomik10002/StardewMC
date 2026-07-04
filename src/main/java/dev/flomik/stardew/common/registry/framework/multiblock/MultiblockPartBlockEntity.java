package dev.flomik.stardew.common.registry.framework.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Базовый BlockEntity клетки мультиблока. Каждая клетка получает свой
 * инстанс (так требует {@code BaseEntityBlock}), но реальное состояние
 * (ХП, прогресс и т.п.) должен хранить только "мозг" — BlockEntity клетки
 * (0,0). Остальные клетки через {@link #getOrigin(Class)} перенаправляют
 * туда любые запросы, самостоятельно ничего не храня.
 */
public abstract class MultiblockPartBlockEntity extends BlockEntity {

    protected MultiblockPartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected boolean isOrigin() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof MultiblockBlock mb && mb.isOrigin(state);
    }

    protected BlockPos getOriginPos() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof MultiblockBlock mb) {
            return mb.getOriginPos(worldPosition, state);
        }
        return worldPosition;
    }

    /**
     * Возвращает BlockEntity "мозга" (клетки 0,0): себя, если мы и есть исток,
     * иначе — реальный инстанс по адресу истока. При отсутствии/несовпадении
     * типа безопасно возвращает себя же (не должно происходить в норме).
     */
    @SuppressWarnings("unchecked")
    protected <T extends MultiblockPartBlockEntity> T getOrigin(Class<T> type) {
        if (isOrigin() && type.isInstance(this)) {
            return (T) this;
        }
        if (level != null && level.getBlockEntity(getOriginPos()) instanceof MultiblockPartBlockEntity be && type.isInstance(be)) {
            return (T) be;
        }
        return type.isInstance(this) ? (T) this : null;
    }
}

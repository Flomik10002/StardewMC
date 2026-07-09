package dev.flomik.stardew.common.registry.framework.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Базовый класс для мультиблоков размером от 1x1 до 5x5 клеток. Каждая клетка —
 * настоящий, отдельно зарегистрированный блок в мире с обычным полноценным
 * хитбоксом, поэтому в отличие от подхода "один блок + увеличенная модель/шейп"
 * (как было у пугала/старого large_stump) тут в принципе не может быть проблем
 * с коллизией или установкой блоков в "нависающую" зону — там просто стоит
 * настоящий блок.
 *
 * <h2>Как сделать новый мультиблок (шпаргалка)</h2>
 * <ol>
 *     <li>Свой блок наследует {@code MultiblockBlock}, в конструкторе вызывает
 *     {@code super(properties, width, height)}.</li>
 *     <li>Регистрируется через обычный {@code BlockBuilder} — тот сам подставит
 *     {@link MultiblockBlockItem} вместо обычного {@code BlockItem}, если видит,
 *     что блок — мультиблок.</li>
 *     <li>Модель/blockstate: клетке (0,0) ("исток") даём настоящую модель,
 *     остальным клеткам — общую переиспользуемую пустую модель
 *     {@code stardew:block/empty} (см. {@code models/block/empty.json}) — то
 *     самое "зачастую один model json" для всех невидимых клеток разом.</li>
 *     <li>Для общего состояния (HP, прогресс и т.д.) — BlockEntity клетки
 *     наследует {@link MultiblockPartBlockEntity} и хранит его только в
 *     "мозге" (origin), см. {@link MultiblockPartBlockEntity#getOrigin(Class)}.</li>
 * </ol>
 *
 * Заполнение всегда идёт в сторону +X/+Z от места клика (без поворота по
 * стороне света) — этого достаточно для текущих задач и не усложняет то,
 * что не просили; при необходимости ротацию можно добавить, храня FACING
 * и разворачивая {@link #getOriginPos}.
 */
public abstract class MultiblockBlock extends BaseEntityBlock {

    public static final int MAX_SIZE = 5;

    private final int width;
    private final int height;

    protected MultiblockBlock(Properties properties, int width, int height) {
        super(properties);
        if (width < 1 || height < 1 || width > MAX_SIZE || height > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Multiblock size must be within 1x1.." + MAX_SIZE + "x" + MAX_SIZE + ", got " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        registerDefaultState(this.stateDefinition.any()
                .setValue(MultiblockProperties.PART_X, 0)
                .setValue(MultiblockProperties.PART_Y, 0));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MultiblockProperties.PART_X, MultiblockProperties.PART_Y);
    }

    /**
     * BaseEntityBlock по умолчанию рендерит INVISIBLE (расчёт на кастомный
     * BlockEntityRenderer) — мультиблокам нужен обычный блочный рендер по
     * модели из blockstate, поэтому переопределяем здесь один раз для всех.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public boolean isOrigin(BlockState state) {
        return state.getValue(MultiblockProperties.PART_X) == 0
                && state.getValue(MultiblockProperties.PART_Y) == 0;
    }

    /**
     * Абсолютная позиция клетки (0,0) для данной клетки мультиблока.
     */
    public BlockPos getOriginPos(BlockPos pos, BlockState state) {
        return pos.offset(-state.getValue(MultiblockProperties.PART_X), 0, -state.getValue(MultiblockProperties.PART_Y));
    }

    /**
     * Ломает всю структуру целиком, если сломана хоть одна клетка. Не важно,
     * какую именно клетку сломали — через origin сносятся все остальные.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos origin = getOriginPos(pos, state);

            if (pos.equals(origin)) {
                for (int dx = 0; dx < width; dx++) {
                    for (int dz = 0; dz < height; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        BlockPos other = origin.offset(dx, 0, dz);
                        if (level.getBlockState(other).is(this)) {
                            level.removeBlock(other, false);
                        }
                    }
                }
            } else if (level.getBlockState(origin).is(this)) {
                level.removeBlock(origin, false);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}

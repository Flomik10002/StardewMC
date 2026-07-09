package dev.flomik.stardew.common.module.nature.block;

import dev.flomik.stardew.common.module.nature.blockentity.LargeStumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import dev.flomik.stardew.common.registry.framework.multiblock.MultiblockBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Большой пень: настоящий мультиблок 2x2 (см. {@link MultiblockBlock}). Каждая
 * клетка — обычный блок с обычной коллизией, поэтому проблем с "нависающим"
 * хитбоксом (как было у старой версии) больше нет.
 *
 * Ломается только топором от медного и выше; подсчёт ударов и ХП —
 * в {@link LargeStumpBlockEntity} (хранится только у "мозга"-истока).
 */
public class BlockLargeStump extends MultiblockBlock {

    // Сколько тиков занимает один "удар" вне зависимости от инструмента —
    // урон за удар зависит от тира топора, а не от скорости накопления прогресса.
    private static final int TICKS_PER_HIT = 10;

    public BlockLargeStump(Properties properties) {
        super(properties, 2, 2);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.isCreative()) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        // Прогресс идёт для любого топора (даже недостаточного тира) — иначе
        // не получится отследить и оштрафовать "бесполезную" попытку рубки.
        if (!LargeStumpBlockEntity.isAxe(player.getMainHandItem())) {
            return 0.0f;
        }

        return 1.0f / TICKS_PER_HIT;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargeStumpBlockEntity(pos, state);
    }
}

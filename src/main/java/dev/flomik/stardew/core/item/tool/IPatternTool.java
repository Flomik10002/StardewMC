package dev.flomik.stardew.core.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Интерфейс для инструментов с паттернами и визуализацией.
 */
public interface IPatternTool {
    /**
     * Получает текущий тип паттерна из ItemStack.
     */
    PatternType getCurrentPattern(ItemStack stack);

    /**
     * Получает максимально доступный паттерн для инструмента.
     */
    PatternType getMaxPattern();

    /**
     * Проверяет, можно ли применить инструмент к данному блоку.
     * 
     * @param level Мир
     * @param pos Позиция блока
     * @param stack ItemStack инструмента
     * @return true если блок можно обработать, false если нет (будет красным)
     */
    boolean canApplyToBlock(Level level, BlockPos pos, ItemStack stack);
}


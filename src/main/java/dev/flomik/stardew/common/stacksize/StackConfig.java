package dev.flomik.stardew.common.stacksize;

/**
 * Конфигурация размера стаков для мода Stardew.
 */
public class StackConfig {
    
    /**
     * Максимальный размер стака
     */
    public static final int MAX_STACK_SIZE = 999;
    
    /**
     * Отслеживает максимальный размер стака из зарегистрированных предметов.
     * Начинаем с 64, так как предметы со стаком 64 не вызывают stacksTo().
     */
    public static int maxRegisteredItemStackSize = 64;
    
    /**
     * Возвращает максимальный размер стака.
     */
    public static int getMaxStackSize() {
        return Math.max(MAX_STACK_SIZE, maxRegisteredItemStackSize);
    }
}

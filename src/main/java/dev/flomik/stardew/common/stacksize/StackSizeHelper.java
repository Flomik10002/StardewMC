package dev.flomik.stardew.common.stacksize;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Утилиты для работы с размерами стаков.
 */
public class StackSizeHelper {
    
    /**
     * Масштабирует лимит слота на основе оригинального значения
     */
    public static void scaleSlotLimit(CallbackInfoReturnable<Integer> cir) {
        int scaled = scaleSlotLimit(cir.getReturnValue());
        cir.setReturnValue(scaled);
    }
    
    /**
     * Масштабирует лимит слота на основе оригинального лимита
     *
     * @param original Оригинальный размер стака
     * @return Масштабированный размер. Если оригинальный слот имеет лимит 1, возвращается 1.
     */
    public static int scaleSlotLimit(int original) {
        int newStackSize = StackConfig.getMaxStackSize();
        
        // Не масштабируем слоты, предназначенные для одного предмета
        if (original == 1)
            return 1;
        else if (newStackSize < 64)
            return 64;
        
        return Math.max(original, original * newStackSize / 64);
    }
    
    /**
     * Возвращает новый размер стака
     *
     * @return Новый размер стака с минимальным значением 64
     */
    public static int getNewStackSize() {
        return Math.max(StackConfig.getMaxStackSize(), 64);
    }
}

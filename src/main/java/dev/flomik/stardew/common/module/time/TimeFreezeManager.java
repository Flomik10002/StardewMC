package dev.flomik.stardew.common.module.time;

import net.minecraft.server.level.ServerLevel;

/**
 * Менеджер заморозки времени.
 * Когда время заморожено - dayTime не увеличивается, но игра продолжает тикать.
 * Это позволяет анимациям работать, но время дня не меняется.
 */
public class TimeFreezeManager {

    // Глобальный флаг заморозки времени (для сервера)
    private static boolean timeFrozen = false;
    
    // Клиентский флаг (для UI)
    private static boolean clientTimeFrozen = false;
    
    // Количество активных "замораживателей" (для вложенных вызовов)
    private static int freezeCount = 0;

    /**
     * Замораживает время. Можно вызывать несколько раз -
     * время разморозится только когда все вызовы будут отменены.
     */
    public static void freeze() {
        freezeCount++;
        timeFrozen = true;
    }

    /**
     * Размораживает время. Уменьшает счетчик заморозки.
     * Время разморозится когда счетчик достигнет 0.
     */
    public static void unfreeze() {
        if (freezeCount > 0) {
            freezeCount--;
        }
        if (freezeCount == 0) {
            timeFrozen = false;
        }
    }

    /**
     * Принудительно размораживает время (сбрасывает счетчик).
     */
    public static void forceUnfreeze() {
        freezeCount = 0;
        timeFrozen = false;
    }

    /**
     * Проверяет, заморожено ли время.
     */
    public static boolean isTimeFrozen() {
        return timeFrozen;
    }

    /**
     * Замораживает время на клиенте (для UI индикации).
     */
    public static void freezeClient() {
        clientTimeFrozen = true;
    }

    /**
     * Размораживает время на клиенте.
     */
    public static void unfreezeClient() {
        clientTimeFrozen = false;
    }

    /**
     * Проверяет заморозку на клиенте.
     */
    public static boolean isClientTimeFrozen() {
        return clientTimeFrozen;
    }

    /**
     * Проверяет, должно ли время тикать для данного уровня.
     * Вызывается из миксина.
     */
    public static boolean shouldTickTime(ServerLevel level) {
        // Если время заморожено - не тикаем
        if (timeFrozen) {
            return false;
        }
        return true;
    }
}


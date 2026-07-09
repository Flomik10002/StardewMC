package dev.flomik.stardew.common.admin;

/**
 * Глобальный переключатель отладочного режима (включается командой
 * {@code /stardew debug}). Не персистентный, живёт только пока запущен
 * сервер — этого достаточно для целей отладки.
 */
public final class StardewDebug {

    private static volatile boolean enabled = false;

    private StardewDebug() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }
}

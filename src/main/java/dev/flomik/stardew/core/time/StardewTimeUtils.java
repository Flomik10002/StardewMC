package dev.flomik.stardew.core.time;

public class StardewTimeUtils {
    private static final int TICKS_PER_MINUTE = 17;
    private static final int MINUTES_IN_DAY = 1200; // 6:00 → 2:00
    private static final int TOTAL_STARDAY_TICKS = TICKS_PER_MINUTE * MINUTES_IN_DAY; // 20400

    public static String formatTicksToClock(long ticks) {
        long dayTime = ticks % 24000;
        int minutesSince6AM = (int) (dayTime / TICKS_PER_MINUTE);
        int hour = (minutesSince6AM / 60 + 6) % 24;
        int minute = minutesSince6AM % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    public static int getHour(long ticks) {
        long dayTime = ticks % 24000;
        int minutes = (int) (dayTime / TICKS_PER_MINUTE);
        return (minutes / 60 + 6) % 24;
    }

    public static int getMinute(long ticks) {
        long dayTime = ticks % 24000;
        int minutes = (int) (dayTime / TICKS_PER_MINUTE);
        return minutes % 60;
    }

    public static boolean isNight(long ticks) {
        int hour = getHour(ticks);
        return hour >= 19 || hour < 6;
    }

    public static boolean shouldPassOut(long ticks) {
        long dayTime = ticks % 24000;
        return dayTime >= TOTAL_STARDAY_TICKS;
    }

    public static int toTicks(int hour, int minute) {
        return ((hour - 6 + 24) % 24) * 60 * 17 + minute * 17;
    }
}

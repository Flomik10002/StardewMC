package dev.flomik.stardew.common.module.time;

public class StardewTimeUtils {
    // 1 Hour = 1000 Ticks
    // 6:00 AM = 0 Ticks
    // 2:00 AM = 20000 Ticks
    // Total Day (to 6:00 AM next day) = 24000 Ticks

    public static String formatTicksToClock(long ticks) {
        // Normalizing to 24h cycle
        long dayTime = ticks % 24000;
        
        // Hour: 0 ticks is 6:00 AM. 1000 ticks is 7:00 AM.
        int hour = (int) ((dayTime / 1000 + 6) % 24);
        
        // Minute: The remainder of the hour.
        // 1000 ticks = 60 minutes.
        // Formula: (ticks % 1000) * 60 / 1000
        int rawMinute = (int) ((dayTime % 1000) * 60 / 1000);
        
        // Visual Rounding: Round to nearest 10 minutes to avoid jitter
        int displayMinute = (rawMinute / 10) * 10;
        
        return String.format("%02d:%02d", hour, displayMinute);
    }

    public static int getHour(long ticks) {
        long dayTime = ticks % 24000;
        return (int) ((dayTime / 1000 + 6) % 24);
    }

    public static int getMinute(long ticks) {
        long dayTime = ticks % 24000;
        return (int) ((dayTime % 1000) * 60 / 1000);
    }

    public static boolean isNight(long ticks) {
        int hour = getHour(ticks);
        // Stardew night starts visually around 6PM (18:00) or 7PM (19:00)
        return hour >= 19 || hour < 6;
    }

    public static boolean shouldPassOut(long ticks) {
        // 2:00 AM is 20 hours after 6:00 AM
        // 20 hours * 1000 ticks = 20000 ticks
        long dayTime = ticks % 24000;
        // Check if we are past 20000 ticks but before the new day logic resets
        // Effectively, 2:00 AM is the limit.
        return dayTime >= 20000;
    }

    public static int toTicks(int hour, int minute) {
        // Inverse logic:
        // Hour offset from 6:00 AM
        int hourOffset = (hour - 6 + 24) % 24;
        
        // 1000 ticks per hour
        // 1000 ticks / 60 minutes = 16.666 ticks per minute
        return (int) (hourOffset * 1000 + (minute / 60.0 * 1000));
    }
}

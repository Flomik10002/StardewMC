package dev.flomik.stardew.core.time;

import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class ScheduleManager {
    private static final List<ScheduleEntry> entries = new ArrayList<>();
    private static int lastHour = -1;
    private static int lastMinute = -1;

    public static void register(int hour, int minute, Runnable action) {
        entries.add(new ScheduleEntry(hour, minute, action));
    }

    public static void clearAll() {
        entries.clear();
    }

    public static void tick(long ticks) {
        int hour = StardewTimeUtils.getHour(ticks);
        int minute = StardewTimeUtils.getMinute(ticks);
        int ticksToday = (int)(ticks % 24000);

        if (hour == 6 && minute == 0 && (lastHour != 6 || lastMinute != 0)) {
            for (ScheduleEntry entry : entries) {
                entry.reset();
            }
        }

        for (ScheduleEntry entry : entries) {
            if (entry.shouldTrigger(ticksToday)) {
                entry.trigger();
            }
        }

        lastHour = hour;
        lastMinute = minute;
    }

    public static void forceNextDay(ServerLevel level) {
        StardewDateData date = StardewDateData.get(level);
        date.advance();
        date.setDirty();
        
        level.setDayTime(StardewTimeUtils.toTicks(6, 0));
        
        for (ScheduleEntry entry : entries) {
            if (entry.getStartTick() == StardewTimeUtils.toTicks(6, 0)) {
                entry.trigger();
            }
        }
    }
}

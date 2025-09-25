package dev.flomik.stardew.core.calendar;

import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewTimeUtils;

public class CalendarEvent {

    public final Season season;
    public final int day;
    public final String name;
    public final Runnable action;

    private final boolean allDay;
    private final int startTick;
    private final int endTick;

    public CalendarEvent(Season season, int day, String name, Runnable action) {
        this(season, day, name, action, true, 0, 0);
    }

    public CalendarEvent(Season season, int day, String name, Runnable action, int startHour, int startMinute, int endHour, int endMinute) {
        this(season, day, name, action, false,
                StardewTimeUtils.toTicks(startHour, startMinute),
                StardewTimeUtils.toTicks(endHour, endMinute));
    }

    private CalendarEvent(Season season, int day, String name, Runnable action, boolean allDay, int startTick, int endTick) {
        this.season = season;
        this.day = day;
        this.name = name;
        this.action = action;
        this.allDay = allDay;
        this.startTick = startTick;
        this.endTick = endTick;
    }

    public boolean matches(Season currentSeason, int currentDay) {
        return this.season == currentSeason && this.day == currentDay;
    }

    public boolean isActiveNow(long ticksToday) {
        if (allDay) return true;
        return ticksToday >= startTick && ticksToday <= endTick;
    }
}

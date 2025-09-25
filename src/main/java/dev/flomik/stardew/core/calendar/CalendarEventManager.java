package dev.flomik.stardew.core.calendar;

import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;

import java.util.ArrayList;
import java.util.List;

public class CalendarEventManager {
    private static final List<CalendarEvent> events = new ArrayList<>();

    public static void register(Season season, int day, String name, Runnable action) {
        events.add(new CalendarEvent(season, day, name, action));
    }

    public static void onNewDay(StardewDateData date) {
        for (CalendarEvent event : events) {
            if (event.matches(date.getSeason(), date.getDay())) {
                event.action.run();
            }
        }
    }

    public static List<CalendarEvent> getAll() {
        return events;
    }
}

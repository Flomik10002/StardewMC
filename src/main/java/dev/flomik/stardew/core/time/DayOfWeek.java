package dev.flomik.stardew.core.time;

public enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

    public static DayOfWeek fromIndex(int i) {
        return values()[i % 7];
    }
}

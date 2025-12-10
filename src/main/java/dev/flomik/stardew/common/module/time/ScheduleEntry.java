package dev.flomik.stardew.common.module.time;

public class ScheduleEntry {
    private final int startTick;
    private final int endTick;
    private final Runnable action;
    private boolean triggered = false;

    public ScheduleEntry(int hour, int minute, Runnable action) {
        this.startTick = StardewTimeUtils.toTicks(hour, minute);
        this.endTick   = this.startTick;
        this.action    = action;
    }

    public ScheduleEntry(int startHour, int startMinute, int endHour, int endMinute, Runnable action) {
        this.startTick = StardewTimeUtils.toTicks(startHour, startMinute);
        this.endTick   = StardewTimeUtils.toTicks(endHour, endMinute);
        this.action    = action;
    }

    public boolean shouldTrigger(int ticksToday) {
        return !triggered && ticksToday >= startTick && ticksToday <= endTick;
    }

    public void trigger() {
        action.run();
        triggered = true;
    }

    public void reset() {
        triggered = false;
    }

    public boolean isRange() {
        return startTick != endTick;
    }

    public int getStartTick() {
        return startTick;
    }

    public int getEndTick() {
        return endTick;
    }
}

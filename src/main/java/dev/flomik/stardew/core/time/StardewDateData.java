package dev.flomik.stardew.core.time;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StardewDateData extends SavedData {

    private static final String NAME = "stardew_date";

    private Season season = Season.SPRING;
    private int day = 1;
    private int totalDays = 0;

    public Season getSeason() {
        return season;
    }

    public int getDay() {
        return day;
    }

    public void setSeason(Season newSeason) {
        season = newSeason;
        syncTotalDaysFromDate();
        setDirty();
    }

    public void setDay(int newDay) {
        day = newDay;
        syncTotalDaysFromDate();
        setDirty();
    }

    public void setDate(Season newSeason, int newDay) {
        this.season = newSeason;
        this.day = newDay;
        syncTotalDaysFromDate();
        setDirty();
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void syncTotalDaysFromDate() {
        this.totalDays = season.ordinal() * 28 + (day - 1);
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.fromIndex(totalDays);
    }

    public void advance() {
        totalDays++;
        day++;

        if (day > 28) {
            day = 1;
            season = season.next();
        }

        setDirty();
    }

    public static StardewDateData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                StardewDateData::load,
                StardewDateData::new,
                NAME
        );
    }

    public static StardewDateData load(CompoundTag tag) {
        StardewDateData data = new StardewDateData();
        data.season = Season.valueOf(tag.getString("season"));
        data.day = tag.getInt("day");
        data.totalDays = tag.getInt("totalDays");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("season", season.name());
        tag.putInt("day", day);
        tag.putInt("totalDays", totalDays);
        return tag;
    }
}

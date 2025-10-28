package dev.flomik.stardew.core.time;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StardewDateData extends SavedData {

    private static final String NAME = "stardew_date";

    private Season season = Season.SPRING;
    private int day = 1;
    private int totalDays = 0;

    private Weather todayWeather = Weather.SUNNY;
    private Weather tomorrowWeather = Weather.SUNNY;
    private float dailyLuck = 0f;
    private String festivalToday = null;
    private String festivalTomorrow = null;
    private boolean weatherInitialized = false; // флаг инициализации погоды

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

        // promote tomorrow -> today state
        this.todayWeather = this.tomorrowWeather;
        this.festivalToday = this.festivalTomorrow;
        // dailyLuck will be generated for the new day externally by WeatherSystem

        setDirty();
    }

    public static StardewDateData get(ServerLevel level) {
        StardewDateData data = level.getDataStorage().computeIfAbsent(
                StardewDateData::load,
                StardewDateData::new,
                NAME
        );
        
        // Инициализируем погоду при первом создании данных
        if (!data.weatherInitialized) {
            WeatherSystem.initializeWeather(level, data);  // Передаём data как параметр, чтобы избежать рекурсии
            data.weatherInitialized = true;
            data.setDirty();
        }
        
        return data;
    }

    public static StardewDateData load(CompoundTag tag) {
        StardewDateData data = new StardewDateData();
        data.season = Season.valueOf(tag.getString("season"));
        data.day = tag.getInt("day");
        data.totalDays = tag.getInt("totalDays");
        if (tag.contains("todayWeather")) data.todayWeather = Weather.valueOf(tag.getString("todayWeather"));
        if (tag.contains("tomorrowWeather")) data.tomorrowWeather = Weather.valueOf(tag.getString("tomorrowWeather"));
        data.dailyLuck = tag.getFloat("dailyLuck");
        if (tag.contains("festivalToday")) data.festivalToday = tag.getString("festivalToday");
        if (tag.contains("festivalTomorrow")) data.festivalTomorrow = tag.getString("festivalTomorrow");
        if (tag.contains("weatherInitialized")) data.weatherInitialized = tag.getBoolean("weatherInitialized");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("season", season.name());
        tag.putInt("day", day);
        tag.putInt("totalDays", totalDays);
        tag.putString("todayWeather", todayWeather.name());
        tag.putString("tomorrowWeather", tomorrowWeather.name());
        tag.putFloat("dailyLuck", dailyLuck);
        if (festivalToday != null) tag.putString("festivalToday", festivalToday);
        if (festivalTomorrow != null) tag.putString("festivalTomorrow", festivalTomorrow);
        tag.putBoolean("weatherInitialized", weatherInitialized);
        return tag;
    }

    public Weather getTodayWeather() { return todayWeather; }
    public Weather getTomorrowWeather() { return tomorrowWeather; }
    public void setTomorrowWeather(Weather w) { this.tomorrowWeather = w; setDirty(); }
    public void setTodayWeather(Weather w) { this.todayWeather = w; setDirty(); }
    public float getDailyLuck() { return dailyLuck; }
    public void setDailyLuck(float v) { this.dailyLuck = v; setDirty(); }
    public String getFestivalToday() { return festivalToday; }
    public String getFestivalTomorrow() { return festivalTomorrow; }
    public void setFestivalTomorrow(String id) { this.festivalTomorrow = id; setDirty(); }
}

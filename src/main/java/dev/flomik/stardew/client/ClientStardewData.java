package dev.flomik.stardew.client;

import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.Weather;

public class ClientStardewData {
    private static float currentEnergy = 270.0f;
    private static float maxEnergy = 270.0f;
    private static boolean isExhausted = false;
    private static long money = 0;

    private static Season season = Season.SPRING;
    private static Weather weather = Weather.SUNNY;
    private static int day = 1;
    private static int shakeTimer = 0;

    public static void updateWorldData(Season s, Weather w, int d) {
        season = s;
        weather = w;
        day = d;
    }

    public static Season getSeason() { return season; }
    public static Weather getWeather() { return weather; }
    public static int getDay() { return day; }

    public static void update(float current, float max, boolean exhausted, long new_money) {
        // Если энергия уменьшилась и стала ниже 20, запускаем тряску
        if (current < currentEnergy && current < 20) {
            shakeTimer = 40;
        }
        
        currentEnergy = current;
        maxEnergy = max;
        isExhausted = exhausted;
        money = new_money;
    }
    
    public static void tick() {
        if (shakeTimer > 0) shakeTimer--;
    }

    public static float getCurrentEnergy() { return currentEnergy; }
    public static float getMaxEnergy() { return maxEnergy; }
    public static boolean isExhausted() { return isExhausted; }
    public static long getMoney() { return money; }
    public static int getShakeTimer() { return shakeTimer; }
}

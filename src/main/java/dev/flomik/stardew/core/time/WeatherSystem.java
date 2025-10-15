package dev.flomik.stardew.core.time;

import net.minecraft.server.level.ServerLevel;

import java.util.Random;

public final class WeatherSystem {

    public static void generateTomorrow(ServerLevel level) {
        StardewDateData data = StardewDateData.get(level);
        Weather w = pickWeatherForTomorrow(level, data);
        data.setTomorrowWeather(w);
        data.setDailyLuck(pickDailyLuck(level, data));
    }

    private static Weather pickWeatherForTomorrow(ServerLevel level, StardewDateData data) {
        // Deterministic PRNG using world seed and next day index
        long seed = level.getSeed() ^ (data.getTotalDays() + 1L) * 0x9E3779B97F4A7C15L;
        Random rng = new Random(seed);

        // Festival override (stub: if festivalTomorrow set -> FEST_OVERRIDE)
        if (data.getFestivalTomorrow() != null) return Weather.FEST_OVERRIDE;

        Season season = data.getSeason();
        switch (season) {
            case WINTER:
                return Weather.SNOW; // SV: зимой снег по умолчанию
            case SPRING: {
                // rough SV-like chances: Rain 20%, Wind 15%, Storm rare 2% (mostly summer), else Sunny
                float r = rng.nextFloat();
                if (r < 0.20f) return Weather.RAIN;
                if (r < 0.35f) return Weather.WIND;
                return Weather.SUNNY;
            }
            case SUMMER: {
                // Summer: more rain, chance of storm
                float r = rng.nextFloat();
                if (r < 0.30f) {
                    // sub-roll for storm
                    return rng.nextFloat() < 0.30f ? Weather.STORM : Weather.RAIN;
                }
                return Weather.SUNNY;
            }
            case FALL: {
                float r = rng.nextFloat();
                if (r < 0.25f) return Weather.RAIN;
                if (r < 0.45f) return Weather.WIND;
                if (r < 0.50f) return Weather.STORM; // occasional storms
                return Weather.SUNNY;
            }
            default:
                return Weather.SUNNY;
        }
    }

    private static float pickDailyLuck(ServerLevel level, StardewDateData data) {
        long seed = (level.getSeed() * 31L) ^ (data.getTotalDays() + 1L) * 0xC2B2AE3D27D4EB4FL;
        Random rng = new Random(seed);
        // SV luck is centered near 0, range approx [-0.1, +0.1]; we use uniform for now
        return (rng.nextFloat() - 0.5f) * 0.2f;
    }
}



package dev.flomik.stardew.core.time;

import dev.flomik.stardew.StardewMod;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

public final class WeatherSystem {

    /**
     * Инициализирует погоду при первом входе в мир.
     * Генерирует сегодняшнюю и завтрашнюю погоду.
     */
    public static void initializeWeather(ServerLevel level, StardewDateData data) {
        // Генерируем погоду на сегодня (используя текущий день)
        Weather today = pickWeatherForDay(level, data, data.getTotalDays());
        data.setTodayWeather(today);
        
        // Генерируем погоду на завтра
        Weather tomorrow = pickWeatherForDay(level, data, data.getTotalDays() + 1);
        data.setTomorrowWeather(tomorrow);
        
        // Генерируем удачу на сегодня
        data.setDailyLuck(pickDailyLuckForDay(level, data, data.getTotalDays()));
        
        StardewMod.LOGGER.info("[WeatherSystem] Initialized weather - Today: {}, Tomorrow: {}", today, tomorrow);
    }

    /**
     * Генерирует погоду на завтра (вызывается в конце текущего дня).
     */
    public static void generateTomorrow(ServerLevel level) {
        StardewDateData data = StardewDateData.get(level);
        Weather w = pickWeatherForDay(level, data, data.getTotalDays() + 1);
        data.setTomorrowWeather(w);
        data.setDailyLuck(pickDailyLuckForDay(level, data, data.getTotalDays() + 1));
        
        StardewMod.LOGGER.info("[WeatherSystem] Generated tomorrow weather: {}", w);
    }
    
    /**
     * Применяет погоду к миру Minecraft.
     */
    public static void applyWeatherToWorld(ServerLevel level, Weather weather) {
        switch (weather) {
            case RAIN:
            case STORM:
                level.setWeatherParameters(0, 24000, true, weather == Weather.STORM);
                StardewMod.LOGGER.info("[WeatherSystem] Applied weather {} - Rain/Thunder enabled", weather);
                break;
            case SUNNY:
            case WIND:
            case FEST_OVERRIDE:
            case WEDDING_OVERRIDE:
            default:
                level.setWeatherParameters(24000, 0, false, false);
                StardewMod.LOGGER.info("[WeatherSystem] Applied weather {} - Clear skies", weather);
                break;
        }
    }

    /**
     * Генерирует погоду для конкретного дня детерминированно.
     * @param dayIndex - индекс дня (0 = первый день, 1 = второй и т.д.)
     */
    private static Weather pickWeatherForDay(ServerLevel level, StardewDateData data, int dayIndex) {
        // Детерминированная генерация на основе seed мира и индекса дня
        long seed = level.getSeed() ^ (dayIndex * 0x9E3779B97F4A7C15L);
        Random rng = new Random(seed);

        // Определяем сезон для нужного дня
        Season season = Season.values()[(dayIndex / 28) % 4];
        
        // Проверка на фестиваль (для завтрашнего дня)
        if (dayIndex == data.getTotalDays() + 1 && data.getFestivalTomorrow() != null) {
            return Weather.FEST_OVERRIDE;
        }

        switch (season) {
            case WINTER:
                return Weather.SNOW; // SV: зимой снег по умолчанию
            case SPRING: {
                // Весна: Дождь 20%, Ветер 15%, остальное - Солнечно
                float r = rng.nextFloat();
                if (r < 0.20f) return Weather.RAIN;
                if (r < 0.35f) return Weather.WIND;
                return Weather.SUNNY;
            }
            case SUMMER: {
                // Лето: больше дождя, шанс грозы
                float r = rng.nextFloat();
                if (r < 0.30f) {
                    // Подброс на грозу
                    return rng.nextFloat() < 0.30f ? Weather.STORM : Weather.RAIN;
                }
                return Weather.SUNNY;
            }
            case FALL: {
                // Осень: Дождь 25%, Ветер 20%, Гроза 5%
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

    private static float pickDailyLuckForDay(ServerLevel level, StardewDateData data, int dayIndex) {
        long seed = (level.getSeed() * 31L) ^ (dayIndex * 0xC2B2AE3D27D4EB4FL);
        Random rng = new Random(seed);
        // Удача центрирована около 0, диапазон примерно [-0.1, +0.1]
        return (rng.nextFloat() - 0.5f) * 0.2f;
    }
}



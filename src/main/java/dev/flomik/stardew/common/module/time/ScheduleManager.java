package dev.flomik.stardew.common.module.time;

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
        
        // 1. Генерируем погоду на завтра ПЕРЕД переходом на следующий день
        WeatherSystem.generateTomorrow(level);

        // 2. Сохраняем текущий сезон для проверки изменений
        Season previousSeason = date.getSeason();

        // 3. Переходим на следующий день (tomorrowWeather -> todayWeather)
        date.advance();
        date.setDirty();

        // 4. Если сезон изменился, обновляем все блоки травы
//        if (previousSeason != date.getSeason()) {
//            GrassBlockUpdateSystem.updateAllGrassBlocks(level);
//        }

        // 5. Применяем сегодняшнюю погоду к миру Minecraft
        WeatherSystem.applyWeatherToWorld(level, date.getTodayWeather());

        // 6. Устанавливаем время на 6:00
        level.setDayTime(StardewTimeUtils.toTicks(6, 0));

        // 7. Запускаем утренние процедуры
        for (ScheduleEntry entry : entries) {
            if (entry.getStartTick() == StardewTimeUtils.toTicks(6, 0)) {
                entry.trigger();
            }
        }
    }
}

package dev.flomik.stardew.core.crop.logic;

import dev.flomik.stardew.core.crop.runtime.FarmlandTracker;
import dev.flomik.stardew.core.time.StardewDateData;
import dev.flomik.stardew.core.time.Weather;
import dev.flomik.stardew.core.crop.blockentity.CropBlockEntity;
import dev.flomik.stardew.common.registry.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class MorningPass {

    public static void run(ServerLevel level) {
        // Получаем погоду из StardewDateData (детерминированно сгенерированную)
        var date = StardewDateData.get(level);
        Weather w = date.getTodayWeather();
        
        // Сначала обезвоживаем всю землю
        for (BlockPos pos : FarmlandTracker.all(level)) {
            var be = level.getBlockEntity(pos);
            if (be instanceof FarmlandBlockEntity fb) {
                fb.dehydrate();
            }
        }

        // Затем увлажняем в зависимости от погоды и удобрений
        for (BlockPos pos : FarmlandTracker.all(level)) {
            var be = level.getBlockEntity(pos);
            if (be instanceof FarmlandBlockEntity fb) {
                if (w == Weather.RAIN || w == Weather.STORM) {
                    fb.hydrate();
                } else {
                    var fert = fb.getFertilizer();
                    float p = fert.isRetention() ? fert.strength : 0f;
                    if (p > 0f && level.random.nextFloat() < p) {
                        fb.hydrate();
                    }
                }
            }
        }

        // Убийство культур вне сезона на улице зимой/не по сезону (приближение SV)
        var date2 = StardewDateData.get(level);
        for (BlockPos pos : dev.flomik.stardew.core.crop.runtime.CropTracker.all(level)) {
            var be = level.getBlockEntity(pos);
            if (be instanceof CropBlockEntity crop) {
                var def = crop.def();
                if (def == null) continue;
                // только если мир не теплица (у тебя пока нет теплицы — считаем весь мир уличным)
                boolean inSeason = def.seasons.contains(date2.getSeason().name().toLowerCase());
                if (!inSeason) {
                    level.removeBlock(pos, false);
                }
            }
        }
    }
}

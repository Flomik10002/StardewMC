package dev.flomik.stardew.core.crop.logic;

import dev.flomik.stardew.core.crop.runtime.FarmlandTracker;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class MorningPass {

    public static void run(ServerLevel level, int today, boolean isRaining) {
        for (BlockPos pos : FarmlandTracker.all(level)) {
            var be = level.getBlockEntity(pos);
            if (be instanceof FarmlandBlockEntity fb) {
                fb.dehydrate(today);
            }
        }
        
        for (BlockPos pos : FarmlandTracker.all(level)) {
            var be = level.getBlockEntity(pos);
            if (be instanceof FarmlandBlockEntity fb) {
                if (isRaining) {
                    fb.hydrate(today);
                } else {
                    var fert = fb.getFertilizer();
                    float p = fert.isRetention() ? fert.strength : 0f;
                    if (p > 0f && level.random.nextFloat() < p) {
                        fb.hydrate(today);
                    }
                }
            }
        }
    }
}

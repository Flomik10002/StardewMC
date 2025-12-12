package dev.flomik.stardew.common.module.farming.crop.logic;

import dev.flomik.stardew.common.module.farming.crop.block.BlockCrop;
import dev.flomik.stardew.common.module.farming.crop.blockentity.CropBlockEntity;
import dev.flomik.stardew.common.module.farming.crop.runtime.CropTracker;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class GrowthSystem {

    public static void run(ServerLevel level) {
        for (BlockPos cropPos : CropTracker.all(level)) {
            var be = level.getBlockEntity(cropPos);
            if (!(be instanceof CropBlockEntity cropBe)) continue;

            var farmPos = cropPos.below();
            var fbe = level.getBlockEntity(farmPos);
            if (!(fbe instanceof FarmlandBlockEntity fb)) continue;

            boolean watered = fb.isHydrated();
            boolean firstHarvestPhase = !cropBe.isReady(); // сохраняем флаг, но скорость теперь применена при посадке
            float speedMul = 0f;
            boolean paddy = isPaddyBoost(level, farmPos, cropBe);

            cropBe.growOneDay(watered, firstHarvestPhase, speedMul, paddy);
            cropBe.tickRegrowIfNeeded();

            // обнови AGE (0..7)
            var state = level.getBlockState(cropPos);
            var def = cropBe.def();
            int max = Math.max(1, def.daysInPhase.size()-1);
            int age = Math.min(7, Math.round((cropBe.currentPhase() * 7f) / max));
            if (state.hasProperty(BlockCrop.AGE) && state.getValue(BlockCrop.AGE) != age) {
                level.setBlock(cropPos, state.setValue(BlockCrop.AGE, age), 2);
            }
        }
    }

    private static boolean isPaddyBoost(ServerLevel level, BlockPos pos, CropBlockEntity be) {
        var def = be.def();
        if (def == null || !def.isPaddyCrop) return false;
        int r = 3;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx=-r; dx<=r; dx++)
            for (int dz=-r; dz<=r; dz++) {
                m.set(pos.getX()+dx, pos.getY()-1, pos.getZ()+dz);
                var fluid = level.getFluidState(m);
                if (!fluid.isEmpty()) return true; // упростим: есть вода — ок
            }
        return false;
    }
}

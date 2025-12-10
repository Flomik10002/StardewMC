package dev.flomik.stardew.common.module.farming.crop.logic;

import dev.flomik.stardew.common.module.farming.crop.blockentity.CropBlockEntity;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.common.module.farming.crop.FertilizerType;
import dev.flomik.stardew.common.module.time.StardewDateData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public final class HarvestHelper {
    private static final Random RNG = new Random();

    public static boolean harvest(ServerLevel level, CropBlockEntity cropBe, Player player) {
        var def = cropBe.def(); if (def == null || !cropBe.isReady()) return false;

        int count = def.harvestMin + RNG.nextInt(Math.max(1, def.harvestMax - def.harvestMin + 1));
        float extra = Math.max(0f, Math.min(0.9f, def.extraHarvestChance));
        while (RNG.nextFloat() < extra) count++; // геометрическая серия

        var item = ForgeRegistries.ITEMS.getValue(def.harvestItem);
        if (item == null) return false;

        // Качество: приблизим SV. Уровень удобрения качества влияет на шанс лучшего качества.
        int quality = computeQuality(level, cropBe);

        ItemStack drop = new ItemStack(item, count);
        var pos = cropBe.getBlockPos();
        // пока не кодируем качество в NBT — можно позже добавить (например, setDamage/CustomTag)
        player.drop(drop, false);

        cropBe.onHarvestDone(); // регров/удаление
        return true;
    }

    private static int computeQuality(ServerLevel level, CropBlockEntity cropBe) {
        // 0=normal, 1=silver, 2=gold, 3=iridium (SV использует 4 для iridium, но мы сведём к 3 тут)
        // Оценим по типу удобрения под культурой и дневной удаче: базовая эвристика.
        int qLevelFromFert = 0;
        var fb = level.getBlockEntity(cropBe.getBlockPos().below());
        if (fb instanceof FarmlandBlockEntity farm) {
            FertilizerType f = farm.getFertilizer();
            if (f == FertilizerType.BASIC_FERTILIZER) qLevelFromFert = 1;
            else if (f == FertilizerType.QUALITY_FERTILIZER) qLevelFromFert = 2;
            else if (f == FertilizerType.DELUXE_FERTILIZER) qLevelFromFert = 3;
        }

        float luck = StardewDateData.get(level).getDailyLuck();
        float baseGold = 0.02f + 0.06f * qLevelFromFert; // 8% на делюкс
        baseGold += Math.max(0f, luck) * 0.5f; // удача слегка повышает
        float baseSilver = Math.min(0.75f, baseGold * 2f);

        if (qLevelFromFert >= 3 && RNG.nextFloat() < baseGold * 0.5f) return 3;
        if (RNG.nextFloat() < baseGold) return 2;
        if (RNG.nextFloat() < baseSilver || qLevelFromFert >= 3) return 1;
        return 0;
    }
}

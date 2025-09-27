package dev.flomik.stardew.core.crop.logic;

import dev.flomik.stardew.core.crop.blockentity.CropBlockEntity;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
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
        float p = Math.max(0f, Math.min(0.9f, def.extraHarvestChance));
        while (RNG.nextFloat() < p) count++; // геометрическая серия, как в SV

        var item = ForgeRegistries.ITEMS.getValue(def.harvestItem);
        if (item == null) return false;

        // качество — при желании добавишь NBT/метку; здесь просто дропаем
        ItemStack drop = new ItemStack(item, count);
        var pos = cropBe.getBlockPos();
        player.drop(drop, false);

        cropBe.onHarvestDone(); // регров/удаление
        return true;
    }
}

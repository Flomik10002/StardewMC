package dev.flomik.stardew.common.module.machinery.recipe;

import dev.flomik.stardew.common.api.quality.Quality;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface MachineRecipe {
    boolean matches(ItemStack input);

    ProcessingResult getResult(ItemStack input, RandomSource random);

    int getProcessingTime();

    record ProcessingResult(ItemStack output, Quality quality) {}
}
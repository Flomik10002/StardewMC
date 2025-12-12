package dev.flomik.stardew.common.module.machinery.recipe;

import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class LoomRecipes {
    public static MachineRecipe WOOL = new MachineRecipe() {
        @Override
        public boolean matches(ItemStack input) {
            return input.is(ModItems.WOOL.get());
        }

        @Override
        public ProcessingResult getResult(ItemStack input, RandomSource random) {
            Quality inputQ = Quality.get(input);
            int count = 1;

            // Silver = 10% chance for 2
            // Gold = 50% chance for 2
            // Iridium = 100% chance for 2
            if (inputQ == Quality.SILVER && random.nextFloat() < 0.10f) count = 2;
            if (inputQ == Quality.GOLD && random.nextFloat() < 0.50f) count = 2;
            if (inputQ == Quality.IRIDIUM) count = 2;

            return new ProcessingResult(new ItemStack(ModItems.CLOTH.get(), count), Quality.NORMAL);
        }

        @Override
        public int getProcessingTime() { return 240; }
    };
}
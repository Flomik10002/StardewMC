package dev.flomik.stardew.common.module.machinery.recipe;

import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OilMakerRecipes {

    public static MachineRecipe OIL_FROM_SUNFLOWER = new SimpleRecipe(
            ModItems.MILK.get(),
            ModItems.OIL.get(),
            Quality.NORMAL,
            200
    );

    public static MachineRecipe OIL_FROM_SUNFLOWER_SEEDS = new SimpleRecipe(
            ModItems.MILK.get(),
            ModItems.OIL.get(),
            Quality.NORMAL,
            200
    );

    public static MachineRecipe OIL_FROM_CORN = new SimpleRecipe(
            ModItems.LARGE_MILK.get(),
            ModItems.OIL.get(),
            Quality.NORMAL,
            200
    );

    public static MachineRecipe TRUFFLE_OIL = new SimpleRecipe(
            ModItems.TRUFFLE.get(),
            ModItems.TRUFFLE_OIL.get(),
            Quality.NORMAL,
            200
    );

    private static class SimpleRecipe implements MachineRecipe {
        private final Item input;
        private final Item output;
        private final Quality targetQuality;
        private final int time;

        public SimpleRecipe(Item input, Item output, Quality targetQuality, int time) {
            this.input = input;
            this.output = output;
            this.targetQuality = targetQuality;
            this.time = time;
        }

        @Override
        public boolean matches(ItemStack stack) {
            return stack.is(input);
        }

        @Override
        public ProcessingResult getResult(ItemStack inputStack, RandomSource random) {
            return new ProcessingResult(new ItemStack(output), targetQuality);
        }

        @Override
        public int getProcessingTime() { return time; }
    }
}
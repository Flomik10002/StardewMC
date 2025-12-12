package dev.flomik.stardew.common.module.machinery.recipe;

import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.registry.ModItems; // Твои предметы
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CheesePressRecipes {

    // Обычное молоко -> Обычный сыр
    public static MachineRecipe MILK = new SimpleRecipe(
            ModItems.MILK.get(),
            ModItems.CHEESE.get(),
            Quality.NORMAL,
            200
    );

    // Большое молоко -> Золотой сыр
    public static MachineRecipe LARGE_MILK = new SimpleRecipe(
            ModItems.LARGE_MILK.get(),
            ModItems.CHEESE.get(),
            Quality.GOLD, // Всегда золото
            200
    );

    // Простая реализация
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
            // Тут можно добавить логику сохранения цены, если нужно для вина
            return new ProcessingResult(new ItemStack(output), targetQuality);
        }

        @Override
        public int getProcessingTime() { return time; }
    }
}
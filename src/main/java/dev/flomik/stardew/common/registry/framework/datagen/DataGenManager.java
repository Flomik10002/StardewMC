package dev.flomik.stardew.common.registry.framework.datagen;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DataGenManager {
    private static final Map<Supplier<? extends Item>, ItemModelGen> ITEM_MODELS = new HashMap<>();

    public static void assign(Supplier<? extends Item> item, ItemModelGen generator) {
        ITEM_MODELS.put(item, generator);
    }

    public static void generateAll(ItemModelProvider provider) {
        ITEM_MODELS.forEach((itemSupplier, generator) -> {
            Item item = itemSupplier.get();
            String name = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item).getPath();
            generator.generate(provider, item, name);
        });
    }
}
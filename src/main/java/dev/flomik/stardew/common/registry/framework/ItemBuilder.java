package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.registry.StardewRegistry;
import dev.flomik.stardew.common.registry.framework.datagen.DataGenManager;
import dev.flomik.stardew.common.registry.framework.datagen.ItemModelGen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class ItemBuilder<T extends Item> {
    private final String name;
    private Function<Item.Properties, T> factory;
    private Item.Properties properties = new Item.Properties();
    private RegistryObject<CreativeModeTab> tab = null;

    private ItemModelGen modelGenerator = null;

    public static ItemBuilder<Item> create(String name) {
        return new ItemBuilder<>(name, Item::new);
    }

    public static <I extends Item> ItemBuilder<I> create(String name, Function<Item.Properties, I> factory) {
        return new ItemBuilder<>(name, factory);
    }

    private ItemBuilder(String name, Function<Item.Properties, T> factory) {
        this.name = name;
        this.factory = factory;
    }

    public ItemBuilder<T> stacksTo(int count) {
        this.properties.stacksTo(count);
        return this;
    }

    public ItemBuilder<T> tab(RegistryObject<CreativeModeTab> tab) {
        this.tab = tab;
        return this;
    }

    /**
     * Указывает, какую модель генерировать.
     * @param generator Пресет (например ModelPresets.simple())
     */
    public ItemBuilder<T> model(ItemModelGen generator) {
        this.modelGenerator = generator;
        return this;
    }

    // -------------------

    public RegistryObject<T> register() {
        RegistryObject<T> itemParams = StardewRegistry.ITEMS.register(name, () -> factory.apply(properties));

        if (tab != null) {
            TabManager.assign(tab, itemParams);
        }

        if (modelGenerator != null) {
            DataGenManager.assign(itemParams, modelGenerator);
        }

        return itemParams;
    }
}
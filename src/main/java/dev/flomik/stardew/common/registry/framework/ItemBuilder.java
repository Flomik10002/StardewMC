package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.registry.StardewRegistry;
import dev.flomik.stardew.common.registry.framework.datagen.DataGenManager;
import dev.flomik.stardew.common.registry.framework.datagen.ItemModelGen;
import dev.flomik.stardew.common.registry.framework.tooltip.StardewTooltip;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemBuilder<T extends Item> {
    private final String name;
    private Function<Item.Properties, T> factory;
    private Item.Properties properties = new Item.Properties().stacksTo(999);
    private RegistryObject<CreativeModeTab> tab = null;
    private final List<StardewTooltip> tooltips = new ArrayList<>();

    private ItemModelGen visualGenerator = null;

    public static ItemBuilder<StardewItemBase> create(String name) {
        return new ItemBuilder<>(name, StardewItemBase::new);
    }

    public static <I extends Item> ItemBuilder<I> create(String name, Function<Item.Properties, I> factory) {
        return new ItemBuilder<>(name, factory);
    }

    private ItemBuilder(String name, Function<Item.Properties, T> factory) {
        this.name = name;
        this.factory = factory;
    }

    public ItemBuilder<T> addTooltip(StardewTooltip tooltip) {
        this.tooltips.add(tooltip);
        return this;
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
    public ItemBuilder<T> visual(ItemModelGen generator) {
        this.visualGenerator = generator;
        return this;
    }

    public RegistryObject<T> register() {
        T itemInstance = factory.apply(properties);

        if (itemInstance instanceof IStardewItem stardewItem) {
            stardewItem.setTooltips(new ArrayList<>(this.tooltips));
        }

        RegistryObject<T> itemParams = StardewRegistry.ITEMS.register(name, () -> itemInstance);

        if (tab != null) {
            TabManager.assign(tab, itemParams);
        }

        if (visualGenerator != null) {
            DataGenManager.assign(itemParams, visualGenerator);
        }

        return itemParams;
    }
}
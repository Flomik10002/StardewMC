package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.registry.StardewRegistry;
import dev.flomik.stardew.common.registry.framework.datagen.DataGenManager;
import dev.flomik.stardew.common.registry.framework.datagen.ItemModelGen;
import dev.flomik.stardew.common.registry.framework.tooltip.ItemCategory;
import dev.flomik.stardew.common.registry.framework.tooltip.StardewTooltip;
import dev.flomik.stardew.common.registry.framework.tooltip.TooltipPresets;
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
    private StardewTooltip categoryTooltip = null;

    private ItemModelGen visualGenerator = null;

    public static ItemBuilder<StardewItemBase> create(String name) {
        return new ItemBuilder<>(name, StardewItemBase::new);
    }

    /**
     * Создает билдер для еды с указанным edibility (первичный параметр еды из Stardew Valley).
     * 
     * @param name имя предмета
     * @param edibility параметр из ObjectData Stardew Valley (определяет восполнение стамины и здоровья)
     */
    public static ItemBuilder<StardewFoodItem> createFood(String name, int edibility) {
        return new ItemBuilder<>(name, p -> new StardewFoodItem(p, edibility));
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

    public ItemBuilder<T> category(ItemCategory category) {
        this.categoryTooltip = TooltipPresets.category(category);
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
        RegistryObject<T> itemParams = StardewRegistry.ITEMS.register(name, () -> {
            T itemInstance = factory.apply(properties);

            if (itemInstance instanceof IStardewItem stardewItem) {
                List<StardewTooltip> finalTooltips = new ArrayList<>();
                if (this.categoryTooltip != null) {
                    finalTooltips.add(this.categoryTooltip);
                }
                finalTooltips.add(TooltipPresets.separator());
                finalTooltips.addAll(this.tooltips);
                stardewItem.setTooltips(finalTooltips);
            }

            return itemInstance;
        });

        if (tab != null) {
            TabManager.assign(tab, itemParams);
        }

        if (visualGenerator != null) {
            DataGenManager.assign(itemParams, visualGenerator);
        }

        return itemParams;
    }
}

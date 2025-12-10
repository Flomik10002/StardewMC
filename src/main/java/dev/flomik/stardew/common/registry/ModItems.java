package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.common.registry.framework.ItemBuilder;
import dev.flomik.stardew.common.registry.framework.datagen.ModelPresets;
import dev.flomik.stardew.core.item.ItemStardewSeed;
import dev.flomik.stardew.core.item.tool.PatternType;
import dev.flomik.stardew.core.item.tool.ToolHoe;
import dev.flomik.stardew.core.item.tool.ToolWateringCan;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final RegistryObject<Item> TOMATO = ItemBuilder.create("tomato")
            .tab(ModTabs.CROPS)
            .model(ModelPresets.simple())
            .register();

    public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create("tomato_seeds",
                    p -> new ItemStardewSeed(p, StardewRegistry.id("tomato")))
            .tab(ModTabs.CROPS)
            .model(ModelPresets.seed())
            .register();

    public static final RegistryObject<Item> HONEY = ItemBuilder
            .create("honey")
            .stacksTo(999)
            .tab(ModTabs.ARTISAN_GOODS)
            .model(ModelPresets.simple())
            .register();

    public static final RegistryObject<Item> WINE = ItemBuilder
            .create("wine")
            .stacksTo(999)
            .tab(ModTabs.ARTISAN_GOODS)
            .model(ModelPresets.simple())
            .register();

    public static final RegistryObject<Item> CHEESE = ItemBuilder
            .create("cheese")
            .stacksTo(999)
            .tab(ModTabs.ARTISAN_GOODS)
            .model(ModelPresets.simple())
            .register();

    public static final RegistryObject<Item> GOAT_CHEESE = ItemBuilder
            .create("goat_cheese")
            .stacksTo(999)
            .tab(ModTabs.ARTISAN_GOODS)
            .model(ModelPresets.simple())
            .register();

    public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
            .create("basic_hoe", p -> new ToolHoe(p, PatternType.SINGLE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> COPPER_HOE = ItemBuilder
            .create("copper_hoe", p -> new ToolHoe(p, PatternType.THREE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> STEEL_HOE = ItemBuilder
            .create("steel_hoe", p -> new ToolHoe(p, PatternType.FIVE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> GOLD_HOE = ItemBuilder
            .create("gold_hoe", p -> new ToolHoe(p, PatternType.GRID_3X3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> IRIDIUM_HOE = ItemBuilder
            .create("iridium_hoe", p -> new ToolHoe(p, PatternType.GRID_5X5))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> BASIC_WATERING_CAN = ItemBuilder
            .create("basic_watering_can", p -> new ToolWateringCan(p,40, PatternType.SINGLE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> COPPER_WATERING_CAN = ItemBuilder
            .create("copper_watering_can", p -> new ToolWateringCan(p,55, PatternType.SINGLE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> STEEL_WATERING_CAN = ItemBuilder
            .create("steel_watering_can", p -> new ToolWateringCan(p,70, PatternType.SINGLE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> GOLD_WATERING_CAN = ItemBuilder
            .create("gold_watering_can", p -> new ToolWateringCan(p,85, PatternType.SINGLE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> IRIDIUM_WATERING_CAN = ItemBuilder
            .create("iridium_watering_can", p -> new ToolWateringCan(p,100, PatternType.SINGLE))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static void load() {}
}

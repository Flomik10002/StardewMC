package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.common.registry.framework.ItemBuilder;
import dev.flomik.stardew.common.registry.framework.datagen.ModelPresets;
import dev.flomik.stardew.common.module.farming.item.ItemStardewSeed;
import dev.flomik.stardew.common.module.tools.PatternType;
import dev.flomik.stardew.common.module.tools.item.ToolHoe;
import dev.flomik.stardew.common.module.tools.item.ToolPickaxe;
import dev.flomik.stardew.common.module.tools.item.ToolWateringCan;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final RegistryObject<Item> TOMATO = ItemBuilder.create("tomato")
            .tab(ModTabs.CROPS)
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create("tomato_seeds",
                    p -> new ItemStardewSeed(p, StardewRegistry.id("tomato")))
            .tab(ModTabs.CROPS)
            .visual(ModelPresets.seed())
            .register();

    public static final RegistryObject<Item> HONEY = ItemBuilder
            .create("honey")
            .tab(ModTabs.ARTISAN_GOODS)
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<Item> WINE = ItemBuilder
            .create("wine")
            .tab(ModTabs.ARTISAN_GOODS)
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<Item> CHEESE = ItemBuilder
            .create("cheese")
            .tab(ModTabs.ARTISAN_GOODS)
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<Item> GOAT_CHEESE = ItemBuilder
            .create("goat_cheese")
            .tab(ModTabs.ARTISAN_GOODS)
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
            .create("basic_hoe", p -> new ToolHoe(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> COPPER_HOE = ItemBuilder
            .create("copper_hoe", p -> new ToolHoe(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> STEEL_HOE = ItemBuilder
            .create("steel_hoe", p -> new ToolHoe(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> GOLD_HOE = ItemBuilder
            .create("gold_hoe", p -> new ToolHoe(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolHoe> IRIDIUM_HOE = ItemBuilder
            .create("iridium_hoe", p -> new ToolHoe(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> BASIC_WATERING_CAN = ItemBuilder
            .create("basic_watering_can", p -> new ToolWateringCan(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> COPPER_WATERING_CAN = ItemBuilder
            .create("copper_watering_can", p -> new ToolWateringCan(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> STEEL_WATERING_CAN = ItemBuilder
            .create("steel_watering_can", p -> new ToolWateringCan(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> GOLD_WATERING_CAN = ItemBuilder
            .create("gold_watering_can", p -> new ToolWateringCan(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolWateringCan> IRIDIUM_WATERING_CAN = ItemBuilder
            .create("iridium_watering_can", p -> new ToolWateringCan(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .register();

    public static final RegistryObject<ToolPickaxe> BASIC_PICKAXE = ItemBuilder
            .create("basic_pickaxe", p -> new ToolPickaxe(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .register();

    public static final RegistryObject<ToolPickaxe> COPPER_PICKAXE = ItemBuilder
            .create("copper_pickaxe", p -> new ToolPickaxe(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .register();

    public static final RegistryObject<ToolPickaxe> STEEL_PICKAXE = ItemBuilder
            .create("steel_pickaxe", p -> new ToolPickaxe(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .register();

    public static final RegistryObject<ToolPickaxe> GOLD_PICKAXE = ItemBuilder
            .create("gold_pickaxe", p -> new ToolPickaxe(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .register();

    public static final RegistryObject<ToolPickaxe> IRIDIUM_PICKAXE = ItemBuilder
            .create("iridium_pickaxe", p -> new ToolPickaxe(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .register();

    public static void load() {}
}

package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.common.module.tools.ToolEnchantment;
import dev.flomik.stardew.common.registry.framework.ItemBuilder;
import dev.flomik.stardew.common.registry.framework.StardewItemBase;
import dev.flomik.stardew.common.registry.framework.datagen.ModelPresets;
import dev.flomik.stardew.common.module.farming.item.ItemStardewSeed;
import dev.flomik.stardew.common.module.tools.PatternType;
import dev.flomik.stardew.common.module.tools.item.ToolHoe;
import dev.flomik.stardew.common.module.tools.item.ToolPickaxe;
import dev.flomik.stardew.common.module.tools.item.ToolWateringCan;
import dev.flomik.stardew.common.registry.framework.tooltip.ItemCategory;
import dev.flomik.stardew.common.registry.framework.tooltip.TooltipKeys;
import dev.flomik.stardew.common.registry.framework.tooltip.TooltipPresets;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final RegistryObject<StardewItemBase> TOMATO = ItemBuilder.create("tomato")
            .tab(ModTabs.CROPS)
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create("tomato_seeds",
                    p -> new ItemStardewSeed(p, StardewRegistry.id("tomato")))
            .tab(ModTabs.CROPS)
            .visual(ModelPresets.seed())
            .register();

    public static final RegistryObject<StardewItemBase> EGG = ItemBuilder
            .create("egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(50))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> LARGE_EGG = ItemBuilder
            .create("large_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(95))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DUCK_EGG = ItemBuilder
            .create("duck_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(95))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> GOLDEN_EGG = ItemBuilder
            .create("golden_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(500))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> VOID_EGG = ItemBuilder
            .create("void_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(65))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DINOSAUR_EGG = ItemBuilder
            .create("dinosaur_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(350))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> OSTRICH_EGG = ItemBuilder
            .create("ostrich_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(600))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> GOAT_MILK = ItemBuilder
            .create("goat_milk")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(225))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> LARGE_GOAT_MILK = ItemBuilder
            .create("large_goat_milk")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(345))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DUCK_FEATHER = ItemBuilder
            .create("duck_feather")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(250))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> RABBITS_FOOT = ItemBuilder
            .create("rabbits_foot")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(565))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> TRUFFLE = ItemBuilder
            .create("truffle")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(625))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> SLIME = ItemBuilder
            .create("slime")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.price(5))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> ROE = ItemBuilder
            .create("roe")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            // Price depends on fish, so no fixed price tooltip
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> MILK = ItemBuilder
            .create("milk")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> LARGE_MILK = ItemBuilder
            .create("large_milk")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> WOOL = ItemBuilder
            .create("wool")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> CLOTH = ItemBuilder
            .create("cloth")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> HONEY = ItemBuilder
            .create("honey")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> WINE = ItemBuilder
            .create("wine")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> CHEESE = ItemBuilder
            .create("cheese")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .addTooltip(TooltipPresets.price(230))
            .register();

    public static final RegistryObject<StardewItemBase> GOAT_CHEESE = ItemBuilder
            .create("goat_cheese")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(400))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> PALE_ALE = ItemBuilder
            .create("pale_ale")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(300))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> BEER = ItemBuilder
            .create("beer")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(200))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> MEAD = ItemBuilder
            .create("mead")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(300))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> MAYONNAISE = ItemBuilder
            .create("mayonnaise")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(190))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DUCK_MAYONNAISE = ItemBuilder
            .create("duck_mayonnaise")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(375))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> VOID_MAYONNAISE = ItemBuilder
            .create("void_mayonnaise")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(275))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DINOSAUR_MAYONNAISE = ItemBuilder
            .create("dinosaur_mayonnaise")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(800))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> TRUFFLE_OIL = ItemBuilder
            .create("truffle_oil")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(1065))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> OIL = ItemBuilder
            .create("oil")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(100))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> PICKLES = ItemBuilder
            .create("pickles")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> JELLY = ItemBuilder
            .create("jelly")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> CAVIAR = ItemBuilder
            .create("caviar")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(500))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> AGED_ROE = ItemBuilder
            .create("aged_roe")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DRIED_MUSHROOMS = ItemBuilder
            .create("dried_mushrooms")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DRIED_FRUIT = ItemBuilder
            .create("dried_fruit")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> RAISINS = ItemBuilder
            .create("raisins")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(600))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> SMOKED_FISH = ItemBuilder
            .create("smoked_fish")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> COFFEE = ItemBuilder
            .create("coffee")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(150))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> GREEN_TEA = ItemBuilder
            .create("green_tea")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(100))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> JUICE = ItemBuilder
            .create("juice")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> VINEGAR = ItemBuilder
            .create("vinegar")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.price(100))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
            .create("basic_hoe", p -> new ToolHoe(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_HOE))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> COPPER_HOE = ItemBuilder
            .create("copper_hoe", p -> new ToolHoe(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_HOE))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> STEEL_HOE = ItemBuilder
            .create("steel_hoe", p -> new ToolHoe(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_HOE))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> GOLD_HOE = ItemBuilder
            .create("gold_hoe", p -> new ToolHoe(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_HOE))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> IRIDIUM_HOE = ItemBuilder
            .create("iridium_hoe", p -> new ToolHoe(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_HOE))
            .addTooltip(TooltipPresets.patternInfo())
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.REACHING))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.ARCHAEOLOGIST))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.EFFICIENT))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.SWIFT))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.GENEROUS))
            .register();

    public static final RegistryObject<ToolWateringCan> BASIC_WATERING_CAN = ItemBuilder
            .create("basic_watering_can", p -> new ToolWateringCan(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_1))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_2))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> COPPER_WATERING_CAN = ItemBuilder
            .create("copper_watering_can", p -> new ToolWateringCan(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_1))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_2))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> STEEL_WATERING_CAN = ItemBuilder
            .create("steel_watering_can", p -> new ToolWateringCan(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_1))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_2))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> GOLD_WATERING_CAN = ItemBuilder
            .create("gold_watering_can", p -> new ToolWateringCan(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_1))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_2))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> IRIDIUM_WATERING_CAN = ItemBuilder
            .create("iridium_watering_can", p -> new ToolWateringCan(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_1))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_WATERING_CAN_2))
            .addTooltip(TooltipPresets.patternInfo())
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.REACHING))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.BOTTOMLESS))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.EFFICIENT))
            .register();

    public static final RegistryObject<ToolPickaxe> BASIC_PICKAXE = ItemBuilder
            .create("basic_pickaxe", p -> new ToolPickaxe(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_PICKAXE))
            .register();

    public static final RegistryObject<ToolPickaxe> COPPER_PICKAXE = ItemBuilder
            .create("copper_pickaxe", p -> new ToolPickaxe(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_PICKAXE))
            .register();

    public static final RegistryObject<ToolPickaxe> STEEL_PICKAXE = ItemBuilder
            .create("steel_pickaxe", p -> new ToolPickaxe(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_PICKAXE))
            .register();

    public static final RegistryObject<ToolPickaxe> GOLD_PICKAXE = ItemBuilder
            .create("gold_pickaxe", p -> new ToolPickaxe(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_PICKAXE))
            .register();

    public static final RegistryObject<ToolPickaxe> IRIDIUM_PICKAXE = ItemBuilder
            .create("iridium_pickaxe", p -> new ToolPickaxe(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description(TooltipKeys.DESC_PICKAXE))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.POWERFUL))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.EFFICIENT))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.SWIFT))
            .register();

    public static void load() {}
}

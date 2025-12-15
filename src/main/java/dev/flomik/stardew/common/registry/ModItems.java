package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.common.module.tools.ToolEnchantment;
import dev.flomik.stardew.common.registry.framework.ItemBuilder;
import dev.flomik.stardew.common.registry.framework.StardewFoodItem;
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

    public static final RegistryObject<StardewFoodItem> TOMATO = ItemBuilder
            .createFood("tomato", 8) // edibility из Objects.csv (256)
            .tab(ModTabs.CROPS)
            .addTooltip(TooltipPresets.description("tooltip.stardew.tomato.desc"))
            .visual(ModelPresets.simple())
            .addTooltip(TooltipPresets.price(60))
            .register();

    public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create("tomato_seeds",
                    p -> new ItemStardewSeed(p, StardewRegistry.id("tomato")))
            .tab(ModTabs.CROPS)
            .visual(ModelPresets.seed())
            .register();

    public static final RegistryObject<StardewFoodItem> EGG = ItemBuilder
            .createFood("egg", 10) // edibility из Objects.csv (176/180)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.egg.desc"))
            .addTooltip(TooltipPresets.price(50))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> LARGE_EGG = ItemBuilder
            .createFood("large_egg", 15) // edibility из Objects.csv (174/182)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.large_egg.desc"))
            .addTooltip(TooltipPresets.price(95))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> DUCK_EGG = ItemBuilder
            .createFood("duck_egg", 15) // edibility из Objects.csv (442)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.duck_egg.desc"))
            .addTooltip(TooltipPresets.price(95))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> GOLDEN_EGG = ItemBuilder
            .create("golden_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.golden_egg.desc"))
            .addTooltip(TooltipPresets.price(500))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> VOID_EGG = ItemBuilder
            .createFood("void_egg", -15) // edibility из Objects.csv (305) - ОТНИМАЕТ стамину!
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.void_egg.desc"))
            .addTooltip(TooltipPresets.price(65))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DINOSAUR_EGG = ItemBuilder
            .create("dinosaur_egg")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.dinosaur_egg.desc"))
            .addTooltip(TooltipPresets.price(350))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> OSTRICH_EGG = ItemBuilder
            .createFood("ostrich_egg", 15) // edibility из Objects.csv (289)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.ostrich_egg.desc"))
            .addTooltip(TooltipPresets.price(600))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> GOAT_MILK = ItemBuilder
            .createFood("goat_milk", 25) // edibility из Objects.csv (436)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.goat_milk.desc"))
            .addTooltip(TooltipPresets.price(225))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> LARGE_GOAT_MILK = ItemBuilder
            .createFood("large_goat_milk", 35) // edibility из Objects.csv (438)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.large_goat_milk.desc"))
            .addTooltip(TooltipPresets.price(345))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> DUCK_FEATHER = ItemBuilder
            .create("duck_feather")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.duck_feather.desc"))
            .addTooltip(TooltipPresets.price(250))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> RABBITS_FOOT = ItemBuilder
            .create("rabbits_foot")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.rabbits_foot.desc"))
            .addTooltip(TooltipPresets.price(565))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> TRUFFLE = ItemBuilder
            .createFood("truffle", 5)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.truffle.desc"))
            .addTooltip(TooltipPresets.price(625))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> SLIME = ItemBuilder
            .create("slime")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.slime.desc"))
            .addTooltip(TooltipPresets.price(5))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> ROE = ItemBuilder
            .create("roe")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.roe.desc"))
            // Price depends on fish, so no fixed price tooltip
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> MILK = ItemBuilder
            .createFood("milk", 15) // edibility из Objects.csv (184)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.milk.desc"))
            .addTooltip(TooltipPresets.price(125))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> LARGE_MILK = ItemBuilder
            .createFood("large_milk", 20) // edibility из Objects.csv (186)
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.large_milk.desc"))
            .addTooltip(TooltipPresets.price(190))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> WOOL = ItemBuilder
            .create("wool")
            .tab(ModTabs.ANIMAL_PRODUCT)
            .addTooltip(TooltipPresets.category(ItemCategory.ANIMAL_PRODUCT))
            .addTooltip(TooltipPresets.description("tooltip.stardew.wool.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> CLOTH = ItemBuilder
            .create("cloth")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.cloth.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewItemBase> HONEY = ItemBuilder
            .create("honey")
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.honey.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> WINE = ItemBuilder
            .createFood("wine", 20) // edibility из Objects.csv (348)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.wine.desc"))
            .addTooltip(TooltipPresets.price(400))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> CHEESE = ItemBuilder
            .createFood("cheese", 50) // edibility из Objects.csv (424)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.separator())  // Линия-разделитель
            .addTooltip(TooltipPresets.description("tooltip.stardew.cheese.desc"))
            .visual(ModelPresets.simple())
            .addTooltip(TooltipPresets.price(230))
            .register();

    public static final RegistryObject<StardewFoodItem> GOAT_CHEESE = ItemBuilder
            .createFood("goat_cheese", 50) // edibility из Objects.csv (426)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.goat_cheese.desc"))
            .addTooltip(TooltipPresets.price(400))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> PALE_ALE = ItemBuilder
            .createFood("pale_ale", 20) // edibility оценка (нет в CSV, аналог beer)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.pale_ale.desc"))
            .addTooltip(TooltipPresets.price(300))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> BEER = ItemBuilder
            .createFood("beer", 20) // edibility из Objects.csv (346)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.beer.desc"))
            .addTooltip(TooltipPresets.price(200))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> MEAD = ItemBuilder
            .createFood("mead", 20) // edibility оценка (нет в CSV, аналог wine)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.mead.desc"))
            .addTooltip(TooltipPresets.price(300))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> MAYONNAISE = ItemBuilder
            .createFood("mayonnaise", 20) // edibility из Objects.csv (306)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.mayonnaise.desc"))
            .addTooltip(TooltipPresets.price(190))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> DUCK_MAYONNAISE = ItemBuilder
            .createFood("duck_mayonnaise", 30) // edibility из Objects.csv (307)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.duck_mayonnaise.desc"))
            .addTooltip(TooltipPresets.price(375))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> VOID_MAYONNAISE = ItemBuilder
            .createFood("void_mayonnaise", -30) // edibility из Objects.csv (308) - ОТНИМАЕТ стамину!
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.void_mayonnaise.desc"))
            .addTooltip(TooltipPresets.price(275))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> DINOSAUR_MAYONNAISE = ItemBuilder
            .createFood("dinosaur_mayonnaise", 50) // edibility из Objects.csv (807)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.dinosaur_mayonnaise.desc"))
            .addTooltip(TooltipPresets.price(800))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> TRUFFLE_OIL = ItemBuilder
            .createFood("truffle_oil", 15) // edibility из Objects.csv (432)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.truffle_oil.desc"))
            .addTooltip(TooltipPresets.price(1065))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> OIL = ItemBuilder
            .createFood("oil", 5) // edibility из Objects.csv (247)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.oil.desc"))
            .addTooltip(TooltipPresets.price(100))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> PICKLES = ItemBuilder
            .createFood("pickles", 25) // edibility оценка (динамический в CSV, фиксированное значение)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.pickles.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> JELLY = ItemBuilder
            .createFood("jelly", 30) // edibility оценка (динамический в CSV, фиксированное значение)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.jelly.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> CAVIAR = ItemBuilder
            .createFood("caviar", 70) // edibility из Objects.csv (445)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.caviar.desc"))
            .addTooltip(TooltipPresets.price(500))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> AGED_ROE = ItemBuilder
            .createFood("aged_roe", 40) // edibility из Objects.csv (447)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.aged_roe.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> DRIED_MUSHROOMS = ItemBuilder
            .createFood("dried_mushrooms", 20) // edibility оценка (динамический в CSV, фиксированное значение)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.dried_mushrooms.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> DRIED_FRUIT = ItemBuilder
            .createFood("dried_fruit", 25) // edibility оценка (динамический в CSV, фиксированное значение)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.dried_fruit.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> RAISINS = ItemBuilder
            .createFood("raisins", 50) // edibility из Objects.csv (Raisins)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.raisins.desc"))
            .addTooltip(TooltipPresets.price(600))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> SMOKED_FISH = ItemBuilder
            .createFood("smoked_fish", 45) // edibility оценка (динамический в CSV, фиксированное значение)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.smoked_fish.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> COFFEE = ItemBuilder
            .createFood("coffee", 1) // edibility из Objects.csv (395)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.coffee.desc"))
            .addTooltip(TooltipPresets.price(150))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> GREEN_TEA = ItemBuilder
            .createFood("green_tea", 5) // edibility из Objects.csv (614)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.green_tea.desc"))
            .addTooltip(TooltipPresets.price(100))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> JUICE = ItemBuilder
            .createFood("juice", 30) // edibility из Objects.csv (350)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.juice.desc"))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<StardewFoodItem> VINEGAR = ItemBuilder
            .createFood("vinegar", 5)
            .tab(ModTabs.ARTISAN_GOODS)
            .addTooltip(TooltipPresets.category(ItemCategory.ARTISAN_GOODS))
            .addTooltip(TooltipPresets.description("tooltip.stardew.vinegar.desc"))
            .addTooltip(TooltipPresets.price(100))
            .visual(ModelPresets.simple())
            .register();

    public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
            .create("basic_hoe", p -> new ToolHoe(p, 0))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.hoe.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> COPPER_HOE = ItemBuilder
            .create("copper_hoe", p -> new ToolHoe(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.hoe.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> STEEL_HOE = ItemBuilder
            .create("steel_hoe", p -> new ToolHoe(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.hoe.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> GOLD_HOE = ItemBuilder
            .create("gold_hoe", p -> new ToolHoe(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.hoe.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolHoe> IRIDIUM_HOE = ItemBuilder
            .create("iridium_hoe", p -> new ToolHoe(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.hoe.desc"))
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
            .addTooltip(TooltipPresets.description("tooltip.stardew.watering_can.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> COPPER_WATERING_CAN = ItemBuilder
            .create("copper_watering_can", p -> new ToolWateringCan(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.watering_can.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> STEEL_WATERING_CAN = ItemBuilder
            .create("steel_watering_can", p -> new ToolWateringCan(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.watering_can.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> GOLD_WATERING_CAN = ItemBuilder
            .create("gold_watering_can", p -> new ToolWateringCan(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.watering_can.desc"))
            .addTooltip(TooltipPresets.patternInfo())
            .register();

    public static final RegistryObject<ToolWateringCan> IRIDIUM_WATERING_CAN = ItemBuilder
            .create("iridium_watering_can", p -> new ToolWateringCan(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.watering_can.desc"))
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
            .addTooltip(TooltipPresets.description("tooltip.stardew.pickaxe.desc"))
            .register();

    public static final RegistryObject<ToolPickaxe> COPPER_PICKAXE = ItemBuilder
            .create("copper_pickaxe", p -> new ToolPickaxe(p, 1))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.pickaxe.desc"))
            .register();

    public static final RegistryObject<ToolPickaxe> STEEL_PICKAXE = ItemBuilder
            .create("steel_pickaxe", p -> new ToolPickaxe(p, 2))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.pickaxe.desc"))
            .register();

    public static final RegistryObject<ToolPickaxe> GOLD_PICKAXE = ItemBuilder
            .create("gold_pickaxe", p -> new ToolPickaxe(p, 3))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.pickaxe.desc"))
            .register();

    public static final RegistryObject<ToolPickaxe> IRIDIUM_PICKAXE = ItemBuilder
            .create("iridium_pickaxe", p -> new ToolPickaxe(p, 4))
            .stacksTo(1)
            .tab(ModTabs.TOOLS)
            .visual(ModelPresets.handheld())
            .addTooltip(TooltipPresets.category(ItemCategory.TOOL))
            .addTooltip(TooltipPresets.description("tooltip.stardew.pickaxe.desc"))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.POWERFUL))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.EFFICIENT))
            .addTooltip(TooltipPresets.enchant(ToolEnchantment.SWIFT))
            .register();

    public static void load() {}
}

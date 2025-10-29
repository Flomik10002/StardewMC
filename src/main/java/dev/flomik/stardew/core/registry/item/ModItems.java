package dev.flomik.stardew.core.registry.item;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.item.ItemStardewSeed;
import dev.flomik.stardew.core.item.tool.PatternType;
import dev.flomik.stardew.core.item.tool.ToolHoe;
import dev.flomik.stardew.core.item.tool.ToolWateringCan;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, StardewMod.MODID);

    public static final RegistryObject<Item> TOMATO_ITEM =
            ITEMS.register("tomato_item", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAULIFLOWER_ITEM =
            ITEMS.register("cauliflower_item", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RICE_ITEM =
            ITEMS.register("rice_item", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TOMATO_SEEDS =
            ITEMS.register("tomato_seeds", () -> new ItemStardewSeed(new Item.Properties(),
                    new ResourceLocation(StardewMod.MODID, "tomato")));
    public static final RegistryObject<Item> CAULIFLOWER_SEEDS =
            ITEMS.register("cauliflower_seeds", () -> new ItemStardewSeed(new Item.Properties(),
                    new ResourceLocation(StardewMod.MODID, "cauliflower")));
    public static final RegistryObject<Item> RICE_SHOOTS =
            ITEMS.register("rice_shoots", () -> new ItemStardewSeed(new Item.Properties(),
                    new ResourceLocation(StardewMod.MODID, "rice")));

    public static final RegistryObject<Item> HONEY =
            ITEMS.register("honey", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WINE =
            ITEMS.register("wine", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHEESE =
            ITEMS.register("cheese", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOAT_CHEESE =
            ITEMS.register("goat_cheese", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BASIC_HOE = ITEMS.register("basic_hoe", () ->
            new ToolHoe(new Item.Properties().stacksTo(1), PatternType.SINGLE)
    );

    public static final RegistryObject<Item> COPPER_HOE = ITEMS.register("copper_hoe", () ->
            new ToolHoe(new Item.Properties().stacksTo(1), PatternType.THREE)
    ); 
    public static final RegistryObject<Item> STEEL_HOE = ITEMS.register("steel_hoe", () ->
            new ToolHoe(new Item.Properties().stacksTo(1), PatternType.FIVE)
    );

    public static final RegistryObject<Item> GOLD_HOE = ITEMS.register("gold_hoe", () ->
            new ToolHoe(new Item.Properties().stacksTo(1), PatternType.GRID_3X3)
    );

    public static final RegistryObject<Item> IRIDIUM_HOE = ITEMS.register("iridium_hoe", () ->
            new ToolHoe(new Item.Properties().stacksTo(1), PatternType.GRID_5X5)
    );

    public static final RegistryObject<Item> BASIC_WATERING_CAN = ITEMS.register("basic_watering_can", () ->
            new ToolWateringCan(
                    new Item.Properties().stacksTo(1),
                    40,
                    PatternType.SINGLE
            )
    );

    public static final RegistryObject<Item> COPPER_WATERING_CAN = ITEMS.register("copper_watering_can", () ->
            new ToolWateringCan(
                    new Item.Properties().stacksTo(1),
                    55,
                    PatternType.THREE
            )
    );

    public static final RegistryObject<Item> STEEL_WATERING_CAN = ITEMS.register("steel_watering_can", () ->
            new ToolWateringCan(
                    new Item.Properties().stacksTo(1),
                    70,
                    PatternType.FIVE
            )
    );

    public static final RegistryObject<Item> GOLD_WATERING_CAN = ITEMS.register("gold_watering_can", () ->
            new ToolWateringCan(
                    new Item.Properties().stacksTo(1),
                    85,
                    PatternType.GRID_3X3
            )
    );

    public static final RegistryObject<Item> IRIDIUM_WATERING_CAN = ITEMS.register("iridium_watering_can", () ->
            new ToolWateringCan(
                    new Item.Properties().stacksTo(1),
                    100,
                    PatternType.GRID_5X5
            )
    );
    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

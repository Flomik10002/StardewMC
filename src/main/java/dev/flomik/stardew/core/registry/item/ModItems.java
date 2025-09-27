package dev.flomik.stardew.core.registry.item;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.item.ItemStardewSeed;
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


    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

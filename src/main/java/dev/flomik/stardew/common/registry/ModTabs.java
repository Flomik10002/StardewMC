package dev.flomik.stardew.common.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class ModTabs {
    public static final RegistryObject<CreativeModeTab> BLOCK = StardewRegistry.TABS.register("blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.stardew.blocks"))
                    .icon(() -> new ItemStack(ModBlocks.DIRT.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> TOOLS = StardewRegistry.TABS.register("tools",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.stardew.tools"))
                    .icon(() -> new ItemStack(ModItems.BASIC_HOE.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> CRAFTABLES = StardewRegistry.TABS.register("craftables",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.stardew.craftables"))
                    .icon(() -> new ItemStack(ModBlocks.CHEST.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> ARTISAN_GOODS = StardewRegistry.TABS.register("artisan_goods",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.stardew.artisan_goods"))
                    .icon(() -> new ItemStack(ModItems.WINE.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> ANIMAL_PRODUCT = StardewRegistry.TABS.register("animal_product",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.stardew.animal_product"))
                    .icon(() -> new ItemStack(ModItems.MILK.get()))
                    .build());

    public static final RegistryObject<CreativeModeTab> CROPS = StardewRegistry.TABS.register("crops",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab.stardew.crops"))
                    .icon(() -> new ItemStack(ModItems.TOMATO.get()))
                    .build());

    public static void load() {}
}

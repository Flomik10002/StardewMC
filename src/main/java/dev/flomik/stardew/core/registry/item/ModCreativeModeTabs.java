package dev.flomik.stardew.core.registry.item;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.registry.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StardewMod.MODID);

    public static final RegistryObject<CreativeModeTab> STARDEW_BLOCK_TAB =
            TABS.register("stardew_block_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.stardew_block_tab"))
                    .icon(() -> new ItemStack(ModBlocks.DIRT.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.DIRT.get());
                        output.accept(ModBlocks.FARMLAND.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> STARDEW_CRAFTABLES_TAB =
            TABS.register("stardew_craftables_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.stardew_craftables_tab"))
                    .icon(() -> new ItemStack(ModBlocks.KEG.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.KEG.get());
                        output.accept(ModBlocks.BEE_HOUSE.get());
                        output.accept(ModBlocks.CHEESE_PRESS.get());
                    }).build());

//    public static final RegistryObject<CreativeModeTab> STARDEW_ITEM_TAB =
//            TABS.register("stardew_item_tab", () -> CreativeModeTab.builder()
//                    .title(Component.translatable("itemGroup.stardew_item_tab"))
//                    .icon(() -> new ItemStack(ModItems.TOMATO_ITEM.get()))
//                    .displayItems((params, output) -> {
//                        output.accept(ModItems.TOMATO_ITEM.get());
//                        output.accept(ModItems.CAULIFLOWER_ITEM.get());
//                        output.accept(ModItems.RICE_ITEM.get());
//                        output.accept(ModItems.TOMATO_SEEDS.get());
//                        output.accept(ModItems.CAULIFLOWER_SEEDS.get());
//                        output.accept(ModItems.RICE_SHOOTS.get());
//                    }).build());

    public static final RegistryObject<CreativeModeTab> STARDEW_TOOLS_TAB =
            TABS.register("stardew_tools_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.stardew_tools_tab"))
                    .icon(() -> new ItemStack(ModItems.BASIC_HOE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BASIC_HOE.get());
                        output.accept(ModItems.COPPER_HOE.get());
                        output.accept(ModItems.STEEL_HOE.get());
                        output.accept(ModItems.GOLD_HOE.get());
                        output.accept(ModItems.IRIDIUM_HOE.get());

                        output.accept(ModItems.BASIC_WATERING_CAN.get());
                        output.accept(ModItems.COPPER_WATERING_CAN.get());
                        output.accept(ModItems.STEEL_WATERING_CAN.get());
                        output.accept(ModItems.GOLD_WATERING_CAN.get());
                        output.accept(ModItems.IRIDIUM_WATERING_CAN.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> STARDEW_ARTISAN_GOODS_TAB =
            TABS.register("stardew_artisan_goods_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.stardew_artisan_goods_tab"))
                    .icon(() -> new ItemStack(ModItems.WINE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.HONEY.get());
                        output.accept(ModItems.CHEESE.get());
                        output.accept(ModItems.GOAT_CHEESE.get());
                        output.accept(ModItems.WINE.get());
                    }).build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
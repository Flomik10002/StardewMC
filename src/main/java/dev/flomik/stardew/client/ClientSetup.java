package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.renderer.BigChestRenderer;
import dev.flomik.stardew.client.renderer.BigStoneChestRenderer;
import dev.flomik.stardew.client.renderer.ChestRenderer;
import dev.flomik.stardew.client.renderer.StoneChestRenderer;
import dev.flomik.stardew.client.screen.BigChestScreen;
import dev.flomik.stardew.client.screen.ChestScreen;
import dev.flomik.stardew.common.module.machinery.menu.ModBigChestMenu;
import dev.flomik.stardew.common.module.machinery.menu.ModChestMenu;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.registry.ModMenuTypes;
import dev.flomik.stardew.common.registry.framework.RendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import dev.flomik.stardew.common.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        RendererRegistry.registerAll(event);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChestRenderer.LAYER_LOCATION, ChestRenderer::createBodyLayer);
        event.registerLayerDefinition(BigChestRenderer.LAYER_LOCATION, BigChestRenderer::createBodyLayer);
        event.registerLayerDefinition(StoneChestRenderer.LAYER_LOCATION, StoneChestRenderer::createBodyLayer);
        event.registerLayerDefinition(BigStoneChestRenderer.LAYER_LOCATION, BigStoneChestRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FARMLAND.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.OIL_MAKER.get(), RenderType.translucent());

            ItemProperties.register(ModItems.EGG.get(), new ResourceLocation(StardewMod.MODID, "variant"), (stack, level, entity, seed) ->
                    stack.hasTag() && stack.getTag().getInt("variant") == 1 ? 1.0F : 0.0F);

            ItemProperties.register(ModItems.LARGE_EGG.get(), new ResourceLocation(StardewMod.MODID, "variant"), (stack, level, entity, seed) ->
                    stack.hasTag() && stack.getTag().getInt("variant") == 1 ? 1.0F : 0.0F);
        });

        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.CHEST_MENU.get(),
                    (ModChestMenu menu, Inventory inv, Component title) -> new ChestScreen<>(menu, inv, title));

            MenuScreens.register(ModMenuTypes.BIG_CHEST_MENU.get(),
                    (ModBigChestMenu menu, Inventory inv, Component title) -> new BigChestScreen(menu, inv, title));
        });

    }
}
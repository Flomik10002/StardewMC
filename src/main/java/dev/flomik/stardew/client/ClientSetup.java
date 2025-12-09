
package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.renderer.ChestRenderer;
import dev.flomik.stardew.client.screen.ChestScreen;
import dev.flomik.stardew.core.registry.block.base.VisualItemAboveRenderer;
import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.registry.blockentity.ModBlockEntities;
import dev.flomik.stardew.core.registry.menu.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.BEE_HOUSE.get(),
                VisualItemAboveRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.CHEESE_PRESS.get(),
                VisualItemAboveRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.KEG.get(),
                VisualItemAboveRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.CHEST.get(),
                ChestRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ChestRenderer.LAYER_LOCATION, ChestRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FARMLAND.get(), RenderType.translucent());
        });

        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.CHEST_MENU.get(), ChestScreen::new);
        });
    }
}
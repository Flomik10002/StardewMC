
package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.renderer.ChestRenderer;
import dev.flomik.stardew.client.screen.ChestScreen;
import dev.flomik.stardew.common.registry.block.base.VisualItemAboveRenderer;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.registry.ModMenuTypes;
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
                ModBlocks.BEE_HOUSE.getTypeValue(),
                VisualItemAboveRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlocks.CHEESE_PRESS.getTypeValue(),
                VisualItemAboveRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlocks.KEG.getTypeValue(),
                VisualItemAboveRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlocks.CHEST.getTypeValue(),
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

package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.block.base.VisualItemAboveRenderer;
import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.registry.blockentity.ModBlockEntities;
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
    }
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FARMLAND.get(), RenderType.translucent());
        });
    }
}
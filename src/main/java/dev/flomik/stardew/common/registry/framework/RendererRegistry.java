package dev.flomik.stardew.common.registry.framework;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Реестр для автоматической регистрации рендереров блок-энтити.
 */
public class RendererRegistry {
    
    private static final List<RendererEntry<?>> RENDERERS = new ArrayList<>();
    
    /**
     * Регистрирует рендерер для блок-энтити.
     */
    public static <E extends BlockEntity> void register(
            Supplier<BlockEntityType<E>> typeSupplier,
            BlockEntityRendererProvider<E> rendererProvider
    ) {
        RENDERERS.add(new RendererEntry<>(typeSupplier, rendererProvider));
    }
    
    /**
     * Вызывается из ClientSetup для регистрации всех рендереров.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerAll(EntityRenderersEvent.RegisterRenderers event) {
        for (RendererEntry<?> entry : RENDERERS) {
            event.registerBlockEntityRenderer(
                    (BlockEntityType) entry.typeSupplier.get(),
                    (BlockEntityRendererProvider) entry.rendererProvider
            );
        }
    }
    
    private record RendererEntry<E extends BlockEntity>(
            Supplier<BlockEntityType<E>> typeSupplier,
            BlockEntityRendererProvider<E> rendererProvider
    ) {}
}


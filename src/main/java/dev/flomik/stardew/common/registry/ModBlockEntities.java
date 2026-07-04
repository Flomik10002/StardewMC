package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.common.module.shipping.ShippingBinBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

/**
 * Дополнительные регистрации BlockEntity, которые не проходят через BlockBuilder.
 */
public class ModBlockEntities {

    public static final RegistryObject<BlockEntityType<ShippingBinBlockEntity>> SHIPPING_BIN =
            StardewRegistry.BLOCK_ENTITIES.register("shipping_bin", () ->
                    BlockEntityType.Builder.of(ShippingBinBlockEntity::new, ModBlocks.SHIPPING_BIN.get())
                            .build(null));

    public static void load() {}
}


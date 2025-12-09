package dev.flomik.stardew.core.registry.blockentity;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.registry.block.craftables.blockentity.BlockEntityBeeHouse;
import dev.flomik.stardew.core.registry.block.craftables.blockentity.BlockEntityCheesePress;
import dev.flomik.stardew.core.registry.block.craftables.blockentity.BlockEntityChest;
import dev.flomik.stardew.core.registry.block.craftables.blockentity.BlockEntityKeg;
import dev.flomik.stardew.core.crop.blockentity.CropBlockEntity;
import dev.flomik.stardew.core.registry.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, StardewMod.MODID);

    public static final RegistryObject<BlockEntityType<FarmlandBlockEntity>> FARMLAND =
            BLOCK_ENTITIES.register("farmland", () ->
                    BlockEntityType.Builder.of(FarmlandBlockEntity::new, ModBlocks.FARMLAND.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<CropBlockEntity>> CROP =
            BLOCK_ENTITIES.register("crop", () ->
                    BlockEntityType.Builder.of(CropBlockEntity::new, ModBlocks.CROP.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<BlockEntityBeeHouse>> BEE_HOUSE =
            BLOCK_ENTITIES.register("bee_house", () ->
                    BlockEntityType.Builder.of(BlockEntityBeeHouse::new, ModBlocks.BEE_HOUSE.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<BlockEntityKeg>> KEG =
            BLOCK_ENTITIES.register("keg", () ->
                    BlockEntityType.Builder.of(BlockEntityKeg::new, ModBlocks.KEG.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<BlockEntityCheesePress>> CHEESE_PRESS =
            BLOCK_ENTITIES.register("cheese_press", () ->
                    BlockEntityType.Builder.of(BlockEntityCheesePress::new, ModBlocks.CHEESE_PRESS.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<BlockEntityChest>> CHEST =
            BLOCK_ENTITIES.register("chest", () ->
                    BlockEntityType.Builder.of(BlockEntityChest::new, ModBlocks.CHEST.get()).build(null)
            );

    public static void register() {
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.client.renderer.ChestRenderer;
import dev.flomik.stardew.client.renderer.VisualItemAboveRenderer;
import dev.flomik.stardew.common.module.craftables.block.*;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityBeeHouse;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityCheesePress;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityChest;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityKeg;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.common.registry.framework.BlockBuilder;
import dev.flomik.stardew.common.module.nature.block.BlockDirt;
import dev.flomik.stardew.common.module.farming.block.BlockFarmland;
import dev.flomik.stardew.common.module.nature.block.BlockGrassSurface;
import dev.flomik.stardew.common.registry.framework.BlockEntry;
import dev.flomik.stardew.common.registry.framework.datagen.ModelPresets;
import dev.flomik.stardew.common.module.farming.crop.block.BlockCrop;
import dev.flomik.stardew.common.module.farming.crop.blockentity.CropBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;

import static dev.flomik.stardew.common.registry.framework.BlockPresets.*;

public class ModBlocks {

    public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder.create("keg", BlockKeg::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityKeg::new)
            .renderer(VisualItemAboveRenderer::new)
            .item()
            .tab(ModTabs.CRAFTABLES)
            .model(ModelPresets.simple())
            .register();

    public static final BlockEntry<BlockChest, BlockEntityChest> CHEST = BlockBuilder.create("chest", BlockChest::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityChest::new)
            .renderer(ChestRenderer::new)
            .item()
            .tab(ModTabs.CRAFTABLES)
            .model(ModelPresets.simple())
            .register();

    public static final BlockEntry<BlockBeeHouse, BlockEntityBeeHouse> BEE_HOUSE = BlockBuilder.create("bee_house", BlockBeeHouse::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityBeeHouse::new)
            .renderer(VisualItemAboveRenderer::new)
            .item()
            .tab(ModTabs.CRAFTABLES)
            .model(ModelPresets.simple())
            .register();

    public static final BlockEntry<BlockCheesePress, BlockEntityCheesePress> CHEESE_PRESS = BlockBuilder.create("cheese_press", BlockCheesePress::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityCheesePress::new)
            .renderer(VisualItemAboveRenderer::new)
            .item()
            .tab(ModTabs.CRAFTABLES)
            .model(ModelPresets.simple())
            .register();

    public static final BlockEntry<BlockFarmland, FarmlandBlockEntity> FARMLAND = BlockBuilder.create("farmland", BlockFarmland::new)
            .transform(copy(Blocks.DIRT))
            .seasonal(true)
            .blockEntity(FarmlandBlockEntity::new)
            .item()
            .tab(ModTabs.BLOCK)
            .model(ModelPresets.simple())
            .register();

    public static final RegistryObject<BlockDirt> DIRT = BlockBuilder.create("dirt", BlockDirt::new)
            .transform(copy(Blocks.DIRT))
            .seasonal(true)
            .item()
            .tab(ModTabs.BLOCK)
            .register();

    public static final RegistryObject<BlockGrassSurface> GRASS = BlockBuilder.create("grass", BlockGrassSurface::new)
            .transform(copy(Blocks.GRASS_BLOCK))
            .seasonal(true)
            .item()
            .tab(ModTabs.BLOCK)
            .register();

    public static final RegistryObject<Block> GRASS_FULL = BlockBuilder.create("grass_full", Block::new)
            .transform(copy(Blocks.GRASS_BLOCK))
            .seasonal(true)
            .item()
            .tab(ModTabs.BLOCK)
            .register();

    public static final BlockEntry<BlockCrop, CropBlockEntity> CROP = BlockBuilder.create("crop", BlockCrop::new)
            .transform(crop())
            .blockEntity(CropBlockEntity::new)
            .noItem()
            .register();

    public static void load() {
    }
}

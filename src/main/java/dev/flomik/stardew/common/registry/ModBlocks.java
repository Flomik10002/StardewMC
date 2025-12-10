package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.common.registry.block.craftables.*;
import dev.flomik.stardew.common.registry.block.craftables.blockentity.BlockEntityBeeHouse;
import dev.flomik.stardew.common.registry.block.craftables.blockentity.BlockEntityCheesePress;
import dev.flomik.stardew.common.registry.block.craftables.blockentity.BlockEntityChest;
import dev.flomik.stardew.common.registry.block.craftables.blockentity.BlockEntityKeg;
import dev.flomik.stardew.common.registry.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.common.registry.framework.BlockBuilder;
import dev.flomik.stardew.common.registry.block.surface.BlockDirt;
import dev.flomik.stardew.common.registry.block.surface.BlockFarmland;
import dev.flomik.stardew.common.registry.block.surface.BlockGrassSurface;
import dev.flomik.stardew.common.registry.framework.BlockEntry;
import dev.flomik.stardew.core.crop.block.BlockCrop;
import dev.flomik.stardew.core.crop.blockentity.CropBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;

import static dev.flomik.stardew.common.registry.framework.BlockPresets.*;

public class ModBlocks {

    public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder.create("keg", BlockKeg::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityKeg::new)
            .item().tab(ModTabs.CRAFTABLES)
            .register();

    public static final BlockEntry<BlockChest, BlockEntityChest> CHEST = BlockBuilder.create("chest", BlockChest::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityChest::new)
            .item().tab(ModTabs.CRAFTABLES)
            .register();

    public static final BlockEntry<BlockBeeHouse, BlockEntityBeeHouse> BEE_HOUSE = BlockBuilder.create("bee_house", BlockBeeHouse::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityBeeHouse::new)
            .item().tab(ModTabs.CRAFTABLES)
            .register();

    public static final BlockEntry<BlockCheesePress, BlockEntityCheesePress> CHEESE_PRESS = BlockBuilder.create("cheese_press", BlockCheesePress::new)
            .transform(woodMachine())
            .blockEntity(BlockEntityCheesePress::new)
            .item().tab(ModTabs.CRAFTABLES)
            .register();

    public static final BlockEntry<BlockFarmland, FarmlandBlockEntity> FARMLAND = BlockBuilder.create("farmland", BlockFarmland::new)
            .transform(copy(Blocks.DIRT))
            .seasonal(true)
            .blockEntity(FarmlandBlockEntity::new)
            .item().tab(ModTabs.BLOCK)
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

    public static void load() {}
}

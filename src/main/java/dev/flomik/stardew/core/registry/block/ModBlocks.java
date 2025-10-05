package dev.flomik.stardew.core.registry.block;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.block.craftables.BlockBeehive;
import dev.flomik.stardew.core.block.craftables.BlockCheesePress;
import dev.flomik.stardew.core.block.craftables.BlockKeg;
import dev.flomik.stardew.core.crop.block.BlockCrop;
import dev.flomik.stardew.core.registry.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, StardewMod.MODID);

    public static final RegistryObject<Block> CROP = registerBlock("crop", BlockCrop::new);

    public static final RegistryObject<Block> FARMLAND = registerBlock("farmland", BlockFarmland::new);


    public static final RegistryObject<Block> CHEESE_PRESS = registerBlock("cheese_press",
            () -> new BlockCheesePress(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0f)
                    .sound(SoundType.WOOD)
                    .dynamicShape().noOcclusion()
                    .pushReaction(PushReaction.NORMAL)));

    public static final RegistryObject<Block> KEG = registerBlock("keg",
            () -> new BlockKeg(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0f)
                    .sound(SoundType.WOOD)
                    .dynamicShape().noOcclusion()
                    .pushReaction(PushReaction.NORMAL)));

    public static final RegistryObject<Block> BEEHIVE = registerBlock("beehive",
            () -> new BlockBeehive(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3.0f)
                    .sound(SoundType.WOOD)
                    .dynamicShape().noOcclusion()
                    .pushReaction(PushReaction.NORMAL)));

    public static final RegistryObject<Block> DIRT = registerBlock("dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(3.0f)
                    .sound(SoundType.GRAVEL)
                    .pushReaction(PushReaction.NORMAL)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, java.util.function.Supplier<T> block) {
        RegistryObject<T> blockRegistry = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () ->
                new BlockItem(blockRegistry.get(), new Item.Properties()));
        return blockRegistry;
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}

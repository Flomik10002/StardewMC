package dev.flomik.stardew.core.registry.block;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.registry.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, StardewMod.MODID);

    public static final RegistryObject<Block> FARMLAND = registerBlock("farmland", BlockFarmland::new);

    public static final RegistryObject<Block> DIRT = registerBlock("dirt",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
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

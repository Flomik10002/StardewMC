package dev.flomik.stardew.common.registry.framework;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import java.util.ArrayList;
import java.util.List;

public class SeasonalRegistry {
    private static final List<RegistryObject<? extends Block>> SEASONAL_BLOCKS = new ArrayList<>();

    public static void register(RegistryObject<? extends Block> block) {
        SEASONAL_BLOCKS.add(block);
    }

    public static List<RegistryObject<? extends Block>> getBlocks() {
        return SEASONAL_BLOCKS;
    }
}
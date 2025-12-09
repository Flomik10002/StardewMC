package dev.flomik.stardew.core.registry.block.surface;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockGrassFull extends Block  {

    public BlockGrassFull() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .strength(3.0f)
                .sound(SoundType.GRASS)
        );
    }
}
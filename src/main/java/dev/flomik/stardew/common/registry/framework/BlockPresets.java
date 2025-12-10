package dev.flomik.stardew.common.registry.framework;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.UnaryOperator;

public class BlockPresets {

    public static <B extends BlockBuilder<?>> UnaryOperator<B> woodMachine() {
        return builder -> (B) builder.properties(p -> p
                .mapColor(MapColor.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion()
        );
    }

    public static <B extends BlockBuilder<?>> UnaryOperator<B> crop() {
        return builder -> (B) builder.properties(p -> p
                .mapColor(MapColor.PLANT)
                .strength(2.5F)
                .sound(SoundType.CROP)
                .noOcclusion().instabreak()
        );
    }

    public static <B extends BlockBuilder<?>> UnaryOperator<B> copy(BlockBehaviour block) {
        return builder -> (B) builder.initialProperties(() -> BlockBehaviour.Properties.copy(block));
    }
}
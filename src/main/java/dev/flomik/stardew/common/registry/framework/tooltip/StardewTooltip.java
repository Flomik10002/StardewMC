package dev.flomik.stardew.common.registry.framework.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface StardewTooltip {
    void append(ItemStack stack, @Nullable Level level, List<Component> tooltip);
}
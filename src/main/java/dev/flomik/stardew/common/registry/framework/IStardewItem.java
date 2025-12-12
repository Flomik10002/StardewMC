package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.registry.framework.tooltip.StardewTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IStardewItem {
    void setTooltips(List<StardewTooltip> tooltips);

    List<StardewTooltip> getTooltips();

    default void appendStardewTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        if (getTooltips() != null) {
            for (StardewTooltip logic : getTooltips()) {
                logic.append(stack, level, tooltip);
            }
        }
    }
}
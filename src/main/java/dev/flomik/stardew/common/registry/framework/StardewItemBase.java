package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.registry.framework.tooltip.StardewTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StardewItemBase extends Item implements IStardewItem {

    private List<StardewTooltip> tooltips;

    public StardewItemBase(Properties properties) {
        super(properties);
    }

    @Override
    public void setTooltips(List<StardewTooltip> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public List<StardewTooltip> getTooltips() {
        return tooltips;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        appendStardewTooltips(stack, level, tooltipComponents);
    }
}
package dev.flomik.stardew.common.registry.framework.tooltip;

import dev.flomik.stardew.common.module.tools.IPatternTool;
import dev.flomik.stardew.common.module.tools.PatternType;
import dev.flomik.stardew.common.module.tools.ToolEnchantment;
import dev.flomik.stardew.common.module.tools.item.ToolWateringCan;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TooltipPresets {


    public static StardewTooltip description(String translationKey) {
        return (stack, level, tooltip) ->
                tooltip.add(Component.translatable(translationKey).withStyle(ChatFormatting.DARK_GRAY));
    }

    public static StardewTooltip category(ItemCategory category) {
        return (stack, level, tooltip) ->
                tooltip.add(Component.translatable(category.getTranslationKey())
                        .withStyle(category.getColor()));
    }

    public static StardewTooltip tool(String descKey) {
        return (stack, level, tooltip) -> {
            tooltip.add(Component.translatable(ItemCategory.TOOL.getTranslationKey())
                    .withStyle(ItemCategory.TOOL.getColor()));
            tooltip.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY));
        };
    }

    public static StardewTooltip patternInfo() {
        return (stack, level, tooltip) -> {
            if (stack.getItem() instanceof IPatternTool tool) {
                PatternType current = tool.getCurrentPattern(stack);
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Pattern: " + current.getDisplayName())
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Shift + Right Click to change pattern")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        };
    }

    public static StardewTooltip enchant(ToolEnchantment enchant) {
        return (stack, level, tooltip) -> {
            if (stack.hasTag() && stack.getTag().getBoolean(enchant.getNbtKey())) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal(enchant.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        };
    }

    public static StardewTooltip price(int basePrice) {
        return (stack, level, tooltip) -> {
            int totalValue = basePrice * stack.getCount();
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("⛀⛁⛂⛃ " + totalValue).withStyle(ChatFormatting.GOLD));
        };
    }
}
package dev.flomik.stardew.common.registry.framework.tooltip;

import net.minecraft.ChatFormatting;

public enum ItemCategory {
    TOOL("tooltip.stardew.category.tool",ChatFormatting.GRAY),

    RESOURCE("tooltip.stardew.category.resource", ChatFormatting.GOLD);

    private final String translationKey;
    private final ChatFormatting color;

    ItemCategory(String translationKey, ChatFormatting color) {
        this.translationKey = translationKey;
        this.color = color;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public ChatFormatting getColor() {
        return color;
    }
}

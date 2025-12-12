package dev.flomik.stardew.common.registry.framework.tooltip;

import net.minecraft.ChatFormatting;

public enum ItemCategory {
    TOOL("tooltip.stardew.category.tool",ChatFormatting.GRAY),

    ARTISAN_GOODS("tooltip.stardew.category.artisan_goods", ChatFormatting.DARK_AQUA),

    COOKING("tooltip.stardew.category.cooking", ChatFormatting.DARK_RED),

    ANIMAL_PRODUCT("tooltip.stardew.category.animal_product", ChatFormatting.RED);

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

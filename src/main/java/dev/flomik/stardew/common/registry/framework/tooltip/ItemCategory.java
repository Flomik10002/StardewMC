package dev.flomik.stardew.common.registry.framework.tooltip;

import net.minecraft.network.chat.TextColor;

public enum ItemCategory {
    TOOL("tooltip.stardew.category.tool", 0x2f4f4f),  // #2f4f4f (тёмно-серый)

    ARTISAN_GOODS("tooltip.stardew.category.artisan_goods", 0x009b6f),  // #009b6f (зелёный)

    COOKING("tooltip.stardew.category.cooking", 0x8B0000),  // #8B0000 (тёмно-красный)

    ANIMAL_PRODUCT("tooltip.stardew.category.animal_product", 0xff0064);  // #ff0064 (розовый/малиновый)

    private final String translationKey;
    private final TextColor color;

    ItemCategory(String translationKey, int rgb) {
        this.translationKey = translationKey;
        this.color = TextColor.fromRgb(rgb);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public TextColor getColor() {
        return color;
    }
}

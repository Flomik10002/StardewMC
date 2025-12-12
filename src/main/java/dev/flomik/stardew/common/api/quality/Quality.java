package dev.flomik.stardew.common.api.quality;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

public enum Quality {
    NORMAL(0, 1.0f, ChatFormatting.WHITE, ""),
    SILVER(1, 1.25f, ChatFormatting.GRAY, "★"),
    GOLD(2, 1.5f, ChatFormatting.GOLD, "★"),
    IRIDIUM(3, 2.0f, ChatFormatting.LIGHT_PURPLE, "★");

    private final int id;
    private final float priceMultiplier;
    private final ChatFormatting color;
    private final String icon;

    Quality(int id, float priceMultiplier, ChatFormatting color, String icon) {
        this.id = id;
        this.priceMultiplier = priceMultiplier;
        this.color = color;
        this.icon = icon;
    }

    public static Quality get(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("StardewQuality")) return NORMAL;
        int id = stack.getTag().getInt("StardewQuality");
        return values()[Math.min(Math.max(id, 0), values().length - 1)];
    }

    public static void set(ItemStack stack, Quality quality) {
        if (quality == NORMAL) {
            if (stack.hasTag()) stack.getTag().remove("StardewQuality");
        } else {
            stack.getOrCreateTag().putInt("StardewQuality", quality.ordinal());
        }
    }

    public float getPriceMultiplier() { return priceMultiplier; }
    public ChatFormatting getColor() { return color; }
    public String getIcon() { return icon; }
}
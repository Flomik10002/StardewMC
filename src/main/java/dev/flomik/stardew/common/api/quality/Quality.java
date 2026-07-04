package dev.flomik.stardew.common.api.quality;

import dev.flomik.stardew.common.registry.ModIcons;
import net.minecraft.world.item.ItemStack;

/**
 * Система качества предметов Stardew Valley.
 * 
 * Quality Values (как в Stardew):
 * - NORMAL: 0
 * - SILVER: 1
 * - GOLD: 2
 * - IRIDIUM: 4
 * 
 * Формулы:
 * - Price: basePrice × (1 + quality × 0.25)
 * - Stamina: ceil(edibility × 2.5) + (quality × edibility)
 * - Health: floor(stamina × 0.45)
 */
public enum Quality {
    NORMAL(0, ""),
    SILVER(1, ModIcons.STAR_SILVER),
    GOLD(2, ModIcons.STAR_GOLD),
    IRIDIUM(4, ModIcons.STAR_IRIDIUM);

    private final int qualityValue;
    private final String icon;

    Quality(int qualityValue, String icon) {
        this.qualityValue = qualityValue;
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

    public int getQualityValue() { return qualityValue; }
    public String getIcon() { return icon; }
    
    /**
     * Вычисляет итоговую цену на основе базовой цены и качества.
     * Formula: price = basePrice × (1 + quality × 0.25)
     */
    public int calculatePrice(int basePrice) {
        return (int)(basePrice * (1f + qualityValue * 0.25f));
    }
    
    /**
     * Вычисляет восполняемую стамину на основе edibility и качества.
     * Formula: stamina = ceil(edibility × 2.5) + (quality × edibility)
     */
    public float calculateStamina(int edibility) {
        return (float)Math.ceil(edibility * 2.5) + (qualityValue * edibility);
    }
    
    /**
     * Вычисляет восполняемое здоровье на основе стамины.
     * Formula: health = floor(stamina × 0.45)
     */
    public float calculateHealth(float stamina) {
        return (float)Math.floor(stamina * 0.45);
    }
}
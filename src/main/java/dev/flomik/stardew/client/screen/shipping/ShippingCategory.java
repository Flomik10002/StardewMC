package dev.flomik.stardew.client.screen.shipping;

import dev.flomik.stardew.common.registry.framework.tooltip.ItemCategory;
import net.minecraft.world.item.ItemStack;

/**
 * Категории для ShippingMenu как в Stardew Valley.
 * Каждая категория имеет свой звук при появлении.
 */
public enum ShippingCategory {
    FARMING(0, "shipping.stardew.category.farming", "item.pickup"), // Crops, animal products
    FORAGING(1, "shipping.stardew.category.foraging", "block.grass.break"), // Foraged items
    FISHING(2, "shipping.stardew.category.fishing", "entity.fish.swim"), // Fish, roe
    MINING(3, "shipping.stardew.category.mining", "block.stone.break"), // Gems, ores
    OTHER(4, "shipping.stardew.category.other", "entity.experience_orb.pickup"), // Everything else
    TOTAL(5, "shipping.stardew.category.total", "entity.player.levelup"); // Sum of all

    private final int index;
    private final String translationKey;
    private final String sound;

    ShippingCategory(int index, String translationKey, String sound) {
        this.index = index;
        this.translationKey = translationKey;
        this.sound = sound;
    }

    public int getIndex() {
        return index;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getSound() {
        return sound;
    }

    /**
     * Определяет категорию предмета на основе его ItemCategory или типа.
     */
    public static ShippingCategory categorize(ItemStack stack) {
        if (stack.isEmpty()) return OTHER;

        // Проверяем наш ItemCategory если есть
        if (stack.getItem() instanceof dev.flomik.stardew.common.registry.framework.IStardewItem stardewItem) {
            var tooltips = stardewItem.getTooltips();
            if (tooltips != null) {
                for (var tooltip : tooltips) {
                    if (tooltip instanceof dev.flomik.stardew.common.registry.framework.tooltip.TooltipPresets.CategoryTooltip catTooltip) {
                        ItemCategory cat = catTooltip.getCategory();
                        return switch (cat) {
                            case ANIMAL_PRODUCT -> FARMING;
                            case ARTISAN_GOODS -> FARMING;
                            case TOOL -> OTHER;
                            default -> OTHER;
                        };
                    }
                }
            }
        }

        // По умолчанию - Other
        return OTHER;
    }

    public static ShippingCategory fromIndex(int index) {
        for (ShippingCategory cat : values()) {
            if (cat.index == index) return cat;
        }
        return OTHER;
    }
}


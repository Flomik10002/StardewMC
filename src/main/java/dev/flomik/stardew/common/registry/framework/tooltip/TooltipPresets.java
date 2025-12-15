package dev.flomik.stardew.common.registry.framework.tooltip;

import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.module.tools.IPatternTool;
import dev.flomik.stardew.common.module.tools.PatternType;
import dev.flomik.stardew.common.module.tools.ToolEnchantment;
import dev.flomik.stardew.common.registry.ModIcons;
import dev.flomik.stardew.common.registry.framework.StardewFoodItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class TooltipPresets {

    /**
     * Максимальная ширина строки описания в символах.
     * Если строка длиннее, она будет разбита на несколько строк.
     */
    private static final int MAX_DESCRIPTION_WIDTH = 32;

    /**
     * Умное описание с автоматическим переносом длинных строк.
     * Разбивает текст по словам, не разрывая их посередине.
     */
    public static StardewTooltip description(String translationKey) {
        return (stack, level, tooltip) -> {
            String text = Component.translatable(translationKey).getString();
            List<String> wrappedLines = wrapText(text, MAX_DESCRIPTION_WIDTH);
            
            for (String line : wrappedLines) {
                tooltip.add(Component.literal(line)
                        .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x331f28))));
            }
        };
    }

    /**
     * Разбивает текст на строки заданной максимальной ширины.
     * Разрывает по пробелам, не разрывая слова посередине.
     *
     * @param text Исходный текст
     * @param maxWidth Максимальная ширина строки в символах
     * @return Список строк
     */
    private static List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Если слово само по себе длиннее maxWidth, добавляем его отдельной строкой
            if (word.length() > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                lines.add(word);
                continue;
            }

            // Проверяем, поместится ли слово в текущую строку
            if (currentLine.length() + word.length() + (currentLine.length() > 0 ? 1 : 0) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Слово не помещается, начинаем новую строку
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            }
        }

        // Добавляем последнюю строку
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public static StardewTooltip category(ItemCategory category) {
        return (stack, level, tooltip) ->
                tooltip.add(Component.translatable(category.getTranslationKey())
                        .withStyle(style -> style.withColor(category.getColor())));
    }
    
    /**
     * Добавляет горизонтальную линию-разделитель.
     * Использует unicode символы для создания визуальной линии.
     */
    public static StardewTooltip separator() {
        return (stack, level, tooltip) -> {
            // Используем тонкую линию (─) или среднюю (━)
            String line = "────────────────────"; // 20 символов
            tooltip.add(Component.literal(line)
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x853605))));
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
                        .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x830fc5))));
            }
        };
    }

    public static StardewTooltip price(int basePrice) {
        return new PriceTooltip(basePrice);
    }

    public static class PriceTooltip implements StardewTooltip {
        private final int basePrice;

        public PriceTooltip(int basePrice) {
            this.basePrice = basePrice;
        }

        @Override
        public void append(ItemStack stack, Level level, List<Component> tooltip) {
            Quality quality = Quality.get(stack);
            int totalValue = quality.calculatePrice(basePrice) * stack.getCount();
            tooltip.add(Component.empty());
            tooltip.add(Component.literal(totalValue + " " + ModIcons.MONEY)
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x221122))));
        }
    }

    public static StardewTooltip foodStats() {
        return (stack, level, tooltip) -> {
            if (stack.getItem() instanceof StardewFoodItem foodItem) {
                int edibility = foodItem.getEdibility();
                
                // Учитываем качество для отображения
                Quality quality = Quality.get(stack);
                float stamina = quality.calculateStamina(edibility);
                
                tooltip.add(Component.empty());
                
                // Если стамина отрицательная, показываем череп вместо иконки энергии
                if (stamina != 0) {
                    String energyIcon = stamina < 0 ? ModIcons.SKULL : ModIcons.ENERGY;
                    int staminaValue = Math.round(Math.abs(stamina));
                    String sign = stamina > 0 ? "+" : "-";
                    tooltip.add(Component.literal(energyIcon + " " + sign + staminaValue + " Energy").withStyle(ChatFormatting.GREEN));
                }
                
                // Если edibility >= 0, показываем здоровье
                if (edibility >= 0) {
                    float health = quality.calculateHealth(stamina);
                if (health > 0) {
                        tooltip.add(Component.literal(ModIcons.HEALTH + " +" + Math.round(health) + " HP").withStyle(ChatFormatting.GREEN));
                    }
                }
            }
        };
    }
}
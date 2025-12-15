package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import dev.flomik.stardew.common.registry.ModIcons;
import dev.flomik.stardew.common.registry.framework.tooltip.StardewTooltip;
import dev.flomik.stardew.common.registry.framework.tooltip.TooltipPresets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class StardewFoodItem extends StardewItemBase {
    private final int edibility;

    public StardewFoodItem(Properties properties, int edibility) {
        super(ensureFood(properties));
        this.edibility = edibility;
    }

    private static Properties ensureFood(Properties properties) {
        // We can't easily check if food is set in Properties (private field), 
        // but we can assume the builder in ModItems will set it or we force a default.
        // For Stardew food, we usually want it to be always edible.
        // Creating a new Properties with food if we could, but Properties is mutable-ish builder.
        // Let's just apply a default food property that allows eating even if full, 
        // to support energy restoration.
        // However, this overrides any custom food properties passed in.
        // Ideally we should trust the passed properties, but the user asked for fields in ModItems
        // which implies specific energy/health config.
        // Let's add a default FoodProperties.
        return properties.food(new FoodProperties.Builder()
                .nutrition(1) // Minimal hunger
                .saturationMod(0.1f)
                .alwaysEat() // Stardew food can be eaten anytime for energy
                .build());
    }

    public int getEdibility() {
        return edibility;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide && entity instanceof Player player) {
            Quality quality = Quality.get(stack);
            
            // Вычисляем стамину и здоровье по формулам Stardew Valley
            float stamina = quality.calculateStamina(edibility);
            
            // Восстанавливаем/убавляем стамину
            if (stamina != 0) {
                player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                    if (stamina > 0) {
                        state.restoreEnergy(stamina);
                    } else {
                        state.consumeEnergy(-stamina); // Отрицательная стамина убавляет энергию
                    }
                });
            }

            // Если edibility >= 0, восстанавливаем здоровье
            // Если edibility < 0, НЕ ТРОГАЕМ здоровье (только стамина уменьшается)
            if (edibility >= 0) {
                float health = quality.calculateHealth(stamina);
                if (health > 0) {
                    player.heal(health);
                }
            }
            
            // Show status message? Stardew does show "+E +H" notifications. 
            // Maybe later.
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // Custom implementation to ensure Energy/Health is shown before Price
        List<StardewTooltip> tooltips = getTooltips();

        if (tooltips != null) {
            // 1. Render everything EXCEPT Price
            for (StardewTooltip t : tooltips) {
                if (!(t instanceof TooltipPresets.PriceTooltip)) {
                    t.append(stack, level, tooltipComponents);
                }
            }
        }

        // 2. Render Food Stats (Energy & Health) - вычисляем по формулам Stardew
        Quality quality = Quality.get(stack);
        float stamina = quality.calculateStamina(edibility);

        tooltipComponents.add(Component.empty());
        
        // Если стамина отрицательная, показываем череп вместо иконки энергии
        if (stamina != 0) {
            String energyIcon = stamina < 0 ? ModIcons.SKULL : ModIcons.ENERGY;
            int staminaValue = Math.round(Math.abs(stamina));
            String sign = stamina > 0 ? "+" : "-";
            tooltipComponents.add(Component.literal(energyIcon + " " + sign + staminaValue + " Energy")
                    .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x221122))));
        }
        
        // Если edibility >= 0, показываем здоровье
        if (edibility >= 0) {
            float health = quality.calculateHealth(stamina);
            if (health > 0) {
                tooltipComponents.add(Component.literal(ModIcons.HEALTH + " +" + Math.round(health) + " Health")
                        .withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(0x221122))));
            }
        }

        // 3. Render Price (if it exists)
        if (tooltips != null) {
            for (StardewTooltip t : tooltips) {
                if (t instanceof TooltipPresets.PriceTooltip) {
                    t.append(stack, level, tooltipComponents);
                }
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        // Uses StardewItemBase's implementation for Quality stars
        return super.getName(stack);
    }
}


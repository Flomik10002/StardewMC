/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.config;

import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Config used for client-side only things, such as how stack numbers are rendered.
 */
@OnlyIn(Dist.CLIENT)
public class ClientConfig
{
    public static final  ForgeConfigSpec                     SPEC;
    public static final  ForgeConfigSpec.BooleanValue        enableNumberShortening;
    private static final Map<String, ChatFormatting>         NUMBER_FORMATTING_COLOURS = new HashMap<>();
    private static final ForgeConfigSpec.ConfigValue<String> numberColour;
    
    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        EnumSet.complementOf(EnumSet.of(
                ChatFormatting.STRIKETHROUGH,
                ChatFormatting.BOLD,
                ChatFormatting.RESET,
                ChatFormatting.UNDERLINE,
                ChatFormatting.ITALIC,
                ChatFormatting.OBFUSCATED
        )).forEach(chatFormatting -> NUMBER_FORMATTING_COLOURS.put(chatFormatting.getName(), chatFormatting));
        
        builder.comment("Client configs");
        enableNumberShortening = builder.comment("Enable number shortening. E.g. 1000000 becomes 1M.")
                                        .define("Enable number shortening", true);
        numberColour = builder.comment("The colour of the exact count tooltip shown on items.",
                                       "Available colours (case insensitive):",
                                       String.join(",\n", NUMBER_FORMATTING_COLOURS.keySet().stream().sorted().toList())
        ).define("Exact count number colour", ChatFormatting.DARK_AQUA.getName(),
                 value -> NUMBER_FORMATTING_COLOURS.containsKey(Objects.requireNonNullElse((String) value,
                                                                                           ""
                 ).toLowerCase())
        );
        
        SPEC = builder.build();
    }
    
    public static ChatFormatting getNumberColour()
    {
        return NUMBER_FORMATTING_COLOURS.get(numberColour.get().toLowerCase());
    }
}


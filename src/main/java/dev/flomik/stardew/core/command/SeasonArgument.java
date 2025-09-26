package dev.flomik.stardew.core.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.flomik.stardew.core.time.Season;

import java.util.Arrays;
import java.util.Collection;

public class SeasonArgument implements ArgumentType<Season> {

    public static SeasonArgument season() {
        return new SeasonArgument();
    }

    public static Season getSeason(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Season.class);
    }

    @Override
    public Season parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString().toUpperCase();
        try {
            return Season.valueOf(input);
        } catch (IllegalArgumentException ex) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList("spring", "summer", "fall", "winter");
    }
}

package dev.flomik.stardew.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;
import dev.flomik.stardew.core.time.ScheduleManager;

public class DateTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("datetest")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("nextday")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            ScheduleManager.forceNextDay(level);
                            ctx.getSource().sendSuccess(() -> Component.literal("§aДень переключен."), false);
                            return 1;
                        }))

                .then(Commands.literal("set")
                        .then(Commands.literal("time")
                                .then(Commands.argument("time", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerLevel level = ctx.getSource().getLevel();
                                            String timeString = StringArgumentType.getString(ctx, "time");

                                            String[] parts = timeString.split(":");
                                            if (parts.length != 2) {
                                                ctx.getSource().sendFailure(Component.literal("§cНеверный формат. Используй HH:MM, например 13:45"));
                                                return 0;
                                            }

                                            int hour, minute;
                                            try {
                                                hour = Integer.parseInt(parts[0].trim());
                                                minute = Integer.parseInt(parts[1].trim());
                                            } catch (NumberFormatException e) {
                                                ctx.getSource().sendFailure(Component.literal("§cЧасы и минуты должны быть числами."));
                                                return 0;
                                            }

                                            if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60) {
                                                ctx.getSource().sendFailure(Component.literal("§cНеверное время. Часы: 0–23, минуты: 0–59."));
                                                return 0;
                                            }

                                            int totalMinutes = ((hour - 6 + 24) % 24) * 60 + minute;
                                            int ticks = totalMinutes * 17;
                                            level.setDayTime(ticks);

                                            ctx.getSource().sendSuccess(() -> Component.literal("§aВремя установлено на: " + String.format("%02d:%02d", hour, minute)), false);
                                            return 1;
                                        })
                                )
                        )

                        .then(Commands.literal("day")
                                .then(Commands.argument("value", IntegerArgumentType.integer(1, 28))
                                        .executes(ctx -> {
                                            ServerLevel level = ctx.getSource().getLevel();
                                            int day = IntegerArgumentType.getInteger(ctx, "value");
                                            StardewDateData date = StardewDateData.get(level);
                                            date.setDay(day);
                                            date.syncTotalDaysFromDate();
                                            date.setDirty();
                                            ctx.getSource().sendSuccess(() -> Component.literal("§aУстановлен день: " + day), false);
                                            return 1;
                                        })))

                        .then(Commands.literal("season")
                                .then(Commands.argument("value", SeasonArgument.season())
                                        .executes(ctx -> {
                                            ServerLevel level = ctx.getSource().getLevel();
                                            Season season = SeasonArgument.getSeason(ctx, "value");
                                            StardewDateData date = StardewDateData.get(level);
                                            Season previousSeason = date.getSeason();
                                            date.setSeason(season);
                                            date.setDirty();
                                            
//                                            if (previousSeason != season) {
//                                                GrassBlockUpdateSystem.updateAllGrassBlocks(level);
//                                            }
                                            
                                            ctx.getSource().sendSuccess(() -> Component.literal("§aУстановлен сезон: " + season), false);
                                            return 1;
                                        })))
                )
        );
    }
}

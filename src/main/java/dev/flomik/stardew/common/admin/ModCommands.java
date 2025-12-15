package dev.flomik.stardew.common.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.StardewDateData;
import dev.flomik.stardew.common.module.time.StardewTimeUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("stardew")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("time")
                                .then(Commands.literal("get")
                                        .executes(ctx -> getTime(ctx.getSource().getLevel(), ctx.getSource()))
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("season", SeasonArgument.season())
                                                .then(Commands.argument("day", IntegerArgumentType.integer(1, 28))
                                                        .executes(ctx -> setTime(ctx.getSource().getLevel(),
                                                                SeasonArgument.getSeason(ctx, "season"),
                                                                IntegerArgumentType.getInteger(ctx, "day"),
                                                                6)) // Default 6 AM
                                                        .then(Commands.argument("hour", IntegerArgumentType.integer(0, 23))
                                                                .executes(ctx -> setTime(ctx.getSource().getLevel(),
                                                                        SeasonArgument.getSeason(ctx, "season"),
                                                                        IntegerArgumentType.getInteger(ctx, "day"),
                                                                        IntegerArgumentType.getInteger(ctx, "hour")))
                                                        )
                                                )
                                        )
                                )
                        )
        );
    }

    private static int setTime(ServerLevel level, Season season, int day, int hour) {
        StardewDateData data = StardewDateData.get(level);
        data.setDate(season, day);

        // Use StardewTimeUtils to convert hour to ticks
        int timeOfDayTicks = StardewTimeUtils.toTicks(hour, 0);
        long dayStartTicks = data.getTotalDays() * 24000L;
        long targetTime = dayStartTicks + timeOfDayTicks;
        
        level.setDayTime(targetTime);

        String timeString = StardewTimeUtils.formatTicksToClock(targetTime);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(String.format("Время установлено: %s %d, %s", season.name(), day, timeString)),
                false
        );

        return 1;
    }

    private static int getTime(ServerLevel level, CommandSourceStack source) {
        long dayTime = level.getDayTime();
        
        // Use StardewTimeUtils to format time correctly
        String timeString = StardewTimeUtils.formatTicksToClock(dayTime);
        
        source.sendSuccess(() -> Component.literal("§eТекущее время: §f" + timeString), false);

        return 1;
    }
}


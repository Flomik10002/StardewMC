package dev.flomik.stardew.common.admin;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.StardewDateData;
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

        // Calculate vanilla time
        // 0 ticks = 6 AM
        // Stardew day starts at 6 AM.
        
        long dayStartTicks = data.getTotalDays() * 24000L;
        
        // Convert hour (0-23) to ticks offset from 6 AM
        // 6 -> 0
        // 12 -> 6000
        // 18 -> 12000
        // 0 -> 18000
        // 6 -> 24000
        
        int timeOfDayTicks = (hour * 1000) - 6000;
        if (timeOfDayTicks < 0) {
            timeOfDayTicks += 24000;
        }

        long targetTime = dayStartTicks + timeOfDayTicks;
        
        level.setDayTime(targetTime);

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(String.format("Time set to %s %d, %02d:00 (Vanilla: %d)", season.name(), day, hour, targetTime)),
                false
        );

        return 1;
    }
}


package dev.flomik.stardew.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import dev.flomik.stardew.core.time.ScheduleManager;
import dev.flomik.stardew.core.time.StardewTimeUtils;

public class RainTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("raintest")
                .requires(source -> source.hasPermission(2))
                
                .then(Commands.literal("force")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            
                            // Устанавливаем время в 6:00
                            level.setDayTime(StardewTimeUtils.toTicks(6, 0));
                            
                            // Принудительно выполняем утренние процедуры
                            ScheduleManager.forceNextDay(level);
                            
                            ctx.getSource().sendSuccess(() -> 
                                Component.literal("§aПринудительно выполнены утренние процедуры в 6:00"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("test_dry")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            
                            // Тестируем засыхание без дождя
                            level.setDayTime(StardewTimeUtils.toTicks(6, 0));
                            
                            // Принудительно выполняем утренние процедуры без дождя
                            ScheduleManager.forceNextDay(level);
                            
                            ctx.getSource().sendSuccess(() -> 
                                Component.literal("§aТест засыхания почвы выполнен (без дождя)"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("test_rain")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            
                            // Принудительно устанавливаем дождь
                            level.setWeatherParameters(24000, 0, true, false);
                            
                            // Тестируем с дождем
                            level.setDayTime(StardewTimeUtils.toTicks(6, 0));
                            
                            // Принудительно выполняем утренние процедуры с дождем
                            ScheduleManager.forceNextDay(level);
                            
                            ctx.getSource().sendSuccess(() -> 
                                Component.literal("§aТест с дождем выполнен - дождь установлен на весь день"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("force_rain")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            
                            // Принудительно устанавливаем дождь
                            level.setWeatherParameters(24000, 0, true, false);
                            
                            ctx.getSource().sendSuccess(() -> 
                                Component.literal("§aДождь принудительно установлен на весь день"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("stop_rain")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            
                            // Останавливаем дождь
                            level.setWeatherParameters(0, 0, false, false);
                            
                            ctx.getSource().sendSuccess(() -> 
                                Component.literal("§aДождь остановлен"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("weather")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            boolean isRaining = level.isRaining();
                            boolean isThundering = level.isThundering();
                            
                            String weatherStatus = isRaining ? (isThundering ? "§cГроза" : "§bДождь") : "§aЯсно";
                            
                            ctx.getSource().sendSuccess(() -> 
                                Component.literal("§eПогода: " + weatherStatus), false);
                            return 1;
                        }))
                
                .then(Commands.literal("settime")
                        .then(Commands.argument("hour", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 23))
                                .then(Commands.argument("minute", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 59))
                                        .executes(ctx -> {
                                            ServerLevel level = ctx.getSource().getLevel();
                                            int hour = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "hour");
                                            int minute = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "minute");
                                            
                                            level.setDayTime(StardewTimeUtils.toTicks(hour, minute));
                                            
                                            ctx.getSource().sendSuccess(() -> 
                                                Component.literal("§aВремя установлено на " + String.format("%02d:%02d", hour, minute)), false);
                                            return 1;
                                        }))))
                
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    long ticks = level.getDayTime();
                    int hour = StardewTimeUtils.getHour(ticks);
                    int minute = StardewTimeUtils.getMinute(ticks);
                    boolean isRaining = level.isRaining();
                    
                    String weatherStatus = isRaining ? "§b (дождь)" : "§a (ясно)";
                    
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal("§eТекущее время: " + String.format("%02d:%02d", hour, minute) + 
                                        " (тики: " + ticks + ")" + weatherStatus), false);
                    return 1;
                })
        );
    }
}

package dev.flomik.stardew.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import dev.flomik.stardew.core.time.StardewDateData;

public class DateCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("date")
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    StardewDateData date = StardewDateData.get(level);

                    Component text = Component.literal("§eСегодня: §f" +
                            date.getDayOfWeek() + ", " + date.getDay() + " " + date.getSeason());
                    context.getSource().sendSuccess(() -> text, false);
                    return 1;
                })
        );
    }
}

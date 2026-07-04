package dev.flomik.stardew.common.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EnergyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("energy")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                        float current = state.getCurrentEnergy();
                        float max = state.getMaxEnergy();
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                String.format("§eЭнергия: §f%.1f / %.1f", current, max)
                        ), false);
                    });
                    return 1;
                })
                .then(Commands.literal("get")
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrException();
                            player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                float current = state.getCurrentEnergy();
                                float max = state.getMaxEnergy();
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        String.format("§eЭнергия: §f%.1f / %.1f", current, max)
                                ), false);
                            });
                            return 1;
                        })
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                .executes(ctx -> {
                                    float amount = FloatArgumentType.getFloat(ctx, "amount");
                                    var player = ctx.getSource().getPlayerOrException();

                                    player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                        state.setEnergy(amount);
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                String.format("§eЭнергия установлена: §f%.1f", amount)
                                        ), true);
                                    });
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("max")
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrException();
                            player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                float max = state.getMaxEnergy();
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        String.format("§eМаксимальная энергия: §f%.1f", max)
                                ), false);
                            });
                            return 1;
                        })
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                        .executes(ctx -> {
                                            float amount = FloatArgumentType.getFloat(ctx, "amount");
                                            var player = ctx.getSource().getPlayerOrException();

                                            player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                                state.setMaxEnergy(amount);
                                                ctx.getSource().sendSuccess(() -> Component.literal(
                                                        String.format("§eМаксимальная энергия установлена: §f%.1f", amount)
                                                ), true);
                                            });
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}
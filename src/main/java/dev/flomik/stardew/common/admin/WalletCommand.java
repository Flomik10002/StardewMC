package dev.flomik.stardew.common.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WalletCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wallet")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                        long money = state.getMoney();
                        long earnings = state.getTotalEarnings();
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                String.format("§6Золото: §f%d g\n§7Всего заработано: §f%d g", money, earnings)
                        ), false);
                    });
                    return 1;
                })
                .then(Commands.literal("get")
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrException();
                            player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                long money = state.getMoney();
                                ctx.getSource().sendSuccess(() -> Component.literal(
                                        String.format("§6Золото: §f%d g", money)
                                ), false);
                            });
                            return 1;
                        })
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                .executes(ctx -> {
                                    long amount = LongArgumentType.getLong(ctx, "amount");
                                    var player = ctx.getSource().getPlayerOrException();

                                    player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                        state.setMoney(amount);
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                String.format("§6Золото установлено: §f%d g", amount)
                                        ), true);
                                    });
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(ctx -> {
                                    long amount = LongArgumentType.getLong(ctx, "amount");
                                    var player = ctx.getSource().getPlayerOrException();

                                    player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                        state.addMoney(amount);
                                        ctx.getSource().sendSuccess(() -> Component.literal(
                                                String.format("§6Добавлено золота: §f%d g", amount)
                                        ), true);
                                    });
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(ctx -> {
                                    long amount = LongArgumentType.getLong(ctx, "amount");
                                    var player = ctx.getSource().getPlayerOrException();

                                    player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
                                        boolean success = state.trySpendMoney(amount);
                                        if (success) {
                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                    String.format("§6Потрачено золота: §f%d g", amount)
                                            ), true);
                                        } else {
                                            ctx.getSource().sendFailure(Component.literal(
                                                    String.format("§cНедостаточно средств! (Нужно: %d, Есть: %d)", amount, state.getMoney())
                                            ));
                                        }
                                    });
                                    return 1;
                                })
                        )
                )
        );
    }
}


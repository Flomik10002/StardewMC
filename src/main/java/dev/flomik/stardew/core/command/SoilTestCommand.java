package dev.flomik.stardew.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import dev.flomik.stardew.core.crop.runtime.FarmlandTracker;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoilTestCommand {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soiltest")
                .requires(source -> source.hasPermission(2))
                
                .then(Commands.literal("dry_all")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            int driedCount = 0;
                            
                            for (BlockPos pos : FarmlandTracker.all(level)) {
                                var be = level.getBlockEntity(pos);
                                if (be instanceof FarmlandBlockEntity fb) {
                                    fb.dehydrate(); // Принудительно засыхаем
                                    driedCount++;
                                }
                            }

                            int finalDriedCount = driedCount;
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("§aПринудительно засохло " + finalDriedCount + " блоков почвы"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("hydrate_all")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            int hydratedCount = 0;
                            
                            for (BlockPos pos : FarmlandTracker.all(level)) {
                                var be = level.getBlockEntity(pos);
                                if (be instanceof FarmlandBlockEntity fb) {
                                    fb.hydrate(); // Принудительно увлажняем
                                    hydratedCount++;
                                }
                            }

                            int finalHydratedCount = hydratedCount;
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("§aПринудительно увлажнено " + finalHydratedCount + " блоков почвы"), false);
                            return 1;
                        }))
                
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            int totalCount = 0;
                            int hydratedCount = 0;
                            
                            for (BlockPos pos : FarmlandTracker.all(level)) {
                                var be = level.getBlockEntity(pos);
                                if (be instanceof FarmlandBlockEntity fb) {
                                    totalCount++;
                                    if (fb.isHydrated()) {
                                        hydratedCount++;
                                    }
                                }
                            }

                            int finalHydratedCount = hydratedCount;
                            int finalTotalCount = totalCount;
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("§eСтатус почвы: " + finalHydratedCount + "/" + finalTotalCount + " увлажнено"), false);
                            return 1;
                        }))
                
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal("§eКоманды soiltest:\n" +
                                        "§7/soiltest dry_all - засушить всю почву\n" +
                                        "§7/soiltest hydrate_all - увлажнить всю почву\n" +
                                        "§7/soiltest status - показать статус почвы"), false);
                    return 1;
                })
        );
    }
}

package dev.flomik.stardew.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.flomik.stardew.common.registry.block.surface.BlockDirt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RandomizeDirtCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stardew")
                .then(Commands.literal("randomize_dirt")
                        .then(Commands.argument("radius", IntegerArgumentType.integer())
                                .executes(RandomizeDirtCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerLevel level = source.getLevel();
            BlockPos center = BlockPos.containing(source.getPosition());
            int radius = IntegerArgumentType.getInteger(context, "radius");

            RandomSource random = level.getRandom();
            int count = 0;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);

                        if (state.getBlock() instanceof BlockDirt) {
                            if (state.hasProperty(BlockDirt.VARIANT)) {

                                float chance = random.nextFloat();
                                int newVariant;

                                if (chance < 0.45f) {
                                    newVariant = 0;
                                } else if (chance < 0.90f) {
                                    newVariant = 1;
                                } else {
                                    newVariant = 2 + random.nextInt(12);
                                }

                                if (state.getValue(BlockDirt.VARIANT) != newVariant) {
                                    BlockState newState = state.setValue(BlockDirt.VARIANT, newVariant);
                                    level.setBlock(pos, newState, 3);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }

            int finalCount = count;
            source.sendSuccess(() -> Component.literal("Randomized " + finalCount + " dirt blocks in radius " + radius), true);
            return count;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
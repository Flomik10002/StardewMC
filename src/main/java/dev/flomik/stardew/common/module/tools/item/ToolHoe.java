package dev.flomik.stardew.common.module.tools.item;

import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.tools.IPatternTool;
import dev.flomik.stardew.common.module.tools.Pattern;
import dev.flomik.stardew.common.module.tools.PatternType;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.common.registry.ModSounds;
import dev.flomik.stardew.common.module.time.StardewDateData;
import dev.flomik.stardew.common.module.time.Weather;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class ToolHoe extends Item implements IPatternTool {

    private static final String NBT_PATTERN = "Pattern";
    private final int tier;
    private final PatternType maxPattern;

    public ToolHoe(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        this.maxPattern = getPatternForTier(tier);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.stardew.tool_type")
                .withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable("tooltip.stardew.hoe.desc")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());

        PatternType current = getCurrentPattern(stack);
        tooltip.add(Component.literal("Pattern: " + current.getDisplayName())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Right Click to change pattern")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        if (tier >= 4 && stack.hasTag() && stack.getTag().getBoolean("reaching")) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("✰ Reaching")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)); // Magenta
        }
    }

    private static PatternType getPatternForTier(int tier) {
        return switch (tier) {
            case 0 -> PatternType.SINGLE;
            case 1 -> PatternType.THREE;
            case 2 -> PatternType.FIVE;
            case 3 -> PatternType.GRID_3X3;
            case 4 -> PatternType.GRID_6X3;
            default -> PatternType.SINGLE;
        };
    }

    @Override
    public PatternType getMaxPattern() {
        return maxPattern;
    }

    @Override
    public PatternType getCurrentPattern(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(NBT_PATTERN)) {
            PatternType pattern = PatternType.fromString(stack.getTag().getString(NBT_PATTERN));
            PatternType limit = maxPattern;
            if (tier >= 4 && stack.getTag().getBoolean("reaching")) {
                limit = PatternType.GRID_5X5;
            }

            if (pattern.ordinal() > limit.ordinal()) {
                return limit;
            }
            return pattern;
        }
        return PatternType.SINGLE;
    }

    private void setCurrentPattern(ItemStack stack, PatternType pattern) {
        stack.getOrCreateTag().putString(NBT_PATTERN, pattern.name());
    }

    @Override
    public boolean canApplyToBlock(Level level, BlockPos pos, ItemStack stack) {
        BlockState state = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(pos.above());
        if (!aboveState.isAir()) {
            return false;
        }
        return state.is(ModBlocks.DIRT.get()) || state.is(ModBlocks.FARMLAND.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                PatternType current = getCurrentPattern(stack);
                PatternType max = this.maxPattern;

                if (tier >= 4 && stack.hasTag() && stack.getTag().getBoolean("reaching")) {
                    max = PatternType.GRID_5X5;
                }

                PatternType next = current.next(max);
                setCurrentPattern(stack, next);
                
                player.displayClientMessage(
                    Component.literal("Pattern: " + next.getDisplayName())
                        .withStyle(ChatFormatting.GREEN),
                    true
                );
                level.playSound(null, player.blockPosition(), ModSounds.TOOL_CHARGE.get(),
                        SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            return InteractionResultHolder.success(stack);
        }
        
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BlockState targetState = level.getBlockState(clicked);
        if (!targetState.is(ModBlocks.DIRT.get()) && !targetState.is(ModBlocks.FARMLAND.get())) {
            return InteractionResult.PASS;
        }

        boolean isRaining = false;
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            StardewDateData dateData = StardewDateData.get(serverLevel);
            Weather weather = dateData.getTodayWeather();
            isRaining = (weather == Weather.RAIN || weather == Weather.STORM);
        }

        PatternType currentPattern = getCurrentPattern(stack);
        Pattern pattern = currentPattern.getPattern();

        for (BlockPos pos : pattern.getAffectedPositions(level, clicked, context.getHorizontalDirection(), player)) {
            BlockState state = level.getBlockState(pos);
            
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir()) {
                continue;
            }
            
            if (state.is(ModBlocks.DIRT.get())) {
                level.setBlock(pos, ModBlocks.FARMLAND.get().defaultBlockState(), 3);
                float volume = 1.0F / (float)pattern.getAffectedPositions(level, clicked, context.getHorizontalDirection(), player).size();
                level.playSound(null, pos, ModSounds.HOE_TILL.get(), SoundSource.PLAYERS, volume, 1.0F);

                if (level instanceof ServerLevel serverLevel) {
                    spawnTillingParticles(serverLevel, pos);
                }
                
                if (isRaining && !level.isClientSide) {
                    var blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof FarmlandBlockEntity farmland) {
                        farmland.hydrate();
                    }
                }
            }
            else if (state.is(ModBlocks.FARMLAND.get())) {
                float volume = 1.0F / (float)pattern.getAffectedPositions(level, clicked, context.getHorizontalDirection(), player).size();
                level.playSound(null, pos, ModSounds.HOE_TILL.get(), SoundSource.PLAYERS, volume, 1.0F);

                if (isRaining && !level.isClientSide) {
                    var blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof FarmlandBlockEntity farmland) {
                        farmland.hydrate();
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void spawnTillingParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        StardewDateData dateData = StardewDateData.get(level);
        DustParticleOptions dustOptions = getDustParticleOptions(dateData);

        for (int i = 0; i < 8; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.6;
            double velocityY = level.random.nextDouble() * 0.2 + 0.1;
            
            level.sendParticles(
                dustOptions,
                x + offsetX,
                y,
                z + offsetZ,
                1,
                0, velocityY, 0,
                0.1
            );
        }
        
        for (int i = 0; i < 3; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.4;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.4;
            
            level.sendParticles(
                ParticleTypes.POOF,
                x + offsetX,
                y + 0.1,
                z + offsetZ,
                1,
                0, 0.05, 0,
                0.02
            );
        }
    }

    private static @NotNull DustParticleOptions getDustParticleOptions(StardewDateData dateData) {
        var season = dateData.getSeason();

        Vector3f particleColor;

        if (season == Season.WINTER) {
            particleColor = new Vector3f(0.780f, 1.000f, 1.000f);
        } else {
            particleColor = new Vector3f(0.60f, 0.40f, 0.13f);
        }
        DustParticleOptions dustOptions = new DustParticleOptions(particleColor, 1.0f);
        return dustOptions;
    }
}

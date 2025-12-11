package dev.flomik.stardew.common.module.tools.item;

import dev.flomik.stardew.common.module.tools.IPatternTool;
import dev.flomik.stardew.common.module.tools.Pattern;
import dev.flomik.stardew.common.module.tools.PatternType;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.common.registry.ModSounds;
import dev.flomik.stardew.common.registry.framework.StardewItemBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToolWateringCan extends StardewItemBase implements IPatternTool {

    private static final String NBT_PATTERN = "Pattern";
    private final int tier;
    private final int maxWater;
    private final PatternType maxPattern;

    public ToolWateringCan(Properties props, int tier) {
        super(props.durability(getCapacityForTier(tier) + 1));
        this.tier = tier;
        this.maxWater = getCapacityForTier(tier);
        this.maxPattern = getPatternForTier(tier);
    }

    private static int getCapacityForTier(int tier) {
        return switch (tier) {
            case 0 -> 40;
            case 1 -> 55;
            case 2 -> 70;
            case 3 -> 85;
            case 4 -> 100;
            default -> 40;
        };
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
            if (pattern.ordinal() > maxPattern.ordinal()) {
                return maxPattern;
            }
            return pattern;
        }
        return PatternType.SINGLE;
    }

    private void setCurrentPattern(ItemStack stack, PatternType pattern) {
        stack.getOrCreateTag().putString(NBT_PATTERN, pattern.name());
    }

    public int getWater(ItemStack stack) {
        int used = stack.getDamageValue();
        return Math.max(0, maxWater - used);
    }

    public void setWater(ItemStack stack, int amount) {
        int dmg = Math.max(0, maxWater - amount);
        stack.setDamageValue(dmg);
    }

    public boolean isEmpty(ItemStack stack) {
        return getWater(stack) <= 0;
    }

    @Override
    public boolean canApplyToBlock(Level level, BlockPos pos, ItemStack stack) {
        BlockState state = level.getBlockState(pos);
        BlockState aboveState = level.getBlockState(pos.above());
        if (!aboveState.isAir()) {
            return false;
        }
        return state.is(ModBlocks.FARMLAND.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                PatternType current = getCurrentPattern(stack);
                PatternType next = current.next(maxPattern);
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
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (isEmpty(stack)) {
            player.displayClientMessage(Component.literal("Watering Can is empty!"), true);
            return InteractionResult.FAIL;
        }

        PatternType currentPattern = getCurrentPattern(stack);
        Pattern pattern = currentPattern.getPattern();

        int hydratedCount = 0;
        int currentWater = getWater(stack);

        for (BlockPos pos : pattern.getAffectedPositions(level, clicked, ctx.getHorizontalDirection(), player)) {
            if (currentWater <= 0) break;

            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlocks.FARMLAND.get())) continue;

            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir()) {
                continue;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FarmlandBlockEntity farmland) {
                farmland.hydrate();
                hydratedCount++;
                currentWater--;
                
                if (level instanceof ServerLevel serverLevel) {
                    spawnWaterSplashParticles(serverLevel, pos);
                }
            }
        }

        if (hydratedCount > 0) {
            setWater(stack, currentWater);
            level.playSound(null, clicked, ModSounds.WATERING_CAN_USE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return hydratedCount > 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private void spawnWaterSplashParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        
        for (int i = 0; i < 8; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.6;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.6;
            double velocityY = level.random.nextDouble() * 0.2 + 0.1;
            
            level.sendParticles(
                ParticleTypes.SPLASH,
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
                ParticleTypes.FALLING_WATER,
                x + offsetX,
                y + 0.3,
                z + offsetZ,
                1,
                0, 0, 0,
                0
            );
        }
    }
}

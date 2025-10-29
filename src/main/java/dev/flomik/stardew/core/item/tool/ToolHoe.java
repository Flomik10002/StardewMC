package dev.flomik.stardew.core.item.tool;

import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.core.registry.sound.ModSounds;
import dev.flomik.stardew.core.time.StardewDateData;
import dev.flomik.stardew.core.time.Weather;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class ToolHoe extends Item implements IPatternTool {

    private static final String NBT_PATTERN = "Pattern";
    private final PatternType maxPattern;

    public ToolHoe(Properties properties, PatternType maxPattern) {
        super(properties);
        this.maxPattern = maxPattern;
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
                PatternType next = current.next(maxPattern);
                setCurrentPattern(stack, next);
                
                player.displayClientMessage(
                    Component.literal("Pattern: " + next.getDisplayName())
                        .withStyle(ChatFormatting.GREEN),
                    true
                );
                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.get(), 
                    SoundSource.PLAYERS, 0.3F, 1.5F);
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

        // Проверяем погоду (только на сервере)
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
            
            // Проверяем что сверху воздух
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir()) {
                continue; // Пропускаем этот блок если сверху что-то есть
            }
            
            // Обрабатываем землю (dirt) - превращаем в грядку
            if (state.is(ModBlocks.DIRT.get())) {
                level.setBlock(pos, ModBlocks.FARMLAND.get().defaultBlockState(), 3);
                float volume = 1.0F / (float)pattern.getAffectedPositions(level, clicked, context.getHorizontalDirection(), player).size();
                level.playSound(null, pos, ModSounds.HOE_TILL.get(), SoundSource.PLAYERS, volume, 1.0F);

                // Добавляем эффект вспашки земли
                if (level instanceof ServerLevel serverLevel) {
                    spawnTillingParticles(serverLevel, pos);
                }
                
                // Если идёт дождь, делаем грядку сразу мокрой
                if (isRaining && !level.isClientSide) {
                    var blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof FarmlandBlockEntity farmland) {
                        farmland.hydrate();
                    }
                }
            }
            // Обрабатываем грядку (farmland) - "докапываем" (оставляем грядкой)
            else if (state.is(ModBlocks.FARMLAND.get())) {
                // Просто проигрываем звук, блок остаётся грядкой
                float volume = 1.0F / (float)pattern.getAffectedPositions(level, clicked, context.getHorizontalDirection(), player).size();
                level.playSound(null, pos, ModSounds.HOE_TILL.get(), SoundSource.PLAYERS, volume, 1.0F);

                // Если идёт дождь, поливаем грядку
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

    /**
     * Спавнит частицы земли при вспашке/обработке блока
     */
    private void spawnTillingParticles(ServerLevel level, BlockPos pos) {
        // Позиция чуть выше блока
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        
        // Желто-оранжевый цвет для частиц земли (RGB: #ebab22 -> 235, 171, 34 -> нормализованный 0-1)
        Vector3f earthColor = new Vector3f(0.922f, 0.671f, 0.133f);
        DustParticleOptions dustOptions = new DustParticleOptions(earthColor, 1.0f);
        
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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        PatternType current = getCurrentPattern(stack);
        tooltip.add(Component.literal("Pattern: " + current.getDisplayName())
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Right Click to change pattern")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}

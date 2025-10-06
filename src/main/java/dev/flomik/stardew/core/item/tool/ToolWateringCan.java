package dev.flomik.stardew.core.item.tool;

import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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

public class ToolWateringCan extends Item {

    private final int maxWater;
    private final int waterCostPerUse;
    private final WateringPattern pattern;

    public ToolWateringCan(Properties props, int maxWater, int waterCostPerUse, WateringPattern pattern) {
        super(props.durability(maxWater + 1));
        this.maxWater = maxWater;
        this.waterCostPerUse = waterCostPerUse;
        this.pattern = pattern;
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
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clicked = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (isEmpty(stack)) {
            player.displayClientMessage(Component.literal("Watering Can is empty!"), true);
            return InteractionResult.FAIL;
        }

        int hydratedCount = 0;
        int currentWater = getWater(stack);

        for (BlockPos pos : pattern.getAffectedPositions(level, clicked, ctx.getHorizontalDirection(), player)) {
            if (currentWater <= 0) break;

            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlocks.FARMLAND.get())) continue;

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FarmlandBlockEntity farmland) {
                int day = StardewDateData.get((ServerLevel) level).getTotalDays();
                farmland.hydrate(day);
                hydratedCount++;
                currentWater--;
            }
        }

        if (hydratedCount > 0) {
            setWater(stack, getWater(stack) - waterCostPerUse);
            level.playSound(null, clicked, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return hydratedCount > 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Water: " + getWater(stack) + " / " + maxWater));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}

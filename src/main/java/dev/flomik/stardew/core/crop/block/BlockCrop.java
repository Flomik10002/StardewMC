package dev.flomik.stardew.core.crop.block;

import dev.flomik.stardew.core.crop.blockentity.CropBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockCrop extends BushBlock implements EntityBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7); // визуальный диапазон, маппим на фазы

    public BlockCrop() {
        super(Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP));
        this.registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl,pos,st,be) -> {
            if (be instanceof CropBlockEntity cbe) CropBlockEntity.serverTick(lvl, pos, st, cbe);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var be = level.getBlockEntity(pos);
        if (be instanceof CropBlockEntity cropBe) {
            // GRAB — ПКМ рукой; SCYTHE — можно дополнить проверкой на инструмент
            var def = cropBe.def();
            if (def != null && def.harvestMethod == dev.flomik.stardew.core.crop.def.CropDef.HarvestMethod.GRAB) {
                boolean ok = dev.flomik.stardew.core.crop.logic.HarvestHelper.harvest((ServerLevel) level, cropBe, player);
                return ok ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AGE); }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CropBlockEntity(pos, state); }
}

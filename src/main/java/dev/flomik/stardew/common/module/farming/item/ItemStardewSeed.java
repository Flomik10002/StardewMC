// ItemStardewSeed.java — простой сид-айтем, сам знает свой cropId
package dev.flomik.stardew.common.module.farming.item;

import dev.flomik.stardew.common.module.farming.crop.runtime.CropTracker;
import dev.flomik.stardew.common.module.farming.crop.CropRegistry;
import dev.flomik.stardew.common.module.farming.crop.blockentity.CropBlockEntity;
import dev.flomik.stardew.common.module.farming.crop.def.CropDef;
import dev.flomik.stardew.common.module.time.StardewDateData;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemStardewSeed extends Item {
    private final ResourceLocation cropId;

    public ItemStardewSeed(Properties props, ResourceLocation cropId) {
        super(props);
        this.cropId = cropId;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos pos = ctx.getClickedPos();
        BlockPos above = pos.above();

        var state = level.getBlockState(pos);
        if (!state.is(ModBlocks.FARMLAND.get())) return InteractionResult.PASS;
        if (!level.getBlockState(above).isAir()) return InteractionResult.FAIL;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof FarmlandBlockEntity fbe)) return InteractionResult.FAIL;

        CropDef def = CropRegistry.get(cropId);
        if (def == null) return InteractionResult.FAIL;

        // Проверка сезона по SV
        var date = StardewDateData.get((net.minecraft.server.level.ServerLevel) level);
        if (!def.seasons.contains(date.getSeason().name().toLowerCase())) return InteractionResult.FAIL;
        // TODO: проверить trellis/isRaised коллизию

        // Если удобрение качества нельзя вносить после посадки — проверяй заранее (твои флаги before/after в FertilizerType)
        // (логика вноса удобрений у тебя уже есть) :contentReference[oaicite:6]{index=6} :contentReference[oaicite:7]{index=7}

        // ставим блок культуры
        level.setBlock(above, ModBlocks.CROP.get().defaultBlockState(), 3);
        var cropBe = (CropBlockEntity) level.getBlockEntity(above);
        cropBe.init(cropId);
        cropBe.applyPlantingSpeedBonuses();
        CropTracker.onLoad(cropBe);

        ctx.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}

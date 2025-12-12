package dev.flomik.stardew.common.module.farming.crop.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.module.farming.blockentity.FarmlandBlockEntity;
import dev.flomik.stardew.common.module.farming.crop.CropRegistry;
import dev.flomik.stardew.common.module.farming.crop.def.CropDef;
import dev.flomik.stardew.common.module.farming.crop.runtime.CropTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CropBlockEntity extends BlockEntity {
    private ResourceLocation cropId;
    private int currentPhase;
    private int dayOfCurrentPhase;
    private boolean readyToHarvest;
    private int regrowCountdown;
    private boolean loaded = false;

    // per-instance adjusted phases (copy of def.daysInPhase with speed changes applied)
    private java.util.List<Integer> phaseDaysAdjusted = new java.util.ArrayList<>();
    private boolean fullyGrown;

    public CropBlockEntity(BlockPos pos, BlockState state) { super(ModBlocks.CROP.getTypeValue(), pos, state); }

    public void init(ResourceLocation id) {
        this.cropId = id; this.currentPhase = 0; this.dayOfCurrentPhase = 0;
        this.readyToHarvest = false; this.regrowCountdown = 0; this.fullyGrown = false;
        setChanged();
    }

    public CropDef def() { return CropRegistry.get(cropId); }
    public boolean isReady() { return readyToHarvest; }
    public int currentPhase() { return currentPhase; }

    public void growOneDay(boolean watered, boolean applySpeed, float speedMul, boolean paddyBoost) {
        if (readyToHarvest) return;
        var d = def(); if (d == null) return;
        if (phaseDaysAdjusted.isEmpty()) phaseDaysAdjusted = new java.util.ArrayList<>(d.daysInPhase);
        if (d.needsWatering && !watered) return;

        int lastPhaseIndex = phaseDaysAdjusted.size()-1;

        if (!fullyGrown) {
            // increment day within current phase
            int phaseLen = phaseDaysAdjusted.get(Math.min(currentPhase, lastPhaseIndex));
            dayOfCurrentPhase = Math.min(dayOfCurrentPhase + 1, phaseLen);
        } else {
            // regrow countdown (counts down in SV when fullyGrown)
            dayOfCurrentPhase = Math.max(0, dayOfCurrentPhase - 1);
        }

        // advance phase if completed and not at final
        int phaseLenNow = phaseDaysAdjusted.get(Math.min(currentPhase, lastPhaseIndex));
        if (dayOfCurrentPhase >= phaseLenNow && currentPhase < lastPhaseIndex) {
            currentPhase++;
            dayOfCurrentPhase = 0;
        }
        while (currentPhase < lastPhaseIndex && phaseDaysAdjusted.get(currentPhase) <= 0) {
            currentPhase++;
        }

        // mark ready when at final and (not fullyGrown or regrow timer reached 0)
        if ((!fullyGrown || dayOfCurrentPhase <= 0) && currentPhase >= lastPhaseIndex) {
            readyToHarvest = true;
        }
        setChanged();
    }

    public void onHarvestDone() {
        var d = def();
        if (d == null) return;
        if (d.regrowDays < 0) {
            level.removeBlock(worldPosition, false);
        } else {
            readyToHarvest = false;
            fullyGrown = true;
            dayOfCurrentPhase = d.regrowDays; // SV keeps fullyGrown and sets dayOfCurrentPhase to regrowDays
            currentPhase = Math.max(0, (phaseDaysAdjusted.isEmpty()? d.daysInPhase : phaseDaysAdjusted).size() - 1);
            regrowCountdown = d.regrowDays; // kept for backward compat; not primary driver now
            setChanged();
        }
    }

    public void tickRegrowIfNeeded() {
        // no-op: regrow now handled via fullyGrown/dayOfCurrentPhase as in SV
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CropBlockEntity be) {
        if (!be.loaded) {
            CropTracker.onLoad(be);
            be.loaded = true;
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        if (cropId != null) tag.putString("cropId", cropId.toString());
        tag.putInt("phase", currentPhase);
        tag.putInt("daysPhase", dayOfCurrentPhase);
        tag.putBoolean("ready", readyToHarvest);
        tag.putInt("regrowCd", regrowCountdown);
        tag.putBoolean("fullyGrown", fullyGrown);
        if (!phaseDaysAdjusted.isEmpty()) {
            var list = new net.minecraft.nbt.ListTag();
            for (Integer v : phaseDaysAdjusted) list.add(net.minecraft.nbt.IntTag.valueOf(v));
            tag.put("phaseDaysAdjusted", list);
        }
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        cropId = tag.contains("cropId") ? new ResourceLocation(tag.getString("cropId")) : null;
        currentPhase = tag.getInt("phase");
        dayOfCurrentPhase = tag.getInt("daysPhase");
        readyToHarvest = tag.getBoolean("ready");
        regrowCountdown = tag.getInt("regrowCd");
        fullyGrown = tag.getBoolean("fullyGrown");
        phaseDaysAdjusted.clear();
        if (tag.contains("phaseDaysAdjusted")) {
            var list = tag.getList("phaseDaysAdjusted", net.minecraft.nbt.Tag.TAG_INT);
            for (int i=0;i<list.size();i++) phaseDaysAdjusted.add(((net.minecraft.nbt.IntTag)list.get(i)).getAsInt());
        }
    }

    public int phaseCount() {
        var d = def();
        int base = (d == null) ? 0 : d.daysInPhase.size();
        return Math.max(base, phaseDaysAdjusted.isEmpty()? 0 : phaseDaysAdjusted.size());
    }

    public java.util.List<Integer> getAdjustedPhaseDays() {
        if (phaseDaysAdjusted.isEmpty()) {
            var d = def();
            if (d != null) phaseDaysAdjusted = new java.util.ArrayList<>(d.daysInPhase);
        }
        return phaseDaysAdjusted;
    }

    public void applyPlantingSpeedBonuses() {
        var d = def(); if (d == null) return;
        if (phaseDaysAdjusted.isEmpty()) phaseDaysAdjusted = new java.util.ArrayList<>(d.daysInPhase);
        // compute speedIncrease: fertilizer speed + paddy bonus
        float speedIncrease = 0f;
        var below = worldPosition.below();
        var be = level.getBlockEntity(below);
        if (be instanceof FarmlandBlockEntity fb) {
            var fert = fb.getFertilizer();
            if (fert.isSpeedBoost()) speedIncrease += fert.strength; // 0.1 / 0.25 / 0.33
        }
        if (d.isPaddyCrop) {
            // reuse GrowthSystem-like paddy check in a local simplified form
            int r = 3;
            var m = new net.minecraft.core.BlockPos.MutableBlockPos();
            for (int dx=-r; dx<=r; dx++)
                for (int dz=-r; dz<=r; dz++) {
                    m.set(below.getX()+dx, below.getY(), below.getZ()+dz);
                    var fluid = level.getFluidState(m);
                    if (!fluid.isEmpty()) { speedIncrease += 0.25f; dx = r+1; dz = r+1; break; }
                }
        }
        if (speedIncrease <= 0f) return;

        // SV-like: remove ceil(totalDays * speedIncrease) from non-final phases
        int total = 0;
        for (int i=0; i<phaseDaysAdjusted.size()-1; i++) total += phaseDaysAdjusted.get(i);
        int toRemove = (int)Math.ceil(total * speedIncrease);
        int tries = 0;
        while (toRemove > 0 && tries < 3) {
            for (int i=0; i<phaseDaysAdjusted.size(); i++) {
                int v = phaseDaysAdjusted.get(i);
                if ((i > 0 || v > 1) && v != 99999 && v > 0) {
                    phaseDaysAdjusted.set(i, v-1);
                    toRemove--;
                }
                if (toRemove <= 0) break;
            }
            tries++;
        }
        setChanged();
    }
}

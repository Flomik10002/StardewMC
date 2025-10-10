package dev.flomik.stardew.core.crop.blockentity;

import dev.flomik.stardew.core.crop.CropRegistry;
import dev.flomik.stardew.core.crop.def.CropDef;
import dev.flomik.stardew.core.crop.runtime.CropTracker;
import dev.flomik.stardew.core.registry.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CropBlockEntity extends BlockEntity {
    private ResourceLocation cropId;
    private int currentPhase;
    private int daysInCurrentPhase;
    private boolean readyToHarvest;
    private int regrowCountdown;
    private boolean loaded = false;

    public CropBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CROP.get(), pos, state); }

    public void init(ResourceLocation id) {
        this.cropId = id; this.currentPhase = 0; this.daysInCurrentPhase = 0;
        this.readyToHarvest = false; this.regrowCountdown = 0;
        setChanged();
    }

    public CropDef def() { return CropRegistry.get(cropId); }
    public boolean isReady() { return readyToHarvest; }
    public int currentPhase() { return currentPhase; }

    public void growOneDay(boolean watered, boolean applySpeed, float speedMul, boolean paddyBoost) {
        if (readyToHarvest) return;
        var d = def(); if (d == null) return;
        if (d.needsWatering && !watered) return;

        if (d.regrowDays >= 0 && currentPhase == d.daysInPhase.size()-1) { readyToHarvest = true; return; }

        int base = d.daysInPhase.get(currentPhase);
        if (paddyBoost && d.isPaddyCrop) { // SV rice/taro: 1,2,2,3 -> 1,1,1,3
            if (currentPhase == 1 || currentPhase == 2) base = Math.max(1, base-1);
        }
        int eff = base;
        if (applySpeed && speedMul > 0f) eff = Math.max(1, Math.round(base * (1f - speedMul)));

        daysInCurrentPhase++;
        if (daysInCurrentPhase >= eff) {
            daysInCurrentPhase = 0;
            currentPhase++;
            if (currentPhase >= d.daysInPhase.size()) {
                currentPhase = d.daysInPhase.size()-1;
                readyToHarvest = true;
            }
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
            regrowCountdown = d.regrowDays;
            currentPhase = d.daysInPhase.size() - 2; // чуть откатить на предпоследнюю фазу
            daysInCurrentPhase = 0;
            setChanged();
        }
    }

    public void tickRegrowIfNeeded() {
        var d = def(); if (d == null || d.regrowDays < 0) return;
        if (regrowCountdown > 0) {
            regrowCountdown--;
            if (regrowCountdown == 0) {
                readyToHarvest = false;
                daysInCurrentPhase = 0;
            }
            setChanged();
        }
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
        tag.putInt("daysPhase", daysInCurrentPhase);
        tag.putBoolean("ready", readyToHarvest);
        tag.putInt("regrowCd", regrowCountdown);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        cropId = tag.contains("cropId") ? new ResourceLocation(tag.getString("cropId")) : null;
        currentPhase = tag.getInt("phase");
        daysInCurrentPhase = tag.getInt("daysPhase");
        readyToHarvest = tag.getBoolean("ready");
        regrowCountdown = tag.getInt("regrowCd");
    }
}

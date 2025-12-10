package dev.flomik.stardew.common.module.farming.crop;

import net.minecraft.util.StringRepresentable;

public enum FertilizerType implements StringRepresentable {

    // Quality
    BASIC_FERTILIZER(true, false, FertilizerEffect.QUALITY),
    QUALITY_FERTILIZER(true, false, FertilizerEffect.QUALITY),
    DELUXE_FERTILIZER(true, true, FertilizerEffect.QUALITY),

    // Retain
    BASIC_RETAINING_SOIL(true, true, FertilizerEffect.RETAIN_WATER, 0.33f),
    QUALITY_RETAINING_SOIL(true, true, FertilizerEffect.RETAIN_WATER, 0.66f),
    DELUXE_RETAINING_SOIL(true, true, FertilizerEffect.RETAIN_WATER, 1.0f),

    // Speed
    SPEED_GRO(true, true, FertilizerEffect.SPEED_GROWTH, 0.10f),
    DELUXE_SPEED_GRO(true, true, FertilizerEffect.SPEED_GROWTH, 0.25f),
    HYPER_SPEED_GRO(true, true, FertilizerEffect.SPEED_GROWTH, 0.33f),

    // Only for trees
    TREE_FERTILIZER(false, true, FertilizerEffect.TREE_GROWTH),

    NONE(false, false, FertilizerEffect.NONE);

    public final boolean canApplyBeforePlanting;
    public final boolean canApplyAfterPlanting;
    public final FertilizerEffect effect;
    public final float strength;

    FertilizerType(boolean before, boolean after, FertilizerEffect effect) {
        this(before, after, effect, 0f);
    }

    FertilizerType(boolean before, boolean after, FertilizerEffect effect, float strength) {
        this.canApplyBeforePlanting = before;
        this.canApplyAfterPlanting = after;
        this.effect = effect;
        this.strength = strength;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public boolean isRetention() {
        return effect == FertilizerEffect.RETAIN_WATER;
    }

    public boolean isSpeedBoost() {
        return effect == FertilizerEffect.SPEED_GROWTH;
    }

    public boolean isQualityBoost() {
        return effect == FertilizerEffect.QUALITY;
    }

    public boolean isTreeOnly() {
        return effect == FertilizerEffect.TREE_GROWTH;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public enum FertilizerEffect {
        NONE,
        QUALITY,
        RETAIN_WATER,
        SPEED_GROWTH,
        TREE_GROWTH
    }
}

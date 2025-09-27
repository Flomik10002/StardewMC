package dev.flomik.stardew.core.crop.def;

import net.minecraft.resources.ResourceLocation;
import java.util.List;

public final class CropDef {
    public enum HarvestMethod { GRAB, SCYTHE }

    public final ResourceLocation id;          // "stardew:tomato"
    public final List<String> seasons;         // ["summer"]
    public final List<Integer> daysInPhase;    // [1,2,2,3]
    public final int regrowDays;               // -1 если нет регров
    public final boolean isRaised;             // trellis
    public final boolean isPaddyCrop;          // рис/таро
    public final boolean needsWatering;        // fiber = false
    public final ResourceLocation harvestItem; // что дропаем
    public final HarvestMethod harvestMethod;  // GRAB/SCYTHE
    public final int harvestMin;
    public final int harvestMax;
    public final float extraHarvestChance;     // 0..0.9 (геометр.)
    public final int spriteIndex;              // индекс ряда в спрайте

    public CropDef(ResourceLocation id, List<String> seasons, List<Integer> daysInPhase,
                   int regrowDays, boolean isRaised, boolean isPaddyCrop, boolean needsWatering,
                   ResourceLocation harvestItem, HarvestMethod harvestMethod,
                   int harvestMin, int harvestMax, float extraHarvestChance, int spriteIndex) {
        this.id = id; this.seasons = seasons; this.daysInPhase = daysInPhase;
        this.regrowDays = regrowDays; this.isRaised = isRaised; this.isPaddyCrop = isPaddyCrop;
        this.needsWatering = needsWatering; this.harvestItem = harvestItem; this.harvestMethod = harvestMethod;
        this.harvestMin = harvestMin; this.harvestMax = harvestMax; this.extraHarvestChance = extraHarvestChance;
        this.spriteIndex = spriteIndex;
    }
}

package dev.flomik.stardew.core.crop;

import dev.flomik.stardew.core.crop.def.CropDef;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class CropRegistry {
    private static final Map<ResourceLocation, CropDef> BY_ID = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> SEED_TO_CROP = new HashMap<>();

    public static void register(ResourceLocation cropId, CropDef def, ResourceLocation seedItemId) {
        BY_ID.put(cropId, def);
        SEED_TO_CROP.put(seedItemId, cropId);
    }
    public static CropDef get(ResourceLocation id) { return BY_ID.get(id); }
    public static Optional<CropDef> bySeed(ResourceLocation seedId) {
        var cid = SEED_TO_CROP.get(seedId);
        return Optional.ofNullable(cid == null ? null : BY_ID.get(cid));
    }

    public static void bootstrapVanillaLike(String modid) {
        // ТОМАТ: лето, фазы [1,2,2,3], регров 4 дня, extra 0.05
        register(new ResourceLocation(modid, "tomato"),
                new CropDef(
                        new ResourceLocation(modid, "tomato"),
                        List.of("summer"),
                        List.of(1,2,2,3),
                        4, false, false, true,
                        new ResourceLocation(modid, "tomato_item"),
                        CropDef.HarvestMethod.GRAB,
                        1, 1, 0.05f, 0
                ),
                new ResourceLocation(modid, "tomato_seeds")
        );

        // КАПУСТА: весна, [1,2,3,3], без регров, extra 0
        register(new ResourceLocation(modid, "cauliflower"),
                new CropDef(
                        new ResourceLocation(modid, "cauliflower"),
                        List.of("spring"),
                        List.of(1,2,3,3),
                        -1, false, false, true,
                        new ResourceLocation(modid, "cauliflower_item"),
                        CropDef.HarvestMethod.GRAB,
                        1, 1, 0f, 1
                ),
                new ResourceLocation(modid, "cauliflower_seeds")
        );

        // РИС: весна, [1,2,2,3] (рядом с водой ускоряется до [1,1,1,3])
        register(new ResourceLocation(modid, "rice"),
                new CropDef(
                        new ResourceLocation(modid, "rice"),
                        List.of("spring"),
                        List.of(1,2,2,3),
                        -1, false, true, true,
                        new ResourceLocation(modid, "rice_item"),
                        CropDef.HarvestMethod.SCYTHE,
                        1, 1, 0f, 2
                ),
                new ResourceLocation(modid, "rice_shoots") // семена риса
        );
    }
}

package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.model.FarmlandSeasonModel;
import dev.flomik.stardew.client.model.SeasonShapeModel;
import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.registry.block.shape.Shape;
import dev.flomik.stardew.core.registry.block.surface.BlockDirt;
import dev.flomik.stardew.core.time.Season;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModelRegistry {

    private static final Map<Season, Map<Shape, ResourceLocation>> GRASS_LOCATIONS = new HashMap<>();
    private static final Map<Season, Map<Shape, ResourceLocation>> FARM_DRY_LOCATIONS = new HashMap<>();
    private static final Map<Season, Map<Shape, ResourceLocation>> FARM_WET_LOCATIONS = new HashMap<>();
    private static final Map<Season, ResourceLocation[]> DIRT_LOCATIONS = new HashMap<>();

    static {
        for (Season s : Season.values()) {
            GRASS_LOCATIONS.put(s, new HashMap<>());
            FARM_DRY_LOCATIONS.put(s, new HashMap<>());
            FARM_WET_LOCATIONS.put(s, new HashMap<>());

            String sName = s.name().toLowerCase();

            ResourceLocation[] dirtVars = new ResourceLocation[14];
            for (int i = 0; i < 14; i++) {
                dirtVars[i] = new ResourceLocation(StardewMod.MODID, "block/dirt/" + sName + "/dirt_" + i);
            }
            DIRT_LOCATIONS.put(s, dirtVars);

            for (Shape shape : Shape.values()) {
                String shName = shape.name().toLowerCase();

                GRASS_LOCATIONS.get(s).put(shape, new ResourceLocation(StardewMod.MODID, "block/grass/" + sName + "/" + shName));
                FARM_DRY_LOCATIONS.get(s).put(shape, new ResourceLocation(StardewMod.MODID, "block/farmland/" + sName + "/dry_" + shName));
                FARM_WET_LOCATIONS.get(s).put(shape, new ResourceLocation(StardewMod.MODID, "block/farmland/" + sName + "/wet_overlay_" + shName));
            }
        }
    }

    @SubscribeEvent
    public static void registerExtraModels(ModelEvent.RegisterAdditional event) {
        GRASS_LOCATIONS.values().forEach(m -> m.values().forEach(event::register));
        FARM_DRY_LOCATIONS.values().forEach(m -> m.values().forEach(event::register));
        FARM_WET_LOCATIONS.values().forEach(m -> m.values().forEach(event::register));
        DIRT_LOCATIONS.values().forEach(arr -> {
            for (ResourceLocation rl : arr) event.register(rl);
        });
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> registry = event.getModels();

        Map<Season, Map<Shape, BakedModel>> bakedGrass = bakeMap(registry, GRASS_LOCATIONS);
        Map<Season, Map<Shape, BakedModel>> bakedDry = bakeMap(registry, FARM_DRY_LOCATIONS);
        Map<Season, Map<Shape, BakedModel>> bakedWet = bakeMap(registry, FARM_WET_LOCATIONS);

        Map<Season, Map<Shape, BakedModel>>[] bakedDirtVariants = new Map[14];
        for (int i = 0; i < 14; i++) {
            bakedDirtVariants[i] = new HashMap<>();
            for (Season s : Season.values()) {
                BakedModel model = registry.get(DIRT_LOCATIONS.get(s)[i]);
                if (model != null) {
                    Map<Shape, BakedModel> shapeMap = new HashMap<>();
                    shapeMap.put(Shape.SINGLE, model);
                    bakedDirtVariants[i].put(s, shapeMap);
                }
            }
        }


        Map<Season, Map<Shape, BakedModel>> bakedGrassFull = new HashMap<>();
        for (Season s : Season.values()) {
            BakedModel fullModel = registry.get(GRASS_LOCATIONS.get(s).get(Shape.CENTER));

            if (fullModel != null) {
                Map<Shape, BakedModel> shapeMap = new HashMap<>();
                for (Shape sh : Shape.values()) shapeMap.put(sh, fullModel);
                bakedGrassFull.put(s, shapeMap);
            }
        }

        replaceSimpleBlock(registry, ModBlocks.GRASS.get(), bakedGrass);
        replaceSimpleBlock(registry, ModBlocks.GRASS_FULL.get(), bakedGrassFull);
        replaceDirtBlock(registry, ModBlocks.DIRT.get(), bakedDirtVariants);

        ResourceLocation farmRL = ForgeRegistries.BLOCKS.getKey(ModBlocks.FARMLAND.get());
        if (farmRL != null) {
            ModBlocks.FARMLAND.get().getStateDefinition().getPossibleStates().forEach(state -> {
                String variant = BlockModelShaper.statePropertiesToString(state.getValues());
                ModelResourceLocation loc = new ModelResourceLocation(farmRL, variant);
                BakedModel original = registry.get(loc);
                if (original != null) {
                    registry.put(loc, new FarmlandSeasonModel(bakedDry, bakedWet, original));
                }
            });
        }
    }

    private static void replaceSimpleBlock(Map<ResourceLocation, BakedModel> registry, net.minecraft.world.level.block.Block block, Map<Season, Map<Shape, BakedModel>> seasonModels) {
        ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
        if (rl != null) {
            block.getStateDefinition().getPossibleStates().forEach(state -> {
                String variant = BlockModelShaper.statePropertiesToString(state.getValues());
                ModelResourceLocation mrl = new ModelResourceLocation(rl, variant);
                BakedModel original = registry.get(mrl);

                if (original == null && variant.isEmpty()) {
                    mrl = new ModelResourceLocation(rl, "");
                    original = registry.get(mrl);
                }

                if (original != null) {
                    registry.put(mrl, new SeasonShapeModel(seasonModels, original));
                }
            });
        }
    }

    private static void replaceDirtBlock(Map<ResourceLocation, BakedModel> registry, net.minecraft.world.level.block.Block block, Map<Season, Map<Shape, BakedModel>>[] variants) {
        ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
        if (rl != null) {
            block.getStateDefinition().getPossibleStates().forEach(state -> {
                String variantStr = BlockModelShaper.statePropertiesToString(state.getValues());
                ModelResourceLocation mrl = new ModelResourceLocation(rl, variantStr);
                BakedModel original = registry.get(mrl);

                if (original == null && variantStr.isEmpty()) {
                    mrl = new ModelResourceLocation(rl, "");
                    original = registry.get(mrl);
                }

                if (original != null) {
                    int variantIndex = state.getValue(BlockDirt.VARIANT);

                    Map<Season, Map<Shape, BakedModel>> correctModelMap = variants[variantIndex];

                    registry.put(mrl, new SeasonShapeModel(correctModelMap, original));
                }
            });
        }
    }

    private static Map<Season, Map<Shape, BakedModel>> bakeMap(Map<ResourceLocation, BakedModel> registry,
                                                               Map<Season, Map<Shape, ResourceLocation>> source) {
        Map<Season, Map<Shape, BakedModel>> result = new HashMap<>();
        for (Season s : Season.values()) {
            Map<Shape, BakedModel> shapeMap = new HashMap<>();
            for (Shape shape : Shape.values()) {
                BakedModel m = registry.get(source.get(s).get(shape));
                if (m != null) shapeMap.put(shape, m);
            }
            result.put(s, shapeMap);
        }
        return result;
    }
}
package dev.flomik.stardew.client.model;

import dev.flomik.stardew.client.ClientSeasonManager;
import dev.flomik.stardew.common.module.farming.block.BlockFarmland;
import dev.flomik.stardew.common.api.block.Shape;
import dev.flomik.stardew.common.module.time.Season;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FarmlandSeasonModel implements BakedModel {
    private final Map<Season, Map<Shape, BakedModel>> dryModels;
    private final Map<Season, Map<Shape, BakedModel>> wetOverlayModels;
    private final BakedModel defaultModel;

    public FarmlandSeasonModel(Map<Season, Map<Shape, BakedModel>> dry,
                               Map<Season, Map<Shape, BakedModel>> wet,
                               BakedModel defaultModel) {
        this.dryModels = dry;
        this.wetOverlayModels = wet;
        this.defaultModel = defaultModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        Season season = ClientSeasonManager.getSeason();
        List<BakedQuad> quads = new ArrayList<>();

        Shape shape = Shape.SINGLE;
        Shape wetShape = Shape.SINGLE;

        if (state != null) {
            if (state.hasProperty(BlockFarmland.SHAPE)) {
                shape = state.getValue(BlockFarmland.SHAPE);
            }
            if (state.hasProperty(BlockFarmland.WET_SHAPE)) {
                wetShape = state.getValue(BlockFarmland.WET_SHAPE);
            }
        }

        BakedModel dry = dryModels.getOrDefault(season, Collections.emptyMap()).get(shape);
        if (dry != null) quads.addAll(dry.getQuads(state, side, rand, data, renderType));

        if (state != null && state.hasProperty(BlockFarmland.HYDRATED) && state.getValue(BlockFarmland.HYDRATED)) {
            BakedModel wet = wetOverlayModels.getOrDefault(season, Collections.emptyMap()).get(wetShape);
            if (wet != null) quads.addAll(wet.getQuads(state, side, rand, data, renderType));
        }

        if (quads.isEmpty() && defaultModel != null) {
            return defaultModel.getQuads(state, side, rand, data, renderType);
        }

        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return defaultModel.getQuads(state, side, rand);
    }

    @Override public boolean useAmbientOcclusion() { return defaultModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return defaultModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return defaultModel.getParticleIcon(); }
    @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}
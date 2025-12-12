package dev.flomik.stardew.client.model;

import dev.flomik.stardew.client.ClientSeasonManager;
import dev.flomik.stardew.common.module.nature.block.BlockGrassSurface;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SeasonShapeModel implements BakedModel {
    private final Map<Season, Map<Shape, BakedModel>> models;
    private final BakedModel defaultModel;

    public SeasonShapeModel(Map<Season, Map<Shape, BakedModel>> models, BakedModel defaultModel) {
        this.models = models;
        this.defaultModel = defaultModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        Season season = ClientSeasonManager.getSeason();

        Shape shape = Shape.SINGLE;
        if (state != null && state.hasProperty(BlockGrassSurface.SHAPE)) {
            shape = state.getValue(BlockGrassSurface.SHAPE);
        }

        BakedModel model = models.getOrDefault(season, Collections.emptyMap())
                .getOrDefault(shape, defaultModel);

        if (model == null) model = defaultModel;

        return model.getQuads(state, side, rand, data, renderType);
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
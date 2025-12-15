package dev.flomik.stardew.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(Font.class)
public interface FontAccessor {
    @Accessor("fonts")
    Function<ResourceLocation, FontSet> getFonts();

    @Accessor("filterFishyGlyphs")
    boolean getFilterFishyGlyphs();
    
    @Invoker("renderChar")
    void invokeRenderChar(BakedGlyph glyph, boolean bold, boolean italic, float boldOffset,
                          float x, float y, Matrix4f matrix, VertexConsumer buffer,
                          float red, float green, float blue, float alpha, int packedLight);
}


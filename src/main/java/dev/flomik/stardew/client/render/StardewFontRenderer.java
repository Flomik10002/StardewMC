package dev.flomik.stardew.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.registry.ModIcons;
import dev.flomik.stardew.mixin.client.FontAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import net.minecraft.client.gui.Font.DisplayMode; // Explicit import

import java.util.function.Function;

/**
 * Custom Font Renderer для отображения кастомных иконок.
 * Использует {@link ModIcons} для регистрации и получения текстур иконок.
 */
public class StardewFontRenderer extends Font {
    
    public static final org.joml.Vector3f SHADOW_OFFSET = new org.joml.Vector3f(0.0F, 0.0F, 0.03F);
    
    /**
     * ThreadLocal флаг для принудительного использования кастомного рендерера
     * (например, при рендеринге tooltip).
     */
    private static final ThreadLocal<Boolean> FORCE_CUSTOM_RENDERING = ThreadLocal.withInitial(() -> false);

    private Function<ResourceLocation, FontSet> fontsFunction;
    private boolean filterFishyGlyphs;

    public StardewFontRenderer(Font original) {
        super(((FontAccessor)original).getFonts(), ((FontAccessor)original).getFilterFishyGlyphs());
        
        this.fontsFunction = ((FontAccessor)original).getFonts();
        this.filterFishyGlyphs = ((FontAccessor)original).getFilterFishyGlyphs();
    }
    
    /**
     * Включает принудительное использование кастомного рендеринга
     * (используется при рендеринге tooltip).
     */
    public static void setForceCustomRendering(boolean force) {
        FORCE_CUSTOM_RENDERING.set(force);
    }
    
    /**
     * Проверяет, включен ли принудительный кастомный рендеринг.
     */
    public static boolean isForceCustomRendering() {
        return FORCE_CUSTOM_RENDERING.get();
    }

    @Override
    public int drawInBatch(FormattedCharSequence sequence, float x, float y, int color, boolean dropShadow, 
                           Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, 
                           int backgroundColor, int packedLight) {
        
        if (sequence == null) return 0;
        
        // Собираем информацию об иконках в тексте
        IconPositionCollector collector = new IconPositionCollector();
        sequence.accept(collector);
        
        // Если иконок нет И не включен принудительный режим - используем стандартный рендеринг
        if (collector.iconPositions.isEmpty() && !isForceCustomRendering()) {
            return super.drawInBatch(sequence, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
        }
        
        // Есть иконки - используем свой рендерер (как в Emojiful, строка 187-195)
        color = (color & -67108864) == 0 ? color | -16777216 : color;
        
        Matrix4f matrix4f = new Matrix4f(matrix);
        
        if (dropShadow) {
            // Рендер тени
            IconAwareSequenceRenderer shadowRenderer = new IconAwareSequenceRenderer(
                    collector.iconPositions, buffer, x, y, color, true, matrix4f, 
                    displayMode == DisplayMode.SEE_THROUGH, packedLight);
            sequence.accept(shadowRenderer);
            shadowRenderer.finish(backgroundColor, x);
            matrix4f.translate(SHADOW_OFFSET);
        }
        
        // Основной рендер
        IconAwareSequenceRenderer mainRenderer = new IconAwareSequenceRenderer(
                collector.iconPositions, buffer, x, y, color, false, matrix4f, 
                displayMode == DisplayMode.SEE_THROUGH, packedLight);
        sequence.accept(mainRenderer);
        return (int) mainRenderer.finish(backgroundColor, x);
    }

    @Override
    public int drawInBatch(String text, float x, float y, int color, boolean dropShadow, 
                           Matrix4f matrix, MultiBufferSource buffer, DisplayMode displayMode, 
                           int backgroundColor, int packedLight) {
        
        if (text == null || text.isEmpty()) return 0;
        
        // Проверяем, есть ли в тексте иконки ИЛИ включен принудительный режим
        if (!containsIcons(text) && !isForceCustomRendering()) {
            // Нет иконок И не принудительный режим - используем стандартный рендеринг
            return super.drawInBatch(text, x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
        }
        
        // Есть иконки - создаем кастомный рендерер
        IconAwareTextRenderer renderer = new IconAwareTextRenderer(buffer, x, y, color, dropShadow, 
                                                                   matrix, displayMode == DisplayMode.SEE_THROUGH, packedLight);
        StringDecomposer.iterateFormatted(text, Style.EMPTY, renderer);
        
        return (int) renderer.finish(backgroundColor, x);
    }
    
    /**
     * Проверяет, содержит ли строка кастомные иконки
     */
    private static boolean containsIcons(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (ModIcons.isIcon(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Создает кастомный RenderType для иконок (как в Emojiful)
     */
    private static RenderType createIconRenderType(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeTextShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(
                            GlStateManager.SourceFactor.SRC_ALPHA, 
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                            GlStateManager.SourceFactor.ONE, 
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .createCompositeState(false);
        
        return RenderType.create("stardew_icon", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, 
                                 VertexFormat.Mode.QUADS, 256, false, true, state);
    }
    
    /**
     * Рендерит иконку в указанной позиции (точно как в Emojiful)
     */
    private static void renderIcon(ResourceLocation texture, float x, float y, float size, Matrix4f matrix, 
                                   MultiBufferSource buffer, int packedLight) {
        // Формула идеального центрирования относительно текста высотой ~8px:
        // offset = (size / 2) - 4
        // 10f -> 1.0 (сдвиг вверх на 1px)
        // 12f -> 2.0 (сдвиг вверх на 2px)
        // 8f  -> 0.0 (без сдвига)
        float offsetY = size / 2.0F - 4.0F;
        float offsetX = 0.0F;
        
        // UV координаты для полной текстуры
        float u0 = 0.0f;
        float v0 = 0.0f;
        float u1 = 1.0f;
        float v1 = 1.0f;
        
        RenderType renderType = createIconRenderType(texture);
        VertexConsumer builder = buffer.getBuffer(renderType);
        
        builder.vertex(matrix, x - offsetX, y - offsetY, 0.0f)
                .color(255, 255, 255, 255)
                .uv(u0, v0)
                .uv2(packedLight)
                .endVertex();
        
        builder.vertex(matrix, x - offsetX, y + size - offsetY, 0.0f)
                .color(255, 255, 255, 255)
                .uv(u0, v1)
                .uv2(packedLight)
                .endVertex();
        
        builder.vertex(matrix, x - offsetX + size, y + size - offsetY, 0.0f)
                .color(255, 255, 255, 255)
                .uv(u1, v1)
                .uv2(packedLight)
                .endVertex();
        
        builder.vertex(matrix, x - offsetX + size, y - offsetY, 0.0f)
                .color(255, 255, 255, 255)
                .uv(u1, v0)
                .uv2(packedLight)
                .endVertex();
    }

    /**
     * Собирает позиции иконок в тексте (не рендерит)
     */
    private class IconPositionCollector implements FormattedCharSink {
        final java.util.Map<Integer, ResourceLocation> iconPositions = new java.util.HashMap<>();
        int position = 0;

        @Override
        public boolean accept(int pos, Style style, int codePoint) {
            ResourceLocation iconTexture = ModIcons.getTexture(codePoint);
            
            if (iconTexture != null) {
                iconPositions.put(position, iconTexture);
            }
            
            position++;
            return true;
        }
    }
    
    /**
     * Рендерер последовательности с иконками (аналог EmojiCharacterRenderer из Emojiful)
     */
    private class IconAwareSequenceRenderer implements FormattedCharSink {
        private final java.util.Map<Integer, ResourceLocation> iconPositions;
        private final MultiBufferSource buffer;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f matrix;
        private final boolean seeThrough;
        private final int packedLight;
        private float x;
        private final float y;
        private int position = 0;

        public IconAwareSequenceRenderer(java.util.Map<Integer, ResourceLocation> iconPositions, 
                                        MultiBufferSource buffer, float x, float y, int color, boolean dropShadow,
                                        Matrix4f matrix, boolean seeThrough, int packedLight) {
            this.iconPositions = iconPositions;
            this.buffer = buffer;
            this.x = x;
            this.y = y;
            this.dropShadow = dropShadow;
            
            // Используем кастомный светло-коричневый цвет для тени (#dd9454)
            if (dropShadow) {
                this.dimFactor = 1.0F;
                this.r = 0xdd / 255.0F;  // #dd
                this.g = 0x94 / 255.0F;  // #94
                this.b = 0x54 / 255.0F;  // #54
                this.a = 1.0F;           // Полная непрозрачность
            } else {
                this.dimFactor = 1.0F;
                this.r = (float) (color >> 16 & 255) / 255.0F;
                this.g = (float) (color >> 8 & 255) / 255.0F;
                this.b = (float) (color & 255) / 255.0F;
                this.a = (float) (color >> 24 & 255) / 255.0F;
            }
            
            this.matrix = matrix;
            this.seeThrough = seeThrough;
            this.packedLight = packedLight;
        }

        @Override
        public boolean accept(int pos, Style style, int codePoint) {
            ResourceLocation iconTexture = iconPositions.get(position);
            
            // Если это кастомная иконка - рендерим её (как в Emojiful, строка 262-266)
            if (iconTexture != null) {
                // Не рендерим тени для иконок (как в Emojiful, строка 265)
                float iconSize = ModIcons.getSize(codePoint);
                if (!this.dropShadow) {
                    renderIcon(iconTexture, this.x, this.y, iconSize, matrix, buffer, packedLight);
                }
                this.x += iconSize;  // Увеличиваем X на ширину иконки (как в Emojiful, строка 266)
                position++;
                return true;
            }
            
            // Обычный символ - рендерим через стандартный механизм Font (как в Emojiful, строка 269-305)
            try {
                FontSet fontSet = fontsFunction.apply(style.getFont());
                com.mojang.blaze3d.font.GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, filterFishyGlyphs);
                net.minecraft.client.gui.font.glyphs.BakedGlyph bakedGlyph = style.isObfuscated() && codePoint != 32 
                        ? fontSet.getRandomGlyph(glyphInfo) 
                        : fontSet.getGlyph(codePoint);
                
                boolean isBold = style.isBold();
                
                // Получаем цвет из стиля (если есть) или используем дефолтный
                float red = this.r;
                float green = this.g;
                float blue = this.b;
                float alpha = this.a;
                
                // Для тени ВСЕГДА используем фиксированный цвет #dd9454, игнорируем стиль
                if (!this.dropShadow) {
                    net.minecraft.network.chat.TextColor textColor = style.getColor();
                    if (textColor != null) {
                        int colorValue = textColor.getValue();
                        red = (float) (colorValue >> 16 & 255) / 255.0F * this.dimFactor;
                        green = (float) (colorValue >> 8 & 255) / 255.0F * this.dimFactor;
                        blue = (float) (colorValue & 255) / 255.0F * this.dimFactor;
                        
                        // В tooltip заменяем белый цвет на #221122 для лучшей читаемости
                        if (isForceCustomRendering() && colorValue == 0xFFFFFF) {
                            red = 0x22 / 255.0F;
                            green = 0x11 / 255.0F;
                            blue = 0x22 / 255.0F;
                        }
                    } else if (isForceCustomRendering()) {
                        // Для tooltip без явного цвета (RESET) используем #221122 вместо белого
                        red = 0x22 / 255.0F;
                        green = 0x11 / 255.0F;
                        blue = 0x22 / 255.0F;
                    }
                }
                
                // Рендерим глиф если это не пустой глиф
                if (!(bakedGlyph instanceof net.minecraft.client.gui.font.glyphs.EmptyGlyph)) {
                    float boldOffset = isBold ? glyphInfo.getBoldOffset() : 0.0F;
                    float shadowOffset = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0F;
                    VertexConsumer vertexConsumer = this.buffer.getBuffer(
                            bakedGlyph.renderType(this.seeThrough ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL));
                    
                    // Вызываем renderChar из Font через Accessor (как в Emojiful, строка 292)
                    ((FontAccessor)StardewFontRenderer.this).invokeRenderChar(bakedGlyph, isBold, style.isItalic(), boldOffset, 
                            this.x + shadowOffset, this.y + shadowOffset, this.matrix, vertexConsumer, 
                            red, green, blue, alpha, this.packedLight);
                }
                
                // Увеличиваем X на ширину глифа
                float advance = glyphInfo.getAdvance(isBold);
                this.x += advance;
                
            } catch (Exception e) {
                // Fallback на случай ошибки
                this.x += 5f;
            }
            
            position++;
            return true;
        }

        public float finish(int backgroundColor, float startX) {
            // TODO: рендер фона, если нужен (colorBackgroundIn)
            return this.x;
        }
    }

    /**
     * Рендерер текста с поддержкой кастомных иконок (аналог EmojiCharacterRenderer из Emojiful)
     */
    private class IconAwareTextRenderer implements FormattedCharSink {
        private final MultiBufferSource buffer;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f matrix;
        private final boolean seeThrough;
        private final int packedLight;
        private float x;
        private final float y;

        public IconAwareTextRenderer(MultiBufferSource buffer, float x, float y, int color, boolean dropShadow,
                                    Matrix4f matrix, boolean seeThrough, int packedLight) {
            this.buffer = buffer;
            this.x = x;
            this.y = y;
            this.dropShadow = dropShadow;
            
            // Используем кастомный светло-коричневый цвет для тени (#dd9454)
            if (dropShadow) {
                this.dimFactor = 1.0F;
                this.r = 0xdd / 255.0F;  // #dd
                this.g = 0x94 / 255.0F;  // #94
                this.b = 0x54 / 255.0F;  // #54
                this.a = 1.0F;           // Полная непрозрачность
            } else {
                this.dimFactor = 1.0F;
                this.r = (float) (color >> 16 & 255) / 255.0F;
                this.g = (float) (color >> 8 & 255) / 255.0F;
                this.b = (float) (color & 255) / 255.0F;
                this.a = (float) (color >> 24 & 255) / 255.0F;
            }
            
            this.matrix = matrix;
            this.seeThrough = seeThrough;
            this.packedLight = packedLight;
        }

        @Override
        public boolean accept(int pos, Style style, int codePoint) {
            ResourceLocation iconTexture = ModIcons.getTexture(codePoint);
            
            // Если это кастомная иконка - рендерим её
            if (iconTexture != null) {
                // Не рендерим тени для иконок (как в Emojiful, строка 265)
                float iconSize = ModIcons.getSize(codePoint);
                if (!this.dropShadow) {
                    renderIcon(iconTexture, this.x, this.y, iconSize, matrix, buffer, packedLight);
                }
                this.x += iconSize;  // Увеличиваем X на ширину иконки (как в Emojiful, строка 266)
                return true;
            }
            
            // Обычный символ - рендерим через стандартный механизм Font
            try {
                FontSet fontSet = fontsFunction.apply(style.getFont());
                com.mojang.blaze3d.font.GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, filterFishyGlyphs);
                net.minecraft.client.gui.font.glyphs.BakedGlyph bakedGlyph = style.isObfuscated() && codePoint != 32 
                        ? fontSet.getRandomGlyph(glyphInfo) 
                        : fontSet.getGlyph(codePoint);
                
                boolean isBold = style.isBold();
                
                // Получаем цвет из стиля (если есть) или используем дефолтный
                float red = this.r;
                float green = this.g;
                float blue = this.b;
                float alpha = this.a;
                
                // Для тени ВСЕГДА используем фиксированный цвет #dd9454, игнорируем стиль
                if (!this.dropShadow) {
                    net.minecraft.network.chat.TextColor textColor = style.getColor();
                    if (textColor != null) {
                        int colorValue = textColor.getValue();
                        red = (float) (colorValue >> 16 & 255) / 255.0F * this.dimFactor;
                        green = (float) (colorValue >> 8 & 255) / 255.0F * this.dimFactor;
                        blue = (float) (colorValue & 255) / 255.0F * this.dimFactor;
                        
                        // В tooltip заменяем белый цвет на #221122 для лучшей читаемости
                        if (isForceCustomRendering() && colorValue == 0xFFFFFF) {
                            red = 0x22 / 255.0F;
                            green = 0x11 / 255.0F;
                            blue = 0x22 / 255.0F;
                        }
                    } else if (isForceCustomRendering()) {
                        // Для tooltip без явного цвета (RESET) используем #221122 вместо белого
                        red = 0x22 / 255.0F;
                        green = 0x11 / 255.0F;
                        blue = 0x22 / 255.0F;
                    }
                }
                
                // Рендерим глиф если это не пустой глиф
                if (!(bakedGlyph instanceof net.minecraft.client.gui.font.glyphs.EmptyGlyph)) {
                    float boldOffset = isBold ? glyphInfo.getBoldOffset() : 0.0F;
                    float shadowOffset = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0F;
                    VertexConsumer vertexConsumer = this.buffer.getBuffer(
                            bakedGlyph.renderType(this.seeThrough ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL));
                    
                    // Вызываем renderChar из Font через Accessor (как в Emojiful, строка 292)
                    ((FontAccessor)StardewFontRenderer.this).invokeRenderChar(bakedGlyph, isBold, style.isItalic(), boldOffset, 
                            this.x + shadowOffset, this.y + shadowOffset, this.matrix, vertexConsumer, 
                            red, green, blue, alpha, this.packedLight);
                }
                
                // Увеличиваем X на ширину глифа
                float advance = glyphInfo.getAdvance(isBold);
                this.x += advance;
                
            } catch (Exception e) {
                // Fallback на случай ошибки
                this.x += 5f;
            }
            
            return true;
        }

        public float finish(int backgroundColor, float startX) {
            // TODO: рендер фона, если нужен (colorBackgroundIn)
            return this.x;
        }
    }
}

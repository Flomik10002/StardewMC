package dev.flomik.stardew.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Корректирует отображение количества предметов >64 в GUI.
 * Масштабирует текст чтобы помещался в слот.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    
    private static double calculateStringScale(Font font, String countString) {
        var width = font.width(countString);
        
        if (width < 16)
            return 1.0;
        else
            return 16.0 / width;
    }
    
    // Отключаем ванильный рендер количества - используем SRG имена для production
    @Redirect(method = "m_280302_", // renderItemDecorations
              at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_252880_(FFF)V"), // translate
              remap = false)
    private void doNothing1(PoseStack instance, float x, float y, float z) {
        // Пропускаем ванильный translate
    }
    
    @Redirect(method = "m_280302_", // renderItemDecorations
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;m_280056_(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"), // drawString
              remap = false)
    private int doNothing2(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean shadow) {
        return 0; // Пропускаем ванильный drawString
    }
    
    // Рендерим свой текст с масштабированием
    @Inject(method = "m_280302_", // renderItemDecorations
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;m_280056_(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"), // drawString
            remap = false)
    private void renderText(Font font, ItemStack itemStack, int x, int y, String alternateCount, CallbackInfo ci) {
        var poseStack = ((GuiGraphics) (Object) this).pose();
        
        // Просто число, без форматирования
        String text = alternateCount == null ? String.valueOf(itemStack.getCount()) : alternateCount;
        
        float scale = (float) calculateStringScale(font, text);
        float inverseScale = 1 / scale;
        float xTransform = (x + 16) * inverseScale - font.width(text);
        float yTransform = (y + 16) * inverseScale - font.lineHeight;
        
        poseStack.scale(scale, scale, 1);
        poseStack.translate(xTransform, yTransform, 200);
        
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(
                text,
                0, // x - позиционирование через poseStack
                0, // y
                16777215, // белый цвет
                true, // тень
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0, // фон
                15728880 // свет
        );
        
        // Возвращаем позицию для durability bar
        poseStack.translate(-xTransform, -yTransform, 0);
        
        bufferSource.endBatch();
    }
}

package dev.flomik.stardew.core.registry.block.base;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.flomik.stardew.StardewMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class VisualItemAboveRenderer<T extends BlockEntityHasItemVisual> implements BlockEntityRenderer<T> {

    public VisualItemAboveRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(T be, float partialTick, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        if (!be.shouldRenderItem()) return;

        ItemStack item = be.getVisualItem();
        if (item.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        pose.pushPose();
        pose.translate(0.5, 2.8, 0.5);

        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        pose.pushPose();
        pose.translate(0, -0.9, 0);
        mc.getTextureManager().bindForSetup(new ResourceLocation(StardewMod.MODID, "textures/gui/item_popup.png"));

        VertexConsumer quad = buf.getBuffer(RenderType.entityTranslucent(new ResourceLocation(StardewMod.MODID, "textures/gui/item_popup.png")));
        Matrix4f matrix = pose.last().pose();

        float size = 0.6f;
        int packedLight = 0xF000F0;
        int packedOverlay = overlay;

        quad.vertex(matrix, -size, 0, 0)
                .color(255, 255, 255, 255)
                .uv(0, 1)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();
        quad.vertex(matrix, size, 0, 0)
                .color(255, 255, 255, 255)
                .uv(1, 1)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();
        quad.vertex(matrix, size, size * 2, 0)
                .color(255, 255, 255, 255)
                .uv(1, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();
        quad.vertex(matrix, -size, size * 2, 0)
                .color(255, 255, 255, 255)
                .uv(0, 0)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(0, 1, 0)
                .endVertex();

        pose.popPose();

        pose.pushPose();
        pose.translate(0, -0.36, 0);
        mc.getItemRenderer().renderStatic(
                item, ItemDisplayContext.GROUND, packedLight, overlay, pose, buf, be.getLevel(), 0
        );
        pose.popPose();

        pose.popPose();
    }


}

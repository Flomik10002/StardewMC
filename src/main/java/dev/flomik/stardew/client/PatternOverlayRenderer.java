package dev.flomik.stardew.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.item.tool.IPatternTool;
import dev.flomik.stardew.core.item.tool.Pattern;
import dev.flomik.stardew.core.item.tool.PatternType;
import dev.flomik.stardew.common.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT)
public class PatternOverlayRenderer {

    private static final ResourceLocation GREEN_OVERLAY = 
        new ResourceLocation(StardewMod.MODID, "textures/misc/selection_green.png");
    private static final ResourceLocation RED_OVERLAY = 
        new ResourceLocation(StardewMod.MODID, "textures/misc/selection_red.png");

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof IPatternTool tool)) {
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        Level level = player.level();
        BlockPos targetPos = blockHit.getBlockPos();
        
        BlockState targetState = level.getBlockState(targetPos);
        if (!targetState.is(ModBlocks.FARMLAND.get()) 
            && !targetState.is(ModBlocks.DIRT.get())) {
            return;
        }

        PatternType patternType = tool.getCurrentPattern(heldItem);
        Pattern pattern = patternType.getPattern();
        Direction facing = blockHit.getDirection().getAxis().isHorizontal() 
            ? blockHit.getDirection() 
            : player.getDirection();
        
        List<BlockPos> affectedBlocks = pattern.getAffectedPositions(level, targetPos, facing, player);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        var camera = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        
        RenderSystem.setShaderTexture(0, GREEN_OVERLAY);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (BlockPos pos : affectedBlocks) {
            if (tool.canApplyToBlock(level, pos, heldItem)) {
                renderBlockOverlay(poseStack, buffer, pos);
            }
        }
        Tesselator.getInstance().end();

        RenderSystem.setShaderTexture(0, RED_OVERLAY);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (BlockPos pos : affectedBlocks) {
            if (!tool.canApplyToBlock(level, pos, heldItem)) {
                renderBlockOverlay(poseStack, buffer, pos);
            }
        }
        Tesselator.getInstance().end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static void renderBlockOverlay(PoseStack poseStack, BufferBuilder buffer, BlockPos pos) {
        double x = pos.getX();
        double y = pos.getY() + 1.001;
        double z = pos.getZ();

        Matrix4f matrix = poseStack.last().pose();

        int red = 255;
        int green = 255;
        int blue = 255;
        int alpha = 200;

        buffer.vertex(matrix, (float) x, (float) y, (float) z).uv(0, 0).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) x, (float) y, (float) (z + 1)).uv(0, 1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) (x + 1), (float) y, (float) (z + 1)).uv(1, 1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) (x + 1), (float) y, (float) z).uv(1, 0).color(red, green, blue, alpha).endVertex();
    }
}


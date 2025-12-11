package dev.flomik.stardew.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.craftables.block.BlockChest;
import dev.flomik.stardew.common.module.craftables.blockentity.BlockEntityChest;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ChestRenderer<T extends BlockEntityChest> implements BlockEntityRenderer<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(StardewMod.MODID, "chest"), "main");

    protected static final ResourceLocation[] TEXTURES = new ResourceLocation[21];
    static {
        for (int i = 0; i < 21; i++) {
            TEXTURES[i] = new ResourceLocation(StardewMod.MODID, "textures/block/craftables/chest/chest_" + i + ".png");
        }
    }

    protected final ModelPart lid;
    protected final ModelPart bottom;
    protected final ModelPart lock;

    public ChestRenderer(BlockEntityRendererProvider.Context context) {
        this(context, LAYER_LOCATION);
    }

    protected ChestRenderer(BlockEntityRendererProvider.Context context, ModelLayerLocation layer) {
        ModelPart root = context.bakeLayer(layer);
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = this.lid.getChild("lock");
    }

    protected ResourceLocation getTexture(int variant) {
        if (variant < 0 || variant >= TEXTURES.length) variant = 0;
        return TEXTURES[variant];
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.0F, 0.0F, 1.0F, 14.0F, 8.0F, 14.0F),
                PartPose.ZERO);

        PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create()
                        .texOffs(0, 22).addBox(1.0F, 0.0F, -14.0F, 14.0F, 4.0F, 14.0F)
                        .texOffs(0, 40).addBox(1.0F, 4.0F, -13.0F, 14.0F, 2.0F, 12.0F)
                        .texOffs(52, 40).addBox(1.0F, 6.0F, -11.0F, 14.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, 8, 15.0F));

        lid.addOrReplaceChild("lock", CubeListBuilder.create()
                        .texOffs(52, 49).addBox(6.0F, -3.0F, -15.0F, 4.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void render(BlockEntityChest entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof BlockChest)) return;

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.5D, 0.5D);
        Direction dir = state.getValue(BlockChest.FACING);
        float rot = switch (dir) {
            case SOUTH -> 180f;
            case WEST  -> 270f;
            case NORTH -> 0f;
            case EAST  -> 90f;
            default -> 0f;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(-rot));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        float openFactor = entity.getOpenNess(partialTick);
        openFactor = 1.0F - openFactor;
        openFactor = 1.0F - openFactor * openFactor * openFactor;

        this.lid.xRot = (openFactor * ((float)Math.PI / 2F));

        int variant = state.getValue(BlockChest.VARIANT);
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(getTexture(variant)));

        this.lid.render(poseStack, consumer, packedLight, packedOverlay);
        this.bottom.render(poseStack, consumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
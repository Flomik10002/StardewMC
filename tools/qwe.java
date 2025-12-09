public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create()
                        .texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F),
                PartPose.ZERO);

        PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 9.0F, 1.0F));

        lid.addOrReplaceChild("lock", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
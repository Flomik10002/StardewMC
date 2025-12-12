package dev.flomik.stardew.common.registry.framework.datagen;

import net.minecraft.resources.ResourceLocation;

public class ModelPresets {

    public static ItemModelGen simple() {
        return (provider, item, name) -> provider.singleTexture(
                name,
                new ResourceLocation("item/generated"),
                "layer0",
                provider.modLoc("item/" + name));
    }

    public static ItemModelGen handheld() {
        return (provider, item, name) -> provider.singleTexture(
                name,
                new ResourceLocation("item/handheld"),
                "layer0",
                provider.modLoc("item/" + name));
    }

    public static ItemModelGen useBlockModel() {
        return (provider, item, name) -> provider.withExistingParent(
                name,
                provider.modLoc("block/" + name));
    }

    public static ItemModelGen seed() {
        return simple();
    }
}
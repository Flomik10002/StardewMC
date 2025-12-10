package dev.flomik.stardew.datagen;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.registry.framework.datagen.DataGenManager;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StardewItemModels extends ItemModelProvider {

    public StardewItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StardewMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        DataGenManager.generateAll(this);
    }
}

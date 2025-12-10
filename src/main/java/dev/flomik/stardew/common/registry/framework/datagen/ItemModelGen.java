package dev.flomik.stardew.common.registry.framework.datagen;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;

@FunctionalInterface
public interface ItemModelGen {
    void generate(ItemModelProvider provider, Item item, String name);
}
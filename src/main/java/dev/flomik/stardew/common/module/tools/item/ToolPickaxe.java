package dev.flomik.stardew.common.module.tools.item;

import dev.flomik.stardew.common.registry.framework.StardewItemBase;

public class ToolPickaxe extends StardewItemBase {
    private final int tier;

    public ToolPickaxe(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }
}


package dev.flomik.stardew.common.module.tools.item;

import dev.flomik.stardew.common.registry.framework.StardewItemBase;

public class ToolAxe extends StardewItemBase {
    private final int tier;

    public ToolAxe(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }
}

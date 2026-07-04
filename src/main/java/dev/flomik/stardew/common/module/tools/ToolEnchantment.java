package dev.flomik.stardew.common.module.tools;

import dev.flomik.stardew.common.registry.ModIcons;

public enum ToolEnchantment {
    // Axe
    SHAVING("shaving", ModIcons.STAR_ENCHANTMENT + " Shaving"),

    // Rod
    AUTO_HOOK("auto_hook", ModIcons.STAR_ENCHANTMENT + " Auto-Hook"),
    MASTER("master", ModIcons.STAR_ENCHANTMENT + " Master"),
    PRESERVING("preserving", ModIcons.STAR_ENCHANTMENT + " Preserving"),

    // Hoe
    ARCHAEOLOGIST("archaeologist", ModIcons.STAR_ENCHANTMENT + " Archaeologist"),
    GENEROUS("generous", ModIcons.STAR_ENCHANTMENT + " Generous"),
    REACHING("reaching", ModIcons.STAR_ENCHANTMENT + " Reaching"),

    // Watering Can
    BOTTOMLESS("bottomless", ModIcons.STAR_ENCHANTMENT + " Bottomless"),

    // Pan
    FISHER("fisher", ModIcons.STAR_ENCHANTMENT + " Fisher"),

    // Shared / Multi-tool
    EFFICIENT("efficient", ModIcons.STAR_ENCHANTMENT + " Efficient"),
    POWERFUL("powerful", ModIcons.STAR_ENCHANTMENT + " Powerful"),
    SWIFT("swift", ModIcons.STAR_ENCHANTMENT + " Swift");

    private final String nbtKey;
    private final String displayName;

    ToolEnchantment(String nbtKey, String displayName) {
        this.nbtKey = nbtKey;
        this.displayName = displayName;
    }

    public String getNbtKey() {
        return nbtKey;
    }

    public String getDisplayName() {
        return displayName;
    }
}
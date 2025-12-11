package dev.flomik.stardew.common.module.tools;

public enum ToolEnchantment {
    // Axe
    SHAVING("shaving", "✰ Shaving"),

    // Rod
    AUTO_HOOK("auto_hook", "✰ Auto-Hook"),
    MASTER("master", "✰ Master"),
    PRESERVING("preserving", "✰ Preserving"),

    // Hoe
    ARCHAEOLOGIST("archaeologist", "✰ Archaeologist"),
    GENEROUS("generous", "✰ Generous"),
    REACHING("reaching", "✰ Reaching"),

    // Watering Can
    BOTTOMLESS("bottomless", "✰ Bottomless"),

    // Pan
    FISHER("fisher", "✰ Fisher"),

    // Shared / Multi-tool
    EFFICIENT("efficient", "✰ Efficient"),
    POWERFUL("powerful", "✰ Powerful"),
    SWIFT("swift", "✰ Swift");

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
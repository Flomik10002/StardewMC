package dev.flomik.stardew.common.module.time;

import net.minecraft.util.StringRepresentable;

public enum Season implements StringRepresentable {
    SPRING, SUMMER, FALL, WINTER;

    public Season next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static Season fromString(String name) {
        return switch (name.toLowerCase()) {
            case "spring" -> SPRING;
            case "summer" -> SUMMER;
            case "fall", "autumn" -> FALL;
            case "winter" -> WINTER;
            default -> SPRING;
        };
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}

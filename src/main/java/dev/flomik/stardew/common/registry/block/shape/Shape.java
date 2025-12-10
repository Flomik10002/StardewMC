package dev.flomik.stardew.common.registry.block.shape;

import net.minecraft.util.StringRepresentable;

public enum Shape implements StringRepresentable {
    SINGLE("single"),
    HORIZONTAL_LEFT("horizontal_left"),
    HORIZONTAL_MID("horizontal_mid"),
    HORIZONTAL_RIGHT("horizontal_right"),
    VERTICAL_TOP("vertical_top"),
    VERTICAL_MID("vertical_mid"),
    VERTICAL_BOTTOM("vertical_bottom"),
    TOP_LEFT("top_left"),
    TOP("top"),
    TOP_RIGHT("top_right"),
    LEFT("left"),
    CENTER("center"),
    RIGHT("right"),
    BOTTOM_LEFT("bottom_left"),
    BOTTOM("bottom"),
    BOTTOM_RIGHT("bottom_right"),
    INNER_TOP_LEFT("inner_top_left"),
    INNER_TOP_RIGHT("inner_top_right"),
    INNER_BOTTOM_LEFT("inner_bottom_left"),
    INNER_BOTTOM_RIGHT("inner_bottom_right");

    private final String name;

    Shape(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
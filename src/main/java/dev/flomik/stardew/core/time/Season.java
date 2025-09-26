package dev.flomik.stardew.core.time;

public enum Season {
    SPRING, SUMMER, FALL, WINTER;

    public Season next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}

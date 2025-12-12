package dev.flomik.stardew.common.module.tools;

/**
 * Типы паттернов для инструментов с их порядком для переключения.
 */
public enum PatternType {
    SINGLE("Single", 1, PatternProvider.SINGLE),
    THREE("Line 3", 3, PatternProvider.THREE),
    FIVE("Line 5", 5, PatternProvider.FIVE),
    GRID_3X3("Grid 3x3", 9, PatternProvider.GRID_3X3),
    GRID_6X3("Grid 6x3", 18, PatternProvider.GRID_6X3),
    GRID_5X5("Grid 5x5", 25, PatternProvider.GRID_5X5);

    private final String displayName;
    private final int blockCount;
    private final Pattern pattern;

    PatternType(String displayName, int blockCount, Pattern pattern) {
        this.displayName = displayName;
        this.blockCount = blockCount;
        this.pattern = pattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Получает следующий паттерн по кругу, не превышая максимальный.
     */
    public PatternType next(PatternType max) {
        int nextOrdinal = (this.ordinal() + 1);
        if (nextOrdinal > max.ordinal()) {
            return SINGLE;
        }
        return values()[nextOrdinal];
    }

    /**
     * Получает паттерн по имени или возвращает SINGLE по умолчанию.
     */
    public static PatternType fromString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return SINGLE;
        }
    }
}


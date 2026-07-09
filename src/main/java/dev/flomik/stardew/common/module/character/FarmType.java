package dev.flomik.stardew.common.module.character;

/**
 * Список из ТЗ §38. Какой именно тип фермы реально доступен/что он делает с
 * миром — вопрос {@code WorldInitializationService} (ТЗ §40), который
 * специально оставлен отдельным модулем на будущее (см.
 * docs/world-template.md — copy-template pipeline пока не реализован).
 * Здесь только сам выбор как часть профиля персонажа.
 */
public enum FarmType {
    STANDARD,
    RIVERLAND,
    FOREST,
    HILLTOP,
    WILDERNESS,
    FOUR_CORNERS,
    BEACH;

    public static FarmType fromId(String id) {
        for (FarmType value : values()) {
            if (value.name().equalsIgnoreCase(id)) return value;
        }
        return STANDARD;
    }
}

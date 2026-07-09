package dev.flomik.stardew.common.module.character;

/**
 * Стабильные ID, а не индекс массива (см. ТЗ §11) — так добавление нового
 * варианта в будущем не превращает старые сохранения в "археологический слой".
 *
 * Порядок значений ОБЯЗАН совпадать с порядком кадров в
 * {@code assets/stardew/textures/gui/character_creation/pets/pets.png} (спрайт-лист 96×16, 6
 * кадров по 16×16, шаг между кадрами 16px) — {@code ordinal()} используется
 * как индекс кадра в {@code CharacterCreationScreen.buildAnimalPreferenceRow}.
 * Как в vanilla Stardew Valley: выбор питомца — это тип+порода одним списком,
 * а не просто "кот или собака".
 */
public enum AnimalPreference {
    CAT_GINGER,
    CAT_GRAY,
    CAT_BEIGE,
    DOG_GOLDEN,
    DOG_DARK,
    DOG_CREAM;

    public static AnimalPreference fromId(String id) {
        for (AnimalPreference value : values()) {
            if (value.name().equalsIgnoreCase(id)) return value;
        }
        return CAT_GINGER;
    }
}

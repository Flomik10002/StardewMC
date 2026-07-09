package dev.flomik.stardew.common.module.character.cosmetic;

/**
 * Один набор ассетов косметики ДЛЯ ОДНОЙ модели рук (classic/slim) — см.
 * {@link CosmeticDefinition}. {@code mask} может быть {@code null} (нечего
 * красить, только {@code texture}).
 *
 * {@code pantsOverlay} — только у рубашки (см. чат: "добавь опциональную
 * текстуру, которая будет рисоваться у рубашки ТОЛЬКО когда одеты штаны").
 * Неизменяемый (НЕ красится, в отличие от {@code mask}) слой, порядок
 * отрисовки: {@code texture} → {@code pantsOverlay} (если есть штаны) →
 * {@code mask} (если есть штаны) — см. {@code SkinComposer.applyShirtCosmetic}.
 * У остальных категорий (штаны/волосы/аксессуары) всегда {@code null}.
 */
public record CosmeticVariant(String texture, String mask, String pantsOverlay) {
}

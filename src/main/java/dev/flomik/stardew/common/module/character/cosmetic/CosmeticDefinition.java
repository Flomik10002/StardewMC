package dev.flomik.stardew.common.module.character.cosmetic;

import dev.flomik.stardew.common.module.character.CharacterModelType;

/**
 * Одна запись косметики (hair/shirt/pants/accessory) — см. ТЗ §13-16, §52.
 *
 * Руки classic и slim РАЗНОЙ ширины в 3D-модели, поэтому у одежды, которая
 * их покрывает (в первую очередь shirt), нужны отдельные ассеты на каждую
 * модель — {@link #male()} (classic) / {@link #female()} (slim), по той же
 * терминологии, что и в assets (character/clothes/shirts/male,.../female).
 * Категории, для которых модель руки не важна (штаны, волосы, аксессуары),
 * просто кладут ОДИН И ТОТ ЖЕ {@link CosmeticVariant} в оба поля -
 * {@code variantFor} работает одинаково для всех категорий, код не завязан
 * на то, важна ли разница по факту для конкретной категории.
 *
 * Два вида одежды внутри одного и того же {@code variant}:
 * - {@code variant.mask() == null} - обычная фикс-текстура, {@code supportsColor}
 *   должен быть {@code false} (перекрашивать нечего, {@code variant.texture()}
 *   уже содержит финальные цвета).
 * - {@code variant.mask() != null} и {@code supportsColor == true} - маска
 *   перекрашивается в выбранный цвет (см. {@code SkinComposer.applyTintMask}),
 *   {@code variant.texture()} поверх - неизменяемые фикс-детали (может
 *   отсутствовать).
 */
public record CosmeticDefinition(
        String id,
        CosmeticVariant male,
        CosmeticVariant female,
        boolean supportsColor
) {
    /**
     * Ни male, ни female не обязательны в JSON по отдельности - если под
     * текущую модель своего варианта нет, тихо используется вариант ДРУГОЙ
     * модели (симметрично в обе стороны), а не просто {@code male} как
     * дефолт. Это специально важно для волос: обычное пролистывание
     * стрелками [←][→] намеренно НЕ ограничено по полу (см.
     * CharacterCreationScreen.randomHairId - лок только на рандомайзере),
     * так что мужской персонаж вполне может выбрать причёску, у которой
     * заполнен только {@code female} - раньше это молча возвращало
     * {@code null} (мужского варианта нет) и причёска не рисовалась вообще
     * (падал fallback на плоскую заливку), а не показывала female-текстуру
     * как разумный запасной вариант.
     */
    public CosmeticVariant variantFor(CharacterModelType modelType) {
        CosmeticVariant primary = modelType == CharacterModelType.SLIM ? female : male;
        CosmeticVariant secondary = modelType == CharacterModelType.SLIM ? male : female;
        return primary != null ? primary : secondary;
    }
}

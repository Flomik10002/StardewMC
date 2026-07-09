package dev.flomik.stardew.common.module.character.cosmetic;

/**
 * Оттенок кожи — один плоский RGB, которым красится {@code skin_mask_male.png}/
 * {@code skin_mask_female.png} (см. {@link dev.flomik.stardew.client.character.skin.SkinComposer}).
 * Раньше это была палитра highlight/base/shadow под procedural-заливку без
 * текстуры — теперь светотень уже запечена в самой маске (тот же принцип,
 * что и у волос, см. asset-tools/skin_tint_map_generator), поэтому нужен
 * только один базовый тон.
 */
public record SkinToneDefinition(
        String id,
        int base
) {
}

package dev.flomik.stardew.common.module.character;

import net.minecraft.nbt.CompoundTag;

/**
 * Миграции схемы {@link CharacterProfile} (ТЗ §35). Пока существует только
 * v1, поэтому миграций нет — метод оставлен как единая точка, куда позже
 * добавляются шаги вида {@code if (version < 2) migrateV1ToV2(tag);}.
 */
final class CharacterProfileMigrations {

    private CharacterProfileMigrations() {
    }

    static void migrate(CompoundTag tag, int fromVersion) {
        // no-op: schemaVersion == 1 == CharacterProfile.SCHEMA_VERSION
    }
}

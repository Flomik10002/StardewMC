package dev.flomik.stardew.mixin.common;

import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Даёт доступ (и, через {@link Mutable}, право ПОДМЕНИТЬ) к приватному полю
 * {@code settings} {@link PrimaryLevelData} - {@link LevelSettings} не имеет
 * {@code withLevelName(...)} (у него есть только {@code withGameType}/
 * {@code withDifficulty}/{@code withDataConfiguration}/{@code withLifecycle}),
 * поэтому переименование мира "на лету" (пока сервер УЖЕ запущен, см.
 * {@code common.module.character.network.CharacterCreationServerHandler}) —
 * это всегда полная пересборка {@code LevelSettings} с новым именем и
 * старыми остальными полями, записанная обратно сюда этим accessor'ом.
 */
@Mixin(PrimaryLevelData.class)
public interface PrimaryLevelDataAccessor {

    @Accessor("settings")
    LevelSettings getSettings();

    @Accessor("settings")
    @Mutable
    void setSettings(LevelSettings settings);
}

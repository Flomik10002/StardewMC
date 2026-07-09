package dev.flomik.stardew.common.module.character;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Признак "это подготовленный мир Stardew Valley" (см. docs/world-template.md
 * §3 — "Ограничение по миру"). Character Creator не должен открываться в
 * произвольных vanilla-мирах, поэтому по умолчанию флаг выключен.
 *
 * При копировании шаблона ({@code WorldTemplateManager.seedWorldMarker})
 * этот файл пишется заранее с {@code stardewWorld=true} прямо в скопированный
 * save, тем же форматом, каким его ждёт {@link #load}. Ручная разметка через
 * {@code /stardew debug markworld} остаётся как инструмент для тестирования
 * миров, не созданных через кнопку "Play Stardew Valley".
 */
public class StardewWorldMarker extends SavedData {

    private static final String NAME = "stardew_world_marker";

    private boolean stardewWorld = false;
    private boolean templateStateReset = false;

    public boolean isStardewWorld() {
        return stardewWorld;
    }

    public void setStardewWorld(boolean value) {
        this.stardewWorld = value;
        setDirty();
    }

    /**
     * Одноразовый сброс даты/кошелька, унаследованных из сырого файла шаблона
     * (тот, кто собирал map/SV, играл в него и оставил свой прогресс) —
     * {@link dev.flomik.stardew.common.module.character.network.CharacterCreationServerHandler}
     * должен выполнить его строго один раз за жизнь мира, иначе повторное
     * прохождение Character Creator (например через {@code /stardew debug
     * resetcharacter}) стирало бы уже НАКОПЛЕННЫЙ игроком прогресс.
     */
    public boolean isTemplateStateReset() {
        return templateStateReset;
    }

    public void markTemplateStateReset() {
        this.templateStateReset = true;
        setDirty();
    }

    public static StardewWorldMarker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                StardewWorldMarker::load,
                StardewWorldMarker::new,
                NAME
        );
    }

    public static StardewWorldMarker load(CompoundTag tag) {
        StardewWorldMarker data = new StardewWorldMarker();
        data.stardewWorld = tag.getBoolean("stardewWorld");
        data.templateStateReset = tag.getBoolean("templateStateReset");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("stardewWorld", stardewWorld);
        tag.putBoolean("templateStateReset", templateStateReset);
        return tag;
    }
}

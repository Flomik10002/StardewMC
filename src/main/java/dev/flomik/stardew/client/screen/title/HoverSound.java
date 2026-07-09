package dev.flomik.stardew.client.screen.title;

import dev.flomik.stardew.common.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * Звук при НАВЕДЕНИИ (не клике) на кнопки главного меню/Load/"?"/"настройки"/
 * "назад"/карточки мира - см. {@link ModSounds#MENU_HOVER}/{@link ModSounds#MENU_HOVER_WORLD}
 * (сверено по декомпилированному {@code TitleMenu}/{@code LoadGameMenu} - см.
 * чат: "проверь все звуки по исходникам stardew"). Играется РОВНО ОДИН РАЗ
 * на переходе false->true, не каждый кадр, пока курсор просто стоит на
 * кнопке - для этого каждый вызывающий хранит своё предыдущее состояние
 * ({@code wasHovered}) и передаёт его сюда/получает обратно на каждый кадр.
 */
final class HoverSound {

    private HoverSound() {
    }

    /** Кнопки главного меню/Load/"?"/"назад" - {@link ModSounds#MENU_HOVER}. */
    static boolean update(boolean hoveredNow, boolean wasHovered) {
        return update(hoveredNow, wasHovered, ModSounds.MENU_HOVER);
    }

    /** Явно указанный cue - см. {@link dev.flomik.stardew.client.screen.title.StardewLoadWorldScreen} (карточка мира - {@link ModSounds#MENU_HOVER_WORLD}, ДРУГОЙ звук, не как у кнопок). */
    static boolean update(boolean hoveredNow, boolean wasHovered, RegistryObject<SoundEvent> sound) {
        if (hoveredNow && !wasHovered) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound.get(), 1.0F));
        }
        return hoveredNow;
    }
}

package dev.flomik.stardew.client;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.screen.title.StardewTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Подменяет ВСЯКИЙ раз, когда игра пытается открыть ванильный
 * {@link TitleScreen} (загрузка, выход из мира/дисконнект и т.п.), на
 * {@link StardewTitleScreen} — вместо правки самого {@code TitleScreen}
 * мискином (как раньше делал {@code mixin.client.TitleScreenMixin}, теперь
 * удалён), перехватываем сам момент открытия. {@link StardewTitleScreen}
 * НЕ наследует {@code TitleScreen} специально — иначе {@code instanceof}
 * ниже совпадал бы и с уже подменённым экраном, подменяя его снова
 * рекурсивно.
 *
 * {@code guiScale} форсируется в 2 на весь наш menu-flow (Title -> Load),
 * тем же приёмом, что и {@code ClientCharacterCreationOpener} у экрана
 * создания персонажа — макет (74×58 кнопки, 400×187 лого) верстался под
 * фиксированный масштаб, на "родном" guiScale игрока пропорции/шрифты
 * поплыли бы. Сохранённый {@code previousGuiScale} возвращается не тут и не
 * в {@code removed()} экранов (тот вызывался бы при каждом переходе
 * Title<->Load, слишком рано) — только в момент реального выхода в геймплей,
 * см. {@link StardewTitleScreen#restoreGuiScale()}.
 */
@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT)
public class TitleScreenReplacer {

    public static final int FORCED_GUI_SCALE = 2;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen) {
            Minecraft minecraft = Minecraft.getInstance();
            int previousGuiScale = minecraft.options.guiScale().get();
            if (previousGuiScale != FORCED_GUI_SCALE) {
                minecraft.options.guiScale().set(FORCED_GUI_SCALE);
                minecraft.resizeDisplay();
            }
            event.setNewScreen(new StardewTitleScreen(previousGuiScale));
        }
    }
}

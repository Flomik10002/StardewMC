package dev.flomik.stardew.client.character.worldtemplate;

import dev.flomik.stardew.client.TitleScreenReplacer;
import dev.flomik.stardew.client.screen.character.CharacterCreationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Обработчик кнопки "Play Stardew Valley"/"New" на титульном экране (см.
 * {@code StardewTitleScreen}). Мир и сервер тут ЕЩЁ НЕ создаются - открывает
 * pre-world {@link CharacterCreationScreen} (см. её javadoc {@code preWorld}),
 * который сам создаст мир из шаблона (docs/world-template.md) и подключится,
 * как только игрок подтвердит персонажа кнопкой OK. Так игрок никогда не
 * видит уже загруженный мир, если решит отменить создание персонажа - его
 * попросту ещё не существует (см. чат: "мы не можем НЕ создавать мир ДО того
 * как игрок создаст всё?").
 */
public final class StardewPlayButtonHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StardewPlayButtonHandler.class);

    /** Также используется {@link CharacterCreationScreen#onOkPressed()} при фактическом создании мира. */
    public static final String DEFAULT_TEMPLATE_ID = "standard";

    private StardewPlayButtonHandler() {
    }

    public static void onClick(Screen currentScreen, int previousGuiScale) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!WorldTemplateManager.templateExists(DEFAULT_TEMPLATE_ID)) {
            Path expected = WorldTemplateManager.templatesRoot().resolve(DEFAULT_TEMPLATE_ID);
            LOGGER.warn("[Stardew] Play button clicked but no template at {}", expected);
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE,
                    Component.literal("Stardew template not found"), Component.literal(expected.toString()));
            return;
        }

        // Форсируем guiScale=2 тем же приёмом, что и Load-кнопка на этом же
        // экране (см. StardewTitleScreen) - ДО создания экрана, а не внутри
        // его init().
        if (minecraft.options.guiScale().get() != TitleScreenReplacer.FORCED_GUI_SCALE) {
            minecraft.options.guiScale().set(TitleScreenReplacer.FORCED_GUI_SCALE);
            minecraft.resizeDisplay();
        }
        minecraft.setScreen(new CharacterCreationScreen(previousGuiScale, true));
    }
}

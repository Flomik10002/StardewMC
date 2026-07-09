package dev.flomik.stardew.client.character.worldtemplate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Обработчик кнопки "Play Stardew Valley" на экране выбора миров (см.
 * {@code mixin.client.SelectWorldScreenMixin}). Копирует шаблон и сразу
 * прыгает в игру — в обход обычного мастера создания мира, генерировать
 * нечего (docs/world-template.md).
 */
public final class StardewPlayButtonHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StardewPlayButtonHandler.class);
    private static final String DEFAULT_TEMPLATE_ID = "standard";
    private static final String DEFAULT_FARM_NAME = "Stardew Valley";

    private StardewPlayButtonHandler() {
    }

    public static void onClick(Screen currentScreen) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!WorldTemplateManager.templateExists(DEFAULT_TEMPLATE_ID)) {
            Path expected = WorldTemplateManager.templatesRoot().resolve(DEFAULT_TEMPLATE_ID);
            LOGGER.warn("[Stardew] Play button clicked but no template at {}", expected);
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE,
                    Component.literal("Stardew template not found"), Component.literal(expected.toString()));
            return;
        }

        try {
            String levelId = WorldTemplateManager.createFromTemplate(DEFAULT_TEMPLATE_ID, DEFAULT_FARM_NAME);
            minecraft.createWorldOpenFlows().loadLevel(currentScreen, levelId);
        } catch (IOException e) {
            LOGGER.error("[Stardew] Failed to create world from template", e);
            SystemToast.add(minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE,
                    Component.literal("Failed to create world from template"), Component.literal(String.valueOf(e.getMessage())));
        }
    }
}

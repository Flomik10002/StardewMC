package dev.flomik.stardew.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.flomik.stardew.StardewMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Заменяет системный курсор ОС на свою текстуру ({@code textures/gui/character_creation/cursor.png}),
 * пока открыт любой из наших кастомных экранов (изначально был
 * package-private только у {@code CharacterCreationScreen}; см. чат: "замени
 * курсор на этих скринах на тот курсор, как в редакторе персонажа" -
 * вынесен сюда, в общий client-пакет, чтобы {@code StardewTitleScreen}/
 * {@code StardewLoadWorldScreen}/{@code StardewAboutScreen} могли
 * переиспользовать ТОТ ЖЕ курсор, не дублируя GLFW-код). Обычный GLFW
 * custom-cursor (не что-то Minecraft-специфичное — ваниль всегда использует
 * системный курсор в GUI, только в 3D-мире рисует крестик сама). Читает файл
 * заново при каждом первом вызове {@link #apply()} за сессию - подставите
 * свою текстуру любого размера, код не завязан на конкретные width/height.
 *
 * Курсор создаётся один раз и кэшируется на весь процесс (как
 * {@code RuntimeSkinManager} кэширует свою {@code DynamicTexture}) - маленький
 * cursor handle не стоит того, чтобы пересоздавать его на каждое открытие
 * экрана.
 */
public final class CustomCursor {

    private static final Logger LOGGER = LoggerFactory.getLogger("StardewCursor");
    private static final ResourceLocation CURSOR_TEXTURE =
            new ResourceLocation(StardewMod.MODID, "textures/gui/character_creation/cursor.png");

    private static long cursorHandle = MemoryUtil.NULL;
    private static boolean creationAttempted = false;

    private CustomCursor() {
    }

    public static void apply() {
        if (!creationAttempted) {
            creationAttempted = true;
            cursorHandle = create();
        }
        if (cursorHandle != MemoryUtil.NULL) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), cursorHandle);
        }
    }

    /** Возвращает системный курсор по умолчанию - вызывается при закрытии экрана. */
    public static void restore() {
        GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), MemoryUtil.NULL);
    }

    /** Нативный курсор (16×16 у нашего файла) рисуется в 1:1 пиксель ОС - на большинстве экранов это выглядит крошечным. */
    private static final int UPSCALE = 4;

    private static long create() {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(CURSOR_TEXTURE);
             NativeImage image = NativeImage.read(stream);
             MemoryStack stack = MemoryStack.stackPush()) {
            int srcWidth = image.getWidth();
            int srcHeight = image.getHeight();
            int width = srcWidth * UPSCALE;
            int height = srcHeight * UPSCALE;

            // NativeImage.getPixelRGBA возвращает упакованный int (A<<24|B<<16|G<<8|R,
            // см. SkinComposer#withAlpha) - разбираем обратно в R,G,B,A байты,
            // как того ждёт GLFWImage. Апскейл nearest-neighbor (каждый
            // исходный пиксель - блок UPSCALE×UPSCALE) - сохраняет чёткие
            // пиксель-артные края, не размывает как билинейный.
            ByteBuffer pixels = stack.malloc(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getPixelRGBA(x / UPSCALE, y / UPSCALE);
                    pixels.put((byte) (argb & 0xFF));
                    pixels.put((byte) ((argb >> 8) & 0xFF));
                    pixels.put((byte) ((argb >> 16) & 0xFF));
                    pixels.put((byte) ((argb >> 24) & 0xFF));
                }
            }
            pixels.flip();

            GLFWImage glfwImage = GLFWImage.malloc(stack);
            glfwImage.set(width, height, pixels);
            // Хотспот (0,0) - верхний левый угол картинки, остриё указателя
            // (проверено по альфа-каналу самой текстуры) - тот же угол и
            // после апскейла, координаты не меняются.
            long handle = GLFW.glfwCreateCursor(glfwImage, 0, 0);
            if (handle == MemoryUtil.NULL) {
                LOGGER.error("glfwCreateCursor returned NULL ({}x{} image) - platform/driver may not support custom cursors here", width, height);
            } else {
                LOGGER.info("Custom cursor created from {}x{} source upscaled to {}x{}, handle={}", srcWidth, srcHeight, width, height, handle);
            }
            return handle;
        } catch (IOException e) {
            LOGGER.error("Failed to load custom cursor texture, falling back to system cursor", e);
            return MemoryUtil.NULL;
        }
    }
}

package dev.flomik.stardew.client.debug;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.flomik.stardew.StardewMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Дебаг-инструмент для ЛЮБОГО GUI (не привязан к конкретному экрану — виснет
 * на базовом {@link Screen} через Forge {@link ScreenEvent}, а не на
 * конкретном классе экрана): F8 включает режим перетаскивания — все видимые
 * {@link AbstractWidget}, А ТАКЖЕ текстовые подписи экранов, реализующих
 * {@link DraggableTextProvider} (см. его javadoc — обычный
 * {@code drawString}/{@code blit} не имеет x/y полей, которые можно было бы
 * подвигать напрямую), можно двигать мышью, зажимая ЛКМ прямо по ним, без
 * обычного клика по кнопке (событие отменяется, пока режим активен).
 * Повторное F8 выключает режим и дампит x/y(+width/height у widget'ов) +
 * порядковый индекс + имя класса/метку каждого элемента в
 * {@code <gameDir>/stardew_debug_layout.json} — координаты в JSON РЕАЛЬНЫЕ
 * экранные пиксели на момент дампа, а не виртуальные единицы холста; при
 * переносе в {@code CharacterCreationLayout} нужно вычесть {@code offsetX/offsetY}
 * и поделить на {@code layout.scale}.
 *
 * Двигает даже ЗАДИЗЕЙБЛЕННЫЕ widget'ы (например, OK, пока не заполнены все
 * поля) — {@link AbstractWidget#isMouseOver} самого виджета возвращает
 * {@code false} для {@code active=false}, поэтому используется собственный
 * bounds-check, игнорирующий {@code active} (но не {@code visible} — невидимое
 * двигать незачем, мышью по нему всё равно не попасть).
 */
@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT)
public final class DebugLayoutDragTool {

    private static final Logger LOGGER = LoggerFactory.getLogger("StardewDebugLayout");

    /** Фиксированный hit-box вокруг DraggablePoint (у текста нет ширины/высоты как у widget'а). */
    private static final int POINT_HIT_HALF_WIDTH = 50;
    private static final int POINT_HIT_TOP = 4;
    private static final int POINT_HIT_BOTTOM = 12;

    private static boolean active = false;
    private static boolean f8Held = false;
    private static AbstractWidget draggingWidget;
    private static DraggablePoint draggingPoint;
    private static double dragOffsetX;
    private static double dragOffsetY;

    private DebugLayoutDragTool() {
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_F8) return;
        event.setCanceled(true);
        if (f8Held) return; // подавляем key-repeat, пока физическая клавиша ещё не отпущена
        f8Held = true;
        toggle(event.getScreen());
    }

    @SubscribeEvent
    public static void onKeyReleased(ScreenEvent.KeyReleased.Pre event) {
        if (event.getKeyCode() == GLFW.GLFW_KEY_F8) {
            f8Held = false;
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!active || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        AbstractWidget widget = findWidgetAt(event.getScreen(), mouseX, mouseY);
        if (widget != null) {
            draggingWidget = widget;
            dragOffsetX = mouseX - widget.getX();
            dragOffsetY = mouseY - widget.getY();
            event.setCanceled(true);
            return;
        }

        DraggablePoint point = findPointAt(event.getScreen(), mouseX, mouseY);
        if (point != null) {
            draggingPoint = point;
            dragOffsetX = mouseX - point.x;
            dragOffsetY = mouseY - point.y;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (draggingWidget == null && draggingPoint == null) return;
        int newX = (int) Math.round(event.getMouseX() - dragOffsetX);
        int newY = (int) Math.round(event.getMouseY() - dragOffsetY);
        if (draggingWidget != null) {
            draggingWidget.setX(newX);
            draggingWidget.setY(newY);
        } else {
            draggingPoint.x = newX;
            draggingPoint.y = newY;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (draggingWidget == null && draggingPoint == null) return;
        draggingWidget = null;
        draggingPoint = null;
        event.setCanceled(true);
    }

    private static void toggle(Screen screen) {
        active = !active;
        if (!active) {
            draggingWidget = null;
            draggingPoint = null;
            dumpToJson(screen);
        }
    }

    private static AbstractWidget findWidgetAt(Screen screen, double mouseX, double mouseY) {
        for (GuiEventListener child : screen.children()) {
            // Намеренно НЕ widget.isMouseOver() - тот возвращает false для
            // active=false (задизейбленные кнопки типа OK иначе не найти).
            if (child instanceof AbstractWidget widget && widget.visible
                    && mouseX >= widget.getX() && mouseX < widget.getX() + widget.getWidth()
                    && mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight()) {
                return widget;
            }
        }
        return null;
    }

    private static DraggablePoint findPointAt(Screen screen, double mouseX, double mouseY) {
        if (!(screen instanceof DraggableTextProvider provider)) return null;
        for (DraggablePoint point : provider.getDebugDraggablePoints()) {
            if (mouseX >= point.x - POINT_HIT_HALF_WIDTH && mouseX < point.x + POINT_HIT_HALF_WIDTH
                    && mouseY >= point.y - POINT_HIT_TOP && mouseY < point.y + POINT_HIT_BOTTOM) {
                return point;
            }
        }
        return null;
    }

    private static void dumpToJson(Screen screen) {
        JsonArray array = new JsonArray();
        int index = 0;
        for (GuiEventListener child : screen.children()) {
            if (!(child instanceof AbstractWidget widget)) continue;
            JsonObject obj = new JsonObject();
            obj.addProperty("index", index++);
            obj.addProperty("type", "widget");
            obj.addProperty("class", widget.getClass().getSimpleName());
            obj.addProperty("x", widget.getX());
            obj.addProperty("y", widget.getY());
            obj.addProperty("width", widget.getWidth());
            obj.addProperty("height", widget.getHeight());
            array.add(obj);
        }

        if (screen instanceof DraggableTextProvider provider) {
            List<DraggablePoint> points = provider.getDebugDraggablePoints();
            for (int i = 0; i < points.size(); i++) {
                DraggablePoint point = points.get(i);
                JsonObject obj = new JsonObject();
                obj.addProperty("index", i);
                obj.addProperty("type", "text");
                obj.addProperty("label", point.label);
                obj.addProperty("x", point.x);
                obj.addProperty("y", point.y);
                array.add(obj);
            }
        }

        // Имя файла включает класс экрана - раньше был один и тот же
        // "stardew_debug_layout.json" для ЛЮБОГО экрана, поэтому дамп с
        // Title-экрана перезаписывался дампом с Load, тот - дампом с
        // character creation и т.д. (см. чат - "перезаписывался при выходе
        // из него и заходе в другое"). Теперь у каждого экрана свой файл.
        String fileName = "stardew_debug_layout_" + screen.getClass().getSimpleName() + ".json";
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(array, writer);
            LOGGER.info("Stardew debug layout dumped to {}", path);
        } catch (IOException e) {
            LOGGER.error("Failed to dump Stardew debug layout", e);
        }
    }
}

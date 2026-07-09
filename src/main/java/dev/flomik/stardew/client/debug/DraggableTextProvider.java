package dev.flomik.stardew.client.debug;

import java.util.List;

/** Экран реализует это, чтобы {@link DebugLayoutDragTool} мог также найти и подвигать текстовые подписи (не только настоящие widget'ы). */
public interface DraggableTextProvider {

    List<DraggablePoint> getDebugDraggablePoints();
}

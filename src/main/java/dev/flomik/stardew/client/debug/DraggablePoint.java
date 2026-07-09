package dev.flomik.stardew.client.debug;

/**
 * Изменяемая позиция текста/декора, который рисуется напрямую
 * ({@code drawString}/{@code blit}), а не через настоящий {@code AbstractWidget} —
 * такие координаты {@link DebugLayoutDragTool} иначе не может ни найти под
 * мышью, ни сдвинуть (у них просто нет x/y полей). Экран регистрирует один
 * {@link DraggablePoint} на каждую подпись при её создании и в дальнейшем
 * рендерит текст ИЗ ЭТИХ ЖЕ полей (не из захваченных в лямбде final int) —
 * тогда перетаскивание точки мгновенно двигает и рендер.
 */
public final class DraggablePoint {

    public final String label;
    public int x;
    public int y;

    public DraggablePoint(String label, int x, int y) {
        this.label = label;
        this.x = x;
        this.y = y;
    }
}

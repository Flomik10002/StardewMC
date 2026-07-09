package dev.flomik.stardew.client.screen.title;

/**
 * Общий расчёт "кнопка растёт от своего центра при наведении" — переиспользуется
 * {@link TitleMainButton}/{@link TitleBackButton}/{@link TitleIconButton} (см.
 * чат: "кнопки при ? и настройки тоже с ховером делай"), чтобы не дублировать
 * одну и ту же арифметику трижды.
 */
final class HoverScale {

    record Rect(int x, int y, int width, int height) {
    }

    private HoverScale() {
    }

    static Rect scaled(int x, int y, int width, int height, float scale) {
        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(height * scale);
        return new Rect(x - (scaledWidth - width) / 2, y - (scaledHeight - height) / 2, scaledWidth, scaledHeight);
    }
}

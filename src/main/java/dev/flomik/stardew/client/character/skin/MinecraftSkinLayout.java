package dev.flomik.stardew.client.character.skin;

/**
 * Координаты областей стандартного 64×64 Minecraft-скина (classic/Steve
 * модель, "новый" формат с раздельными left arm/leg). Каждый массив — все 6
 * граней (right, front, left, back, top, bottom в этом порядке) одной части
 * тела одного слоя.
 *
 * layer2 везде используется: голова (под волосы), ноги (напуск штанины,
 * {@link #RIGHT_LEG_LAYER2}/{@link #LEFT_LEG_LAYER2}) и тело/руки (куртка/
 * рукава, {@link #BODY_LAYER2}/{@link #RIGHT_ARM_LAYER2}/{@link #LEFT_ARM_LAYER2}) -
 * рубашки красят и туда тоже (проверено побайтово).
 */
final class MinecraftSkinLayout {

    record Rect(int x, int y, int w, int h) {
    }

    // index: 0=right 1=front 2=left 3=back 4=top 5=bottom
    static final Rect[] HEAD_LAYER1 = {
            new Rect(0, 8, 8, 8), new Rect(8, 8, 8, 8), new Rect(16, 8, 8, 8),
            new Rect(24, 8, 8, 8), new Rect(8, 0, 8, 8), new Rect(16, 0, 8, 8)
    };

    static final Rect[] HEAD_LAYER2 = {
            new Rect(32, 8, 8, 8), new Rect(40, 8, 8, 8), new Rect(48, 8, 8, 8),
            new Rect(56, 8, 8, 8), new Rect(40, 0, 8, 8), new Rect(48, 0, 8, 8)
    };

    static final Rect[] BODY_LAYER1 = {
            new Rect(16, 20, 4, 12), new Rect(20, 20, 8, 12), new Rect(28, 20, 4, 12),
            new Rect(32, 20, 8, 12), new Rect(20, 16, 8, 4), new Rect(28, 16, 8, 4)
    };

    static final Rect[] RIGHT_ARM_LAYER1 = {
            new Rect(40, 20, 4, 12), new Rect(44, 20, 4, 12), new Rect(48, 20, 4, 12),
            new Rect(52, 20, 4, 12), new Rect(44, 16, 4, 4), new Rect(48, 16, 4, 4)
    };

    static final Rect[] LEFT_ARM_LAYER1 = {
            new Rect(32, 52, 4, 12), new Rect(36, 52, 4, 12), new Rect(40, 52, 4, 12),
            new Rect(44, 52, 4, 12), new Rect(36, 48, 4, 4), new Rect(40, 48, 4, 4)
    };

    static final Rect[] RIGHT_LEG_LAYER1 = {
            new Rect(0, 20, 4, 12), new Rect(4, 20, 4, 12), new Rect(8, 20, 4, 12),
            new Rect(12, 20, 4, 12), new Rect(4, 16, 4, 4), new Rect(8, 16, 4, 4)
    };

    static final Rect[] LEFT_LEG_LAYER1 = {
            new Rect(16, 52, 4, 12), new Rect(20, 52, 4, 12), new Rect(24, 52, 4, 12),
            new Rect(28, 52, 4, 12), new Rect(20, 48, 4, 4), new Rect(24, 48, 4, 4)
    };

    /**
     * "Напуск" штанины (та же геометрия ног, но чуть крупнее в 3D-модели) —
     * ваниль рендерит эти кубы ВСЕГДА, вне зависимости от того, знает ли наш
     * код об этой зоне. Реальные маски одежды (clothes/down/*_mask.png)
     * красят и сюда тоже (проверено побайтово) - без этих rect'ов такая
     * покраска шла бы неограниченным сырым блитом мимо любого учёта областей.
     */
    static final Rect[] RIGHT_LEG_LAYER2 = {
            new Rect(0, 36, 4, 12), new Rect(4, 36, 4, 12), new Rect(8, 36, 4, 12),
            new Rect(12, 36, 4, 12), new Rect(4, 32, 4, 4), new Rect(8, 32, 4, 4)
    };

    static final Rect[] LEFT_LEG_LAYER2 = {
            new Rect(0, 52, 4, 12), new Rect(4, 52, 4, 12), new Rect(8, 52, 4, 12),
            new Rect(12, 52, 4, 12), new Rect(4, 48, 4, 4), new Rect(8, 48, 4, 4)
    };

    /** "Куртка" - тот же напуск, что и у ног, но для тела/рук (рубашки красят и сюда, проверено побайтово). */
    static final Rect[] BODY_LAYER2 = {
            new Rect(16, 36, 4, 12), new Rect(20, 36, 8, 12), new Rect(28, 36, 4, 12),
            new Rect(32, 36, 8, 12), new Rect(20, 32, 8, 4), new Rect(28, 32, 8, 4)
    };

    static final Rect[] RIGHT_ARM_LAYER2 = {
            new Rect(40, 36, 4, 12), new Rect(44, 36, 4, 12), new Rect(48, 36, 4, 12),
            new Rect(52, 36, 4, 12), new Rect(44, 32, 4, 4), new Rect(48, 32, 4, 4)
    };

    static final Rect[] LEFT_ARM_LAYER2 = {
            new Rect(48, 52, 4, 12), new Rect(52, 52, 4, 12), new Rect(56, 52, 4, 12),
            new Rect(60, 52, 4, 12), new Rect(52, 48, 4, 4), new Rect(56, 48, 4, 4)
    };

    static final int FACE_RIGHT = 0;
    static final int FACE_FRONT = 1;
    static final int FACE_LEFT = 2;
    static final int FACE_BACK = 3;
    static final int FACE_TOP = 4;
    static final int FACE_BOTTOM = 5;

    private MinecraftSkinLayout() {
    }
}

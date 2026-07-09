package dev.flomik.stardew.client.character.skin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.character.CharacterModelType;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Регистрирует и переиспользует ОДНУ {@link DynamicTexture} для сгенерированного
 * скина (ТЗ §29 — не плодить texture resources на каждое изменение).
 *
 * Раньше "применение" к локальному игроку означало ЗАПИСЬ в приватные поля
 * {@code PlayerInfo} (через {@code PlayerInfoAccessor}) - и это была гонка:
 * {@code PlayerInfo.registerTextures()} асинхронно тянет НАСТОЯЩИЙ
 * (лицензионный) скин с Mojang и в какой-то момент после логина сам пишет в
 * ТУ ЖЕ карту - независимо от того, когда мы туда написали, "чья запись
 * останется последней" не гарантировалось (на экране создания персонажа
 * обычно успевали мы, т.к. там несколько секунд взаимодействия с GUI; на
 * простом перезаходе без GUI-паузы почти всегда успевал Mojang - именно это
 * и было замечено как баг: "после каждого захода ставится лицензионный").
 *
 * Теперь вместо записи - {@code mixin.client.PlayerInfoSkinLockMixin}
 * перехватывает сами МЕТОДЫ ЧТЕНИЯ ({@code PlayerInfo.getSkinLocation()}/
 * {@code getModelName()}, единственный путь, которым
 * {@code AbstractClientPlayer}/рендер узнают скин и модель руки) и для
 * локального игрока подменяет результат данными ОТСЮДА
 * ({@link #hasComposedSkin()}/{@link #getTextureLocation()}/
 * {@link #getModelName()}) - гонки нет в принципе, потому что мы никогда не
 * читаем то самое поле, которое Mojang-коллбэк может перезаписать.
 */
public final class RuntimeSkinManager {

    private static final ResourceLocation LOCAL_PLAYER_SKIN =
            new ResourceLocation(StardewMod.MODID, "dynamic_skin/local_player");

    private static DynamicTexture texture;
    private static CharacterModelType currentModelType = CharacterModelType.CLASSIC;
    /**
     * Последний ПОЛНЫЙ профиль (редактор или подтверждённое создание
     * персонажа) - хранится отдельно от того, что реально сейчас нарисовано
     * на скине, потому что {@link #refreshEquipmentOverride} рисует ЭТОТ ЖЕ
     * профиль, но с shirtId/pantsId, подменёнными по факту надетого предмета
     * (см. {@code ClothingEquipmentListener}) - не мутируя сам {@code profile},
     * который остаётся источником истины для мирового значка/капабилити.
     */
    private static CharacterProfile lastBaseProfile;

    private RuntimeSkinManager() {
    }

    /** Пересобирает скин из профиля и обновляет live-превью/локального игрока одной операцией. */
    public static void refresh(CharacterProfile profile) {
        lastBaseProfile = profile;
        composeAndUpload(profile);
    }

    /**
     * Живой пересчёт скина по факту надетого предмета одежды (см. чат: "в
     * редакторе голым быть нельзя, только в игре") - вызывается ТОЛЬКО из
     * {@code ClothingEquipmentListener} (реальная игра, смена экипировки), а
     * не из редактора. {@code shirtId/pantsId == "none"}, если слот пуст или
     * в нём не наш {@code ModItems.CLOTHING_SHIRT/PANTS} (см.
     * {@code ClothingStacks.clothingId}) - тогда {@code SkinComposer} рисует
     * fallback {@code no_clothes/*}. Если {@link #lastBaseProfile} ещё не
     * известен (эквип поменялся раньше первого {@link #refresh}), тихо
     * ничего не делает - применить оверрайд не к чему.
     *
     * {@code pantsColor} - {@code null}, если у надетых штанов нет тега
     * {@code ClothingColor} (пустой слот/чужой предмет) - тогда остаётся
     * цвет из {@link #lastBaseProfile}, а НЕ фиксированный дефолт (см. чат:
     * "надену шорты одного цвета, потом другие совсем другого цвета - будет
     * работать?"). Красит ОБЕ категории (рубашка И штаны) - у рубашки
     * своего цвета в GUI нет и не появилось, она по-прежнему красится в
     * цвет штанов (см. {@code SkinComposer}), просто теперь этот цвет может
     * реально смениться в рантайме сменой надетых штанов.
     */
    public static void refreshEquipmentOverride(String shirtId, String pantsId, Integer pantsColor) {
        if (lastBaseProfile == null) {
            return;
        }
        CharacterProfile effective = lastBaseProfile.copy();
        effective.setShirtId(shirtId);
        effective.setPantsId(pantsId);
        if (pantsColor != null) {
            effective.setPantsColor(pantsColor);
        }
        composeAndUpload(effective);
    }

    private static void composeAndUpload(CharacterProfile profile) {
        NativeImage composed = SkinComposer.compose(profile);
        DynamicTexture tex = getOrCreateTexture();
        tex.setPixels(composed); // сам закрывает предыдущий NativeImage - без утечки
        tex.upload();
        currentModelType = profile.getModelType();
    }

    /** {@code false} до первого {@link #refresh}, т.е. пока ещё нечем подменять скин игрока (см. PlayerInfoSkinLockMixin). */
    public static boolean hasComposedSkin() {
        return texture != null;
    }

    public static ResourceLocation getTextureLocation() {
        getOrCreateTexture();
        return LOCAL_PLAYER_SKIN;
    }

    /** "default" = Steve (широкие руки), "slim" = Alex (узкие) - те же строки, что Mojang кладёт в metadata настоящих скинов. */
    public static String getModelName() {
        return currentModelType == CharacterModelType.SLIM ? "slim" : "default";
    }

    private static DynamicTexture getOrCreateTexture() {
        if (texture == null) {
            texture = new DynamicTexture(new NativeImage(64, 64, true));
            Minecraft.getInstance().getTextureManager().register(LOCAL_PLAYER_SKIN, texture);
        }
        return texture;
    }
}

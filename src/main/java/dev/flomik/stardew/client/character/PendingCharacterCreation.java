package dev.flomik.stardew.client.character;

import dev.flomik.stardew.common.module.character.network.C2SFinishCharacterCreation;

/**
 * "Полка" для профиля, собранного на pre-world {@code CharacterCreationScreen}
 * (кнопка "New" на титуле, см. {@code StardewPlayButtonHandler}), пока клиент
 * создаёт мир и подключается к нему (см. {@code CharacterCreationScreen#onOkPressed}).
 * Сервер этого свежесозданного мира неизбежно пришлёт {@code S2COpenCharacterCreation}
 * (у него, как у любого нового профиля, {@code isCharacterCreated()==false}) -
 * {@link ClientCharacterCreationOpener#openOrAutoSubmit()} находит здесь уже
 * подтверждённые игроком данные и отправляет их сам, вместо повторного показа
 * экрана редактирования.
 */
public final class PendingCharacterCreation {

    private static C2SFinishCharacterCreation pending;

    private PendingCharacterCreation() {
    }

    public static void stash(C2SFinishCharacterCreation msg) {
        pending = msg;
    }

    public static boolean hasPending() {
        return pending != null;
    }

    /** Забирает отложенный профиль и сразу очищает полку - вызывать не чаще одного раза на подключение. */
    public static C2SFinishCharacterCreation takeAndClear() {
        C2SFinishCharacterCreation msg = pending;
        pending = null;
        return msg;
    }

    public static void clear() {
        pending = null;
    }
}

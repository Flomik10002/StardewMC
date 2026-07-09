package dev.flomik.stardew.client.character;

import com.mojang.blaze3d.platform.NativeImage;
import dev.flomik.stardew.client.character.skin.RuntimeSkinManager;
import dev.flomik.stardew.client.character.skin.WorldIconComposer;
import dev.flomik.stardew.client.screen.character.CharacterCreationScreen;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Единственная точка, где сетевой common-код (см.
 * {@code common.module.character.network}) касается клиентских классов —
 * всегда через {@code DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)}, никогда
 * прямым импортом в самом packet-классе. CLAUDE.md отдельно просит не плодить
 * больше common-классов с прямыми ссылками на client/Minecraft (существующий
 * {@code S2COpenShippingResultScreen} — уже известный технический долг, не
 * образец для подражания).
 */
public final class ClientCharacterCreationOpener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCharacterCreationOpener.class);

    private ClientCharacterCreationOpener() {
    }

    /**
     * Экран всегда открывается с фиксированным {@code guiScale=2}, независимо
     * от настройки игрока - "ВСЕГДА" по ТЗ. Меняем ДО открытия экрана (а не
     * внутри {@code Screen.init()}), потому что {@code resizeDisplay()} сам
     * вызывает {@code screen.resize()} -> {@code init()} повторно, и делать
     * это изнутри собственного init() было бы реентерабельно/небезопасно.
     */
    private static final int FORCED_GUI_SCALE = 2;

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        int previousGuiScale = minecraft.options.guiScale().get();
        if (previousGuiScale != FORCED_GUI_SCALE) {
            minecraft.options.guiScale().set(FORCED_GUI_SCALE);
            minecraft.resizeDisplay();
        }
        minecraft.setScreen(new CharacterCreationScreen(previousGuiScale));
    }

    /**
     * Вызывается вместо {@link #open()} из {@code S2COpenCharacterCreation}
     * (см. её javadoc) - по этому пакету сервер просит показать экран
     * создания персонажа КАЖДЫЙ раз, когда у профиля {@code !isCharacterCreated()},
     * включая только что созданный самим клиентом pre-world мир (см.
     * {@code CharacterCreationScreen#onOkPressed}, ветка {@code preWorld}).
     * Если на {@link PendingCharacterCreation} лежат данные, уже подтверждённые
     * игроком на pre-world экране, - отправляем их сами и НЕ открываем экран
     * второй раз. Иначе (обычный случай - свежий вход без pre-world шага,
     * либо возобновление брошенного сейва через Load) - как раньше, {@link #open()}.
     */
    public static void openOrAutoSubmit() {
        if (PendingCharacterCreation.hasPending()) {
            PacketHandler.sendToServer(PendingCharacterCreation.takeAndClear());
            return;
        }
        open();
    }

    /**
     * {@code S2CCharacterCreationAccepted} шлётся в двух случаях: сразу после
     * успешного OK на creation screen, и на КАЖДОМ следующем логине уже
     * созданного персонажа ({@link dev.flomik.stardew.common.module.character.event.FirstJoinController}) —
     * во втором случае экран не открыт, но скин пересобрать всё равно нужно
     * (ТЗ §31, AC-20). {@code refresh()} тут безусловно, а не внутри Screen -
     * дальше скин "запирается" перманентно через
     * {@code mixin.client.PlayerInfoSkinLockMixin} (см. его javadoc), больше
     * ничего явно "применять" не нужно.
     */
    public static void onAccepted(CharacterProfile confirmedProfile) {
        RuntimeSkinManager.refresh(confirmedProfile);
        writeWorldIcon(confirmedProfile);

        if (Minecraft.getInstance().screen instanceof CharacterCreationScreen screen) {
            screen.onServerAccepted();
        }
    }

    /**
     * Иконка мира ({@code icon.png}) - плоский портрет "по пояс" из уже
     * готовой composed-текстуры скина (см. {@link WorldIconComposer}).
     * Только singleplayer ({@code getSingleplayerServer() == null} и на
     * dedicated-сервере, и в гостях на чужом - иконка это файл в ЛОКАЛЬНОЙ
     * save-папке, писать её имеет смысл только хосту мира), что совпадает с
     * тем, что весь текущий world-flow мода (см. {@code WorldTemplateManager})
     * и так только про singleplayer.
     */
    private static void writeWorldIcon(CharacterProfile profile) {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return;

        try (NativeImage icon = WorldIconComposer.compose(profile)) {
            icon.writeToFile(server.getWorldPath(LevelResource.ICON_FILE));
        } catch (IOException e) {
            LOGGER.warn("[Stardew] Failed to write world icon", e);
        }
    }

    /**
     * Обычный случай - экран ещё открыт (игрок кликнул OK сам), просто
     * показываем ошибку на нём. Но если это был auto-submit
     * ({@link #openOrAutoSubmit()}) - экран уже закрыт (пока сервер отвечал,
     * client успел уйти в геймплей) и показывать ошибку негде. Это крайне
     * маловероятно (клиент уже провалидировал поля перед тем, как положить
     * их на полку), но должно быть обработано - иначе игрок навсегда
     * застрянет в мире без персонажа. Открываем экран заново (обычный,
     * НЕ pre-world режим - мир-то уже существует) и показываем ошибку на нём.
     */
    public static void onRejected(String reasonKey) {
        if (Minecraft.getInstance().screen instanceof CharacterCreationScreen screen) {
            screen.onServerRejected(reasonKey);
            return;
        }

        LOGGER.warn("[Stardew] Auto-submitted character creation rejected with no screen open: {}", reasonKey);
        open();
        if (Minecraft.getInstance().screen instanceof CharacterCreationScreen screen) {
            screen.onServerRejected(reasonKey);
        }
    }
}

package dev.flomik.stardew.client.character.worldtemplate;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Копирует подготовленный шаблон мира (см. docs/world-template.md) в новый,
 * независимый save-слот игрока и открывает его напрямую — без экрана
 * создания мира, генерировать тут нечего, terrain уже существует.
 *
 * v1: единственный источник шаблона — {@code <gameDir>/stardew_templates/<id>/},
 * заполняется вручную в dev-окружении копией {@code map/SV} (см. чат/доку —
 * реальная доставка шаблона игрокам, release asset/кэш, ещё не реализована).
 */
public final class WorldTemplateManager {

    /** Имя SavedData-файла с признаком "это подготовленный Stardew-мир" — совпадает с StardewWorldMarker.NAME. */
    private static final String WORLD_MARKER_FILE = "stardew_world_marker.dat";

    private WorldTemplateManager() {
    }

    public static Path templatesRoot() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("stardew_templates");
    }

    public static boolean templateExists(String templateId) {
        return Files.isDirectory(templatesRoot().resolve(templateId));
    }

    /**
     * @param templateId  id шаблона под {@link #templatesRoot()}
     * @param desiredName желаемое имя новой фермы; если папка с таким именем
     *                    уже занята, будет подобрано ближайшее свободное
     *                    (см. {@code selectWorld.recreate} - тот же принцип)
     * @return фактически использованное имя save-папки (== levelId для {@code WorldOpenFlows#loadLevel})
     */
    public static String createFromTemplate(String templateId, String desiredName) throws IOException {
        Path templateSource = templatesRoot().resolve(templateId);
        if (!Files.isDirectory(templateSource)) {
            throw new IOException("Template not found: " + templateSource);
        }

        LevelStorageSource storageSource = Minecraft.getInstance().getLevelSource();
        Path baseDir = storageSource.getBaseDir();
        Files.createDirectories(baseDir);

        String levelId = net.minecraft.FileUtil.findAvailableName(baseDir, desiredName, "");
        Path targetDir = baseDir.resolve(levelId);

        copyDirectory(templateSource, targetDir);
        patchLevelData(targetDir.resolve("level.dat"), desiredName);
        resetDateData(targetDir);
        seedWorldMarker(targetDir);

        return levelId;
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** Точка спавна нового мира (ТЗ: "координаты 8 117 20") - используется world spawn, раз Player-тега больше нет (см. ниже). */
    private static final int SPAWN_X = 8, SPAWN_Y = 117, SPAWN_Z = 20;

    /**
     * Переименовывает ферму И приводит {@code level.dat} в состояние
     * "только что созданный мир", а не "снимок dev-тестовой сессии":
     *
     * - {@code Data.Player} у шаблона содержит вагон тестового мусора
     *   (инвентарь, деньги, XP, creative instabuild/mayfly и т.п. - см. чат) -
     *   и это именно тот тег, который ваниль использует как ДЕФОЛТНОГО игрока
     *   для UUID, у которого ещё нет своего {@code playerdata/<uuid>.dat}
     *   (а в шаблоне {@code playerdata/} вообще нет папки) - то есть КАЖДЫЙ
     *   новый игрок наследовал бы это состояние целиком. Тег удаляется
     *   полностью - ваниль сама создаёт свежего игрока (полное здоровье/голод,
     *   пустой инвентарь, survival) при первом входе.
     * - {@code Data.SpawnX/Y/Z} - мировая точка спавна, используется именно
     *   когда Player-тега нет вообще (см. выше) - без неё игрок появился бы
     *   в дефолтных (0, heightmap, 0).
     * - {@code Data.DayTime} = 0 - 6:00 утра (см. {@code StardewTimeUtils.toTicks(6,0)}),
     *   а не то время суток, в которое шаблон случайно был сохранён
     *   (в этом шаблоне - 21:00, глубокая ночь).
     */
    private static void patchLevelData(Path levelDat, String newName) throws IOException {
        if (!Files.isRegularFile(levelDat)) return;

        CompoundTag root = NbtIo.readCompressed(levelDat.toFile());
        CompoundTag data = root.getCompound("Data");

        data.putString("LevelName", newName);
        data.putLong("DayTime", 0L);
        data.remove("Player");
        data.putInt("SpawnX", SPAWN_X);
        data.putInt("SpawnY", SPAWN_Y);
        data.putInt("SpawnZ", SPAWN_Z);

        root.put("Data", data);
        NbtIo.writeCompressed(root, levelDat.toFile());
    }

    /**
     * Удаляет скопированный из шаблона {@code data/stardew_date.dat} (см.
     * чат: "почему у нас генерятся миры то 26 день лета, то 1й день весны" -
     * шаблон это снимок dev-тестовой сессии, где дата продвинута до дня 26
     * лета, а не просто "новый мир" - copyDirectory тащит этот файл как
     * есть, тот же баг для {@code level.dat} уже чинит {@link #patchLevelData}).
     * Не переписываем файл вручную (риск разойтись форматом с
     * {@code StardewDateData.save}) - просто убираем его: {@code StardewDateData.get()}
     * при первом же обращении к {@code getDataStorage().computeIfAbsent}
     * не найдёт файл и создаст свежий {@code new StardewDateData()} сам -
     * день 1 весны, totalDays=0, {@code weatherInitialized=false} (запустит
     * нормальную инициализацию погоды), ровно как у мира "с нуля".
     */
    private static void resetDateData(Path targetDir) throws IOException {
        Files.deleteIfExists(targetDir.resolve("data").resolve("stardew_date.dat"));
    }

    /**
     * Пишет {@code data/stardew_world_marker.dat} с {@code stardewWorld=true}
     * заранее, тем же форматом, что и обычный {@code SavedData} (root
     * compound с вложенным "data" и "DataVersion") — так при первом же
     * {@code StardewWorldMarker.get(level)} игра прочитает его как самый
     * обычный, уже существующий файл, без специального кода под "это из
     * шаблона".
     */
    private static void seedWorldMarker(Path worldDir) throws IOException {
        Path dataDir = worldDir.resolve("data");
        Files.createDirectories(dataDir);

        CompoundTag payload = new CompoundTag();
        payload.putBoolean("stardewWorld", true);

        CompoundTag root = new CompoundTag();
        root.put("data", payload);
        root.putInt("DataVersion", SharedConstants.WORLD_VERSION);

        NbtIo.writeCompressed(root, dataDir.resolve(WORLD_MARKER_FILE).toFile());
    }
}

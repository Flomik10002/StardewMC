package dev.flomik.stardew.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.flomik.stardew.StardewMod;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Генерирует {@code assets/stardew/character/shirts.json} из папок
 * {@code character/clothes/shirts/<NNN>/} — каждая папка это ОДНА рубашка
 * ({@code shirt_<NNN>}), внутри которой могут лежать (любое подмножество):
 * {@code base_male.png}, {@code mask_male.png}, {@code pants_overlay_male.png},
 * {@code base_female.png}, {@code mask_female.png}, {@code pants_overlay_female.png}.
 * {@code pants_overlay_*} — опциональный неизменяемый слой, который
 * {@code SkinComposer} рисует ТОЛЬКО когда на игроке есть штаны (см.
 * {@code SkinComposer.applyShirtCosmetic}), между {@code base} и {@code mask}.
 * Не нужно вручную поддерживать shirts.json в актуальном состоянии —
 * добавили/убрали папку под {@code clothes/shirts/}, перезапустили
 * {@code ./gradlew runData}, файл пересобрался сам.
 *
 * {@code supportsColor} выставляется в {@code true}, если у ЛЮБОЙ из
 * сторон (male/female) есть mask-файл - маска красится в выбранный цвет
 * (см. {@code SkinComposer.applyTintMask}), просто {@code texture} без маски
 * значит фикс-цвет, перекрашивать нечего.
 *
 * Ассеты читаются из {@code src/main/resources} (руками разложенные
 * PNG), а НЕ из {@link PackOutput} - тот описывает КУДА писать
 * сгенерированное, а не откуда читать существующее. Путь считается
 * относительно рабочей директории Gradle-рана ({@code run/}, см.
 * {@code build.gradle}'s {@code workingDirectory}), поэтому поднимаемся на
 * уровень вверх - если когда-нибудь workingDirectory изменится, этот путь
 * тоже придётся поправить.
 */
public class ShirtCosmeticProvider implements DataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShirtCosmeticProvider.class);
    private static final Path SOURCE_ROOT = Path.of("../src/main/resources/assets/stardew/textures/character/clothes/shirts");
    private static final Pattern NUMBER_FOLDER = Pattern.compile("\\d+");

    private final PackOutput output;

    public ShirtCosmeticProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonArray shirts = new JsonArray();

        for (Path shirtDir : listShirtFolders()) {
            String number = shirtDir.getFileName().toString();
            boolean maleBase = Files.exists(shirtDir.resolve("base_male.png"));
            boolean maleMask = Files.exists(shirtDir.resolve("mask_male.png"));
            boolean maleOverlay = Files.exists(shirtDir.resolve("pants_overlay_male.png"));
            boolean femaleBase = Files.exists(shirtDir.resolve("base_female.png"));
            boolean femaleMask = Files.exists(shirtDir.resolve("mask_female.png"));
            boolean femaleOverlay = Files.exists(shirtDir.resolve("pants_overlay_female.png"));

            if (!maleBase && !maleMask && !femaleBase && !femaleMask) {
                LOGGER.warn("[Stardew] shirts/{} has no base_*/mask_* files, skipping", number);
                continue;
            }

            JsonObject entry = new JsonObject();
            entry.addProperty("id", "shirt_" + number);
            entry.addProperty("supportsColor", maleMask || femaleMask);

            String folder = "character/clothes/shirts/" + number;
            if (maleBase || maleMask || maleOverlay) {
                entry.add("male", variant(folder, "male", maleBase, maleMask, maleOverlay));
            }
            if (femaleBase || femaleMask || femaleOverlay) {
                entry.add("female", variant(folder, "female", femaleBase, femaleMask, femaleOverlay));
            }
            shirts.add(entry);
        }

        ResourceLocation id = new ResourceLocation(StardewMod.MODID, "shirts");
        Path target = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "character").json(id);
        return DataProvider.saveStable(cache, shirts, target);
    }

    private static JsonObject variant(String folder, String side, boolean hasBase, boolean hasMask, boolean hasPantsOverlay) {
        JsonObject variant = new JsonObject();
        // base_*.png - сама рубашка (неизменяемая), mask_*.png - оверлей
        // комбинезона (перекрашивается в цвет штанов). Порядок отрисовки:
        // texture -> pants_overlay_*.png (только если есть штаны) -> mask
        // (только если есть штаны) - см. SkinComposer.applyShirtCosmetic.
        if (hasBase) {
            variant.addProperty("texture", folder + "/base_" + side + ".png");
        }
        if (hasPantsOverlay) {
            variant.addProperty("pantsOverlay", folder + "/pants_overlay_" + side + ".png");
        }
        if (hasMask) {
            variant.addProperty("mask", folder + "/mask_" + side + ".png");
        }
        return variant;
    }

    /** Папки с числовым именем, отсортированные численно (не лексикографически - иначе "10" оказался бы перед "2"). */
    private static List<Path> listShirtFolders() {
        if (!Files.isDirectory(SOURCE_ROOT)) {
            LOGGER.warn("[Stardew] Shirt textures folder missing: {}", SOURCE_ROOT.toAbsolutePath());
            return List.of();
        }
        try (Stream<Path> children = Files.list(SOURCE_ROOT)) {
            List<Path> folders = new ArrayList<>();
            children.filter(Files::isDirectory)
                    .filter(p -> NUMBER_FOLDER.matcher(p.getFileName().toString()).matches())
                    .forEach(folders::add);
            folders.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString())));
            return folders;
        } catch (IOException e) {
            LOGGER.error("[Stardew] Failed to list {}", SOURCE_ROOT, e);
            return List.of();
        }
    }

    @Override
    public String getName() {
        return "Stardew Shirt Cosmetics";
    }
}

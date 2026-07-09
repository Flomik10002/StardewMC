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
 * Генерирует {@code assets/stardew/character/hairs.json} из папок
 * {@code character/hair/<NNN>/} — каждая папка это ОДНА причёска
 * ({@code hair_<NNN>}), внутри которой может лежать (любое подмножество):
 * {@code mask_male.png}, {@code mask_female.png} — тот же принцип, что и у
 * {@link ShirtCosmeticProvider}, только без {@code base_*.png}: у волос НЕТ
 * отдельного {@code texture} (неизменяемого оверлея) — вся причёска красится
 * через ОДНУ маску, никакой "фиксированной" нетонируемой части у неё нет.
 *
 * male/female тут — НЕ про ширину рук (как у рубашек), а про то, какие
 * причёски вообще предлагаются мужской/женской модели: рандомайзер
 * ({@code CharacterCreationScreen.randomHairId}) фильтрует по полу и берёт
 * ТОЛЬКО причёски с вариантом под текущий пол, а обычное пролистывание
 * стрелками — нет (см. чат: "лок на рандомайзере, но не при обычном
 * редактировании"), пользуясь тем же fallback'ом
 * {@code CosmeticDefinition#variantFor}, что и рубашки (SLIM без своего
 * female-варианта тихо показывает male).
 *
 * Старый плоский пак ({@code <NNN>_base.png}/{@code <NNN>_mask.png} прямо в
 * папке) переехал в {@code character/hair_deprecated/} как есть, без
 * реорганизации — реальную раскладку по папкам/полу делает пользователь
 * вручную.
 */
public class HairCosmeticProvider implements DataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HairCosmeticProvider.class);
    private static final Path SOURCE_ROOT = Path.of("../src/main/resources/assets/stardew/textures/character/hair");
    private static final Pattern NUMBER_FOLDER = Pattern.compile("\\d+");

    private final PackOutput output;

    public HairCosmeticProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonArray hairs = new JsonArray();

        for (Path hairDir : listHairFolders()) {
            String number = hairDir.getFileName().toString();
            boolean maleMask = Files.exists(hairDir.resolve("mask_male.png"));
            boolean femaleMask = Files.exists(hairDir.resolve("mask_female.png"));

            if (!maleMask && !femaleMask) {
                LOGGER.warn("[Stardew] hair/{} has no mask_male.png/mask_female.png, skipping", number);
                continue;
            }

            JsonObject entry = new JsonObject();
            entry.addProperty("id", "hair_" + number);
            entry.addProperty("supportsColor", true);

            String folder = "character/hair/" + number;
            if (maleMask) {
                entry.add("male", variant(folder, "male"));
            }
            if (femaleMask) {
                entry.add("female", variant(folder, "female"));
            }
            hairs.add(entry);
        }

        ResourceLocation id = new ResourceLocation(StardewMod.MODID, "hairs");
        Path target = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "character").json(id);
        return DataProvider.saveStable(cache, hairs, target);
    }

    private static JsonObject variant(String folder, String side) {
        JsonObject variant = new JsonObject();
        variant.addProperty("mask", folder + "/mask_" + side + ".png");
        return variant;
    }

    /** Папки с числовым именем, отсортированные численно (не лексикографически - иначе "10" оказался бы перед "2"). */
    private static List<Path> listHairFolders() {
        if (!Files.isDirectory(SOURCE_ROOT)) {
            LOGGER.warn("[Stardew] Hair textures folder missing: {}", SOURCE_ROOT.toAbsolutePath());
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
        return "Stardew Hair Cosmetics";
    }
}

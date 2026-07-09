package dev.flomik.stardew.common.module.character.cosmetic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data-driven каталог косметики (ТЗ §52) — списки в JSON под
 * {@code assets/stardew/character/*.json}, а не {@code if (hair == 1) ...} в
 * Java. Порядок элементов в JSON — это же порядок пролистывания стрелками
 * {@code [←] [→]} в GUI, поэтому используется {@link LinkedHashMap}, а не
 * обычный {@code HashMap} (тот не гарантирует порядок).
 *
 * Читается напрямую через classloader ({@link Class#getResourceAsStream}), а
 * не через Minecraft {@code ResourceManager} — тот на dedicated-сервере не
 * видит {@code assets/} вообще (это client resource pack namespace), а ID
 * косметики нужно валидировать и на сервере (ТЗ §33). Полноценный
 * datapack-driven reload (ТЗ §53, п.4 "перезагрузить ресурсы") — открытый
 * вопрос на будущее.
 */
public final class CosmeticRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmeticRegistry.class);

    private static final Map<String, SkinToneDefinition> SKINS = new LinkedHashMap<>();
    private static final Map<String, CosmeticDefinition> HAIRS = new LinkedHashMap<>();
    private static final Map<String, CosmeticDefinition> SHIRTS = new LinkedHashMap<>();
    private static final Map<String, CosmeticDefinition> PANTS = new LinkedHashMap<>();
    private static final Map<String, CosmeticDefinition> ACCESSORIES = new LinkedHashMap<>();

    private CosmeticRegistry() {
    }

    public static void load() {
        SKINS.clear();
        HAIRS.clear();
        SHIRTS.clear();
        PANTS.clear();
        ACCESSORIES.clear();

        loadSkins("skins.json");
        loadCosmetics("hairs.json", HAIRS);
        // allowNoneOnly - "нет рубашки"/"нет штанов" (см. чат: "когда игрок
        // без одежды") такой же реальный выбор, как "нет аксессуара" -
        // SkinComposer рисует шейдер no_clothes/* именно когда applyCosmetic
        // для "none" (variant без texture/mask) естественно возвращает false.
        loadCosmetics("shirts.json", SHIRTS, true);
        loadCosmetics("pants.json", PANTS, true);
        loadCosmetics("accessories.json", ACCESSORIES, true);

        LOGGER.info("[Stardew] Cosmetic registry loaded: {} skins, {} hairs, {} shirts, {} pants, {} accessories",
                SKINS.size(), HAIRS.size(), SHIRTS.size(), PANTS.size(), ACCESSORIES.size());
    }

    private static JsonArray readJsonArray(String fileName) {
        String path = "/assets/stardew/character/" + fileName;
        try (InputStream stream = CosmeticRegistry.class.getResourceAsStream(path)) {
            if (stream == null) {
                LOGGER.warn("[Stardew] Cosmetic definition file missing: {}", path);
                return new JsonArray();
            }
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonElement root = JsonParser.parseReader(reader);
                return root.getAsJsonArray();
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("[Stardew] Failed to parse {}", path, e);
            return new JsonArray();
        }
    }

    private static void loadSkins(String fileName) {
        for (JsonElement element : readJsonArray(fileName)) {
            JsonObject obj = element.getAsJsonObject();
            String id = obj.get("id").getAsString();
            int base = parseHex(obj.get("color").getAsString());
            SKINS.put(id, new SkinToneDefinition(id, base));
        }
    }

    private static void loadCosmetics(String fileName, Map<String, CosmeticDefinition> target) {
        loadCosmetics(fileName, target, false);
    }

    private static void loadCosmetics(String fileName, Map<String, CosmeticDefinition> target, boolean allowNoneOnly) {
        for (JsonElement element : readJsonArray(fileName)) {
            JsonObject obj = element.getAsJsonObject();
            String id = obj.get("id").getAsString();
            boolean supportsColor = obj.has("supportsColor") && obj.get("supportsColor").getAsBoolean();

            CosmeticVariant male;
            CosmeticVariant female;
            if (obj.has("male") || obj.has("female")) {
                // Одежда, которую видно на руках (в первую очередь shirt) -
                // classic/slim разной ширины, нужны отдельные ассеты.
                male = parseVariant(obj.getAsJsonObject("male"));
                female = obj.has("female") ? parseVariant(obj.getAsJsonObject("female")) : null;
            } else {
                // Плоская схема (штаны/волосы/аксессуары - модель руки тут
                // не важна) - тот же CosmeticVariant в оба поля.
                CosmeticVariant flat = parseVariant(obj);
                male = flat;
                female = flat;
            }
            target.put(id, new CosmeticDefinition(id, male, female, supportsColor));
        }
        if (allowNoneOnly && !target.containsKey("none")) {
            target.put("none", new CosmeticDefinition("none", null, null, false));
        }
    }

    private static CosmeticVariant parseVariant(JsonObject obj) {
        if (obj == null) return null;
        String texture = obj.has("texture") ? obj.get("texture").getAsString() : null;
        String mask = obj.has("mask") ? obj.get("mask").getAsString() : null;
        String pantsOverlay = obj.has("pantsOverlay") ? obj.get("pantsOverlay").getAsString() : null;
        return new CosmeticVariant(texture, mask, pantsOverlay);
    }

    private static int parseHex(String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        return 0xFF000000 | Integer.parseInt(cleaned, 16);
    }

    // --- Доступ ---

    public static List<String> skinIds() { return new ArrayList<>(SKINS.keySet()); }
    public static List<String> hairIds() { return new ArrayList<>(HAIRS.keySet()); }
    public static List<String> shirtIds() { return new ArrayList<>(SHIRTS.keySet()); }
    public static List<String> pantsIds() { return new ArrayList<>(PANTS.keySet()); }
    public static List<String> accessoryIds() { return new ArrayList<>(ACCESSORIES.keySet()); }

    public static SkinToneDefinition skin(String id) {
        return SKINS.getOrDefault(id, fallback(SKINS, new SkinToneDefinition(id, 0xFFD9A17E)));
    }

    public static CosmeticDefinition hair(String id) { return HAIRS.getOrDefault(id, fallback(HAIRS, HAIRS.values().stream().findFirst().orElse(null))); }
    public static CosmeticDefinition shirt(String id) { return SHIRTS.getOrDefault(id, fallback(SHIRTS, SHIRTS.values().stream().findFirst().orElse(null))); }
    public static CosmeticDefinition pants(String id) { return PANTS.getOrDefault(id, fallback(PANTS, PANTS.values().stream().findFirst().orElse(null))); }
    public static CosmeticDefinition accessory(String id) { return ACCESSORIES.getOrDefault(id, new CosmeticDefinition("none", null, null, false)); }

    public static boolean isValidSkin(String id) { return SKINS.containsKey(id); }
    public static boolean isValidHair(String id) { return HAIRS.containsKey(id); }
    public static boolean isValidShirt(String id) { return SHIRTS.containsKey(id); }
    public static boolean isValidPants(String id) { return PANTS.containsKey(id); }
    public static boolean isValidAccessory(String id) { return ACCESSORIES.containsKey(id); }

    /** Удалённый/повреждённый ID (ТЗ §57 "Не существует сохранённая причёска") — fallback вместо падения. */
    private static <T> T fallback(Map<String, T> map, T fallbackValue) {
        return fallbackValue;
    }
}

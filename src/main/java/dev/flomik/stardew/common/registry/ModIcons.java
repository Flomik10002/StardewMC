package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.StardewMod;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Регистрация кастомных иконок для отображения в тексте.
 * Иконки используют Unicode Private Use Area (U+E000 - U+F8FF).
 */
public class ModIcons {
    
    private static final Map<Integer, IconData> ICON_REGISTRY = new HashMap<>();
    
    // Стандартные иконки 10x10
    public static final String ENERGY = icon(0xE000, "energy", 12f);
    public static final String HEALTH = icon(0xE001, "health", 12f);
    public static final String SKULL = icon(0xE006, "skull", 12f);
    public static final String MONEY = icon(0xE002, "money", 10f);
    
    // Звездочки качества 10x10
    public static final String STAR_SILVER = icon(0xE003, "silver_star", 8f);
    public static final String STAR_GOLD = icon(0xE004, "gold_star", 8f);
    public static final String STAR_IRIDIUM = icon(0xE005, "iridium_star", 8f);

    // Звездочка зачарования 10x10
    public static final String STAR_ENCHANTMENT = icon(0xE007, "enchantment_star", 12f);

    /**
     * Регистрирует иконку и возвращает её Unicode строку для использования в тексте.
     * 
     * @param codePoint Unicode код (0xE000 - 0xE0FF рекомендуется)
     * @param textureName Имя текстуры без расширения (например, "energy" -> "textures/gui/icons/energy.png")
     * @param size Размер иконки в пикселях (обычно 10f или 12f)
     * @return Unicode строка для вставки в текст (например, "\uE000")
     */
    private static String icon(int codePoint, String textureName, float size) {
        ResourceLocation texture = new ResourceLocation(StardewMod.MODID, "textures/gui/icons/" + textureName + ".png");
        ICON_REGISTRY.put(codePoint, new IconData(texture, size));
        return String.valueOf((char) codePoint);
    }
    
    /**
     * Получить текстуру иконки по её Unicode коду.
     * Используется внутри {@link dev.flomik.stardew.client.render.StardewFontRenderer}.
     * 
     * @param codePoint Unicode код символа
     * @return ResourceLocation текстуры или null, если иконка не зарегистрирована
     */
    public static ResourceLocation getTexture(int codePoint) {
        IconData data = ICON_REGISTRY.get(codePoint);
        return data != null ? data.texture : null;
    }
    
    /**
     * Получить размер иконки по её Unicode коду.
     * 
     * @param codePoint Unicode код символа
     * @return Размер иконки или 10f по умолчанию
     */
    public static float getSize(int codePoint) {
        IconData data = ICON_REGISTRY.get(codePoint);
        return data != null ? data.size : 10f;
    }
    
    /**
     * Проверяет, является ли символ зарегистрированной иконкой.
     * 
     * @param codePoint Unicode код символа
     * @return true, если это иконка
     */
    public static boolean isIcon(int codePoint) {
        return ICON_REGISTRY.containsKey(codePoint);
    }
    
    /**
     * Данные иконки: текстура и размер.
     */
    private static class IconData {
        final ResourceLocation texture;
        final float size;
        
        IconData(ResourceLocation texture, float size) {
            this.texture = texture;
            this.size = size;
        }
    }
    
    /**
     * Инициализация класса (вызывается автоматически при первом обращении).
     */
    public static void init() {
        StardewMod.LOGGER.info("Registered {} custom icons", ICON_REGISTRY.size());
    }
}


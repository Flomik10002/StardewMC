package dev.flomik.stardew.client.screen.shipping;

import dev.flomik.stardew.StardewMod;
import net.minecraft.resources.ResourceLocation;

/**
 * Все текстуры для экрана результатов продаж.
 */
public class ShippingTextures {
    
    private static final String PATH = "textures/gui/shipping/";
    
    // ФОНЫ
    public static final ResourceLocation NIGHT_SKY = tex("bg_night_sky.png");
    public static final ResourceLocation RAINY_SKY = tex("bg_rainy_sky.png");
    public static final ResourceLocation WINTER_SKY = tex("bg_winter_sky.png");
    public static final ResourceLocation GROUND = tex("bg_ground.png");
    public static final ResourceLocation GROUND_WINTER = tex("bg_ground_winter.png");
    
    // КНОПКИ
    public static final ResourceLocation BTN_OK = tex("btn_ok.png");
    public static final ResourceLocation BTN_OK_HOVER = tex("btn_ok_hover.png");
    public static final ResourceLocation BTN_BACK = tex("btn_back.png");
    public static final ResourceLocation BTN_BACK_HOVER = tex("btn_back_hover.png");
    public static final ResourceLocation BTN_FORWARD = tex("btn_forward.png");
    public static final ResourceLocation BTN_FORWARD_HOVER = tex("btn_forward_hover.png");
    public static final ResourceLocation BTN_PLUS = tex("btn_plus.png");
    public static final ResourceLocation BTN_PLUS_HOVER = tex("btn_plus_hover.png");
    
    // ПАНЕЛИ
    public static final ResourceLocation DATE_SCROLL = tex("date_scroll.png");
    public static final ResourceLocation CATEGORY_BOX = tex("category_box.png");
    public static final ResourceLocation ITEM_BOX = tex("item_box.png");
    public static final ResourceLocation DIVIDER = tex("divider.png");
    
    // ИКОНКИ
    public static final ResourceLocation COIN = tex("icon_coin.png");
    public static final ResourceLocation STAR = tex("icon_star.png");
    public static final ResourceLocation CLOUD = tex("icon_cloud.png");
    public static final ResourceLocation SMOKE = tex("icon_smoke.png");
    public static final ResourceLocation MOON = tex("icon_moon.png");
    public static final ResourceLocation MOON_EYES = tex("icon_moon_eyes.png");
    
    // КАТЕГОРИИ
    public static final ResourceLocation CAT_FARMING = tex("cat_farming.png");
    public static final ResourceLocation CAT_FORAGING = tex("cat_foraging.png");
    public static final ResourceLocation CAT_FISHING = tex("cat_fishing.png");
    public static final ResourceLocation CAT_MINING = tex("cat_mining.png");
    public static final ResourceLocation CAT_OTHER = tex("cat_other.png");
    public static final ResourceLocation CAT_TOTAL = tex("cat_total.png");
    
    // ЦИФРЫ
    public static final ResourceLocation[] DIGITS = new ResourceLocation[] {
        tex("digit_0.png"), tex("digit_1.png"), tex("digit_2.png"),
        tex("digit_3.png"), tex("digit_4.png"), tex("digit_5.png"),
        tex("digit_6.png"), tex("digit_7.png"), tex("digit_8.png"),
        tex("digit_9.png")
    };
    public static final ResourceLocation DIGIT_G = tex("digit_g.png");
    
    // АНИМАЦИИ
    public static final ResourceLocation BIRD_1 = tex("anim_bird_1.png");
    public static final ResourceLocation BIRD_2 = tex("anim_bird_2.png");
    public static final ResourceLocation OWL = tex("anim_owl.png");
    public static final ResourceLocation SANTA = tex("anim_santa.png");
    
    private static ResourceLocation tex(String filename) {
        return new ResourceLocation(StardewMod.MODID, PATH + filename);
    }
    
    public static ResourceLocation getCategoryIcon(int index) {
        return switch (index) {
            case 0 -> CAT_FARMING;
            case 1 -> CAT_FORAGING;
            case 2 -> CAT_FISHING;
            case 3 -> CAT_MINING;
            case 4 -> CAT_OTHER;
            case 5 -> CAT_TOTAL;
            default -> CAT_OTHER;
        };
    }
    
    public static ResourceLocation getDigit(int digit) {
        if (digit < 0 || digit > 9) return DIGITS[0];
        return DIGITS[digit];
    }
}

package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.StardewMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final RegistryObject<SoundEvent> HOE_TILL = register("hoe_till");
    public static final RegistryObject<SoundEvent> WATERING_CAN_USE = register("watering_can_use");
    public static final RegistryObject<SoundEvent> CHEST_OPEN = register("open_chest");
    public static final RegistryObject<SoundEvent> CHEST_CLOSE = register("close_chest");
    public static final RegistryObject<SoundEvent> TOOL_CHARGE = register("tool_charge");
    public static final RegistryObject<SoundEvent> MACHINE_INSERT_1 = register("machine_insert_1");
    public static final RegistryObject<SoundEvent> MACHINE_INSERT_2 = register("machine_insert_2");
    public static final RegistryObject<SoundEvent> MACHINE_COLLECT = register("machine_collect");

    // --- Character Creation Screen (см. CharacterCreationScreen) - разложены
    // по оригинальным именам звуков StardewValley.Menus.CharacterCustomization
    // (см. чат/reference-sources), "cc_"-префикс - чтобы не конфликтовать по
    // имени с будущими игровыми звуками (топор/молоток и т.п. уже
    // используются как звук для совсем других вещей в оригинале).
    public static final RegistryObject<SoundEvent> CC_SKELETON_STEP = register("cc_skeleton_step"); // Skin arrows
    public static final RegistryObject<SoundEvent> CC_GRASSY_STEP = register("cc_grassy_step");     // Hair arrows
    public static final RegistryObject<SoundEvent> CC_COIN = register("cc_coin");                   // Shirt/Pants/Pet arrows, gender, farm card, OK
    public static final RegistryObject<SoundEvent> CC_PURCHASE = register("cc_purchase");           // Accessory arrows
    public static final RegistryObject<SoundEvent> CC_DRUMKIT_1 = register("cc_drumkit1");
    public static final RegistryObject<SoundEvent> CC_DRUMKIT_2 = register("cc_drumkit2");
    public static final RegistryObject<SoundEvent> CC_DRUMKIT_3 = register("cc_drumkit3");
    public static final RegistryObject<SoundEvent> CC_DRUMKIT_4 = register("cc_drumkit4");
    public static final RegistryObject<SoundEvent> CC_DRUMKIT_5 = register("cc_drumkit5");
    public static final RegistryObject<SoundEvent> CC_DRUMKIT_6 = register("cc_drumkit6");           // Skip Intro toggle + dice's first click
    public static final RegistryObject<SoundEvent> CC_AXCHOP = register("cc_axchop");
    public static final RegistryObject<SoundEvent> CC_HOE_HIT = register("cc_hoe_hit");
    public static final RegistryObject<SoundEvent> CC_FISH_SLAP = register("cc_fish_slap");
    public static final RegistryObject<SoundEvent> CC_JUNIMO_MEEP = register("cc_junimo_meep");
    public static final RegistryObject<SoundEvent> CC_AXE = register("cc_axe");
    public static final RegistryObject<SoundEvent> CC_HAMMER = register("cc_hammer");
    public static final RegistryObject<SoundEvent> CC_HIT_ENEMY = register("cc_hit_enemy"); // stand-in for original's "dirtyHit" - not in our ripped sound pack

    /**
     * Все звуки этого блока сверены по декомпилированному исходнику
     * StardewValley.Menus.TitleMenu/LoadGameMenu (см. чат: "проверь все звуки
     * по исходникам stardew") - НЕ по памяти/на глаз, как было раньше (тогда
     * почти всё было перепутано - hover везде игрался как "smallSelect",
     * клики как "coin").
     *
     * Ховер кнопок ГЛАВНОГО МЕНЮ (New/Load/Co-op/Exit/Back/About/язык) -
     * {@code TitleMenu.performHoverAction}: {@code Game1.playSound("Cowboy_Footstep")}.
     */
    public static final RegistryObject<SoundEvent> MENU_HOVER = register("menu_hover");

    /** Ховер карточки МИРА в Load - ДРУГОЙ звук, не тот же, что у кнопок ({@code LoadGameMenu.performHoverAction}: {@code Game1.playSound("Cowboy_gunshot")}). */
    public static final RegistryObject<SoundEvent> MENU_HOVER_WORLD = register("menu_hover_world");

    /** Клик New/Co-op/Load, и клик по валидной карточке мира ({@code TitleMenu.performButtonAction} / {@code LoadGameMenu.receiveLeftClick}: {@code Game1.playSound("select")}). */
    public static final RegistryObject<SoundEvent> MENU_SELECT = register("menu_select");

    /** Клик Exit и "Назад" (закрытие сабменю) - {@code TitleMenu}: {@code Game1.playSound("bigDeSelect")}. */
    public static final RegistryObject<SoundEvent> MENU_DESELECT = register("menu_deselect");

    /** Клик "?" (открытие AboutMenu) - {@code TitleMenu.receiveLeftClick}: {@code Game1.playSound("newArtifact")}. */
    public static final RegistryObject<SoundEvent> MENU_ABOUT = register("menu_about");

    /** Отмена удаления мира - {@code LoadGameMenu.receiveLeftClick}: {@code Game1.playSound("smallSelect")}. */
    public static final RegistryObject<SoundEvent> MENU_CANCEL = register("menu_cancel");

    /** Подтверждение удаления мира - {@code LoadGameMenu.deleteFile}-путь: {@code Game1.playSound("trashcan")}. */
    public static final RegistryObject<SoundEvent> MENU_TRASHCAN = register("menu_trashcan");

    /** Звук скролла списка миров - {@code LoadGameMenu.receiveScrollWheelAction}: {@code Game1.playSound("shwip")} (см. чат: "звук шшш при скроле как в стардью" - этот уже был верным). */
    public static final RegistryObject<SoundEvent> MENU_SCROLL = register("menu_scroll");

    private static RegistryObject<SoundEvent> register(String name) {
        return StardewRegistry.SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StardewMod.MODID, name)));
    }

    public static void load() {}
}


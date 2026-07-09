package dev.flomik.stardew.client.screen.character;

import com.mojang.logging.LogUtils;
import dev.flomik.stardew.client.CustomCursor;
import dev.flomik.stardew.client.character.skin.RuntimeSkinManager;
import dev.flomik.stardew.client.render.StardewFrameRenderer;
import dev.flomik.stardew.client.screen.title.StardewTitleScreen;
import dev.flomik.stardew.common.module.character.AnimalPreference;
import dev.flomik.stardew.common.module.character.CharacterModelType;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.common.module.character.FarmType;
import dev.flomik.stardew.common.module.character.cosmetic.CosmeticDefinition;
import dev.flomik.stardew.common.module.character.cosmetic.CosmeticRegistry;
import dev.flomik.stardew.common.module.character.cosmetic.SkinToneDefinition;
import dev.flomik.stardew.common.module.character.event.FirstJoinController;
import dev.flomik.stardew.common.module.character.network.C2SFinishCharacterCreation;
import dev.flomik.stardew.common.module.time.TimeFreezeManager;
import dev.flomik.stardew.common.module.time.network.C2STimeFreezePacket;
import dev.flomik.stardew.common.registry.ModSounds;
import dev.flomik.stardew.core.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.joml.Quaternionf;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Character Creation Screen (ТЗ §6 и далее), расстановка/масштаб — 1:1 с
 * референсом (см. {@link CharacterCreationLayout} — virtual-canvas 1920×1080,
 * масштабируется и центрируется под фактический экран).
 *
 * v1-упрощения (см. чат ТЗ, без изменений от предыдущих итераций):
 * <ul>
 *   <li>Фон/панели/иконки — программистская заливка прямоугольниками, не
 *       пиксель-арт текстуры (их пока не существует);</li>
 *   <li>Farm Type карточки — цветные плейсхолдеры без иконок;</li>
 *   <li>Кнопка "BACK" с референса сознательно не реализована — обязательный
 *       первый экран нельзя обойти (ТЗ §2/§4), рабочая кнопка "выйти без
 *       создания персонажа" прямо противоречила бы этому;</li>
 *   <li>"Wrench" — заглушка без назначения, как и в референсе/ТЗ §6 (там оно
 *       тоже не описано).</li>
 * </ul>
 */
public class CharacterCreationScreen extends Screen implements dev.flomik.stardew.client.debug.DraggableTextProvider {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int SKY_TOP = 0xFF003266;
    private static final int SKY_MIDDLE = 0xFF0475E1;
    private static final int SKY_BOTTOM = 0xFF64E8E2;
    /** Отступ рамки выбора карточки фермы от её краёв (в реальных пикселях) - крупнее, чем у квадратных кнопок пола. */
    private static final int FARM_CARD_SELECTION_PADDING = 4;
    /** Полосы рамки этого экрана - по 5px вместо стандартного 1px у StardewFrameRenderer (тултипы остаются 1px). */
    // "уменьши с 5 до х" - конкретное число не было указано, взял 2 как
    // разумную середину (было 1 изначально, 5 оказалось слишком толсто).
    private static final int FRAME_THICKNESS = 3;

    // Единый цвет для ВСЕГО текста в этом UI (кроме сообщений об ошибке) -
    // раньше были разные (0xFF2A2A2A для лейблов, 0xFFAA2222 для полей).
    private static final int TEXT_COLOR = 0xFF221122;
    private static final int ERROR_COLOR = 0xFFFF0000;
    /** Тень ВСЕГО текста этого экрана, даже красного (см. чат) - у ванильного drawString тень фиксированная (автозатемнение), поэтому рисуем её вручную (dropShadow=false + отдельный вызов со сдвигом +1,+1), см. drawWithShadow. */
    private static final int SHADOW_COLOR = 0xFFDD9454;

    // --- Текстуры gui/character_creation (ТЗ: все кнопки — один видимый
    // размер вне зависимости от нативного разрешения PNG, см. IconButton).
    // Разложены по подпапкам по смыслу (buttons/gender/pets/farm_types/preview)
    // - раньше всё лежало плоским списком в gui/farm, что не отражало
    // содержимое (там нет ничего собственно про farm-геймплей, всё про
    // character creation screen). ---
    private static final String TEX_DIR = "textures/gui/character_creation/";
    private static final ResourceLocation TEX_ARROW_LEFT = tex("buttons/arrow_left.png");
    private static final ResourceLocation TEX_ARROW_RIGHT = tex("buttons/arrow_right.png");
    private static final ResourceLocation TEX_MALE = tex("gender/male.png");
    private static final ResourceLocation TEX_FEMALE = tex("gender/female.png");
    private static final ResourceLocation TEX_GENDER_SELECTED = tex("gender/gender_selected.png");
    private static final ResourceLocation TEX_DICE = tex("buttons/dice.png");
    private static final ResourceLocation TEX_OK = tex("buttons/ok.png");
    private static final ResourceLocation TEX_PETS = tex("pets/pets.png");
    // Отдельный файл ТОЛЬКО с дневным кадром (128×192, обрезан из
    // preview/character_backgrounds.png один раз при разработке) - предыдущие
    // 2 попытки использовать общий спрайт-лист (день+ночь впритык) через U/V
    // региона почему-то не показывали текстуру вообще, поэтому вместо
    // region-математики - тот же "растяни ВЕСЬ файл под bounding box" приём,
    // что и в IconButton (см. его javadoc) - тут вообще нечему сломаться.
    private static final ResourceLocation TEX_CHARACTER_BG_DAY = tex("preview/character_background_day.png");

    // pets/pets.png - спрайт-лист 96×16 (6 кадров по 16×16); CAT/DOG рисуются
    // БЕЗ растяжения (dest size == native frame size), поэтому тут нужен
    // настоящий размер листа для корректного выбора кадра по U-координате.
    private static final int PETS_TEX_TOTAL_WIDTH = 96;
    private static final int PETS_TEX_FRAME_SIZE = 16;
    private static final int PETS_TEX_FRAME_COUNT = PETS_TEX_TOTAL_WIDTH / PETS_TEX_FRAME_SIZE;

    // farm_types/farms.png - 127×20, 7 кадров РАЗНОЙ ширины (18/19/18/18/18/18/18)
    // при одной высоте 20 - в отличие от pets.png тут нельзя использовать
    // общий "fakeTexWidth = destSize*count" трюк (кадры не одинаковой ширины),
    // формула для каждого кадра выведена индивидуально в farmCardUV().
    private static final ResourceLocation TEX_FARMS = tex("farm_types/farms.png");
    private static final int[] FARMS_TEX_FRAME_WIDTHS = {18, 19, 18, 18, 18, 18, 18};
    private static final int FARMS_TEX_HEIGHT = 20;
    private static final int FARMS_TEX_TOTAL_WIDTH = 127;

    private static ResourceLocation tex(String relativePath) {
        return new ResourceLocation(dev.flomik.stardew.StardewMod.MODID, TEX_DIR + relativePath);
    }

    /** Немедленно меняем draft на каждый клик, но пересобираем скин не чаще этого интервала (ТЗ §30). */
    private static final long PREVIEW_REBUILD_DEBOUNCE_MS = 50;

    private final CharacterProfile draft = new CharacterProfile();
    private final Random random = new Random();
    /** Первый клик по кубику всегда {@code drumkit6}, дальше - случайный из пула (см. randomizeAppearance()), как в оригинале. */
    private int diceClickCount = 0;
    private static final List<java.util.function.Supplier<SoundEvent>> DICE_SOUND_POOL = List.of(
            ModSounds.CC_DRUMKIT_1::get, ModSounds.CC_HIT_ENEMY::get, ModSounds.CC_AXCHOP::get, ModSounds.CC_HOE_HIT::get,
            ModSounds.CC_FISH_SLAP::get, ModSounds.CC_DRUMKIT_6::get, ModSounds.CC_DRUMKIT_5::get, ModSounds.CC_DRUMKIT_6::get,
            ModSounds.CC_JUNIMO_MEEP::get, ModSounds.CC_COIN::get, ModSounds.CC_AXE::get, ModSounds.CC_HAMMER::get,
            ModSounds.CC_DRUMKIT_2::get, ModSounds.CC_DRUMKIT_4::get, ModSounds.CC_DRUMKIT_3::get
    );

    private CharacterCreationLayout layout;

    private int skinIndex, hairIndex, shirtIndex, pantsIndex, accessoryIndex;
    private List<String> skinIds, hairIds, shirtIds, pantsIds, accessoryIds;

    private int eyeHue, eyeSat, eyeVal;
    private int hairHue, hairSat, hairVal;
    private int pantsHue, pantsSat, pantsVal;

    private EditBox nameBox;
    private EditBox farmNameBox;
    private EditBox favoriteThingBox;
    private Button okButton;
    private Checkbox skipIntroBox;

    private boolean previewDirty = true;
    private long lastRebuildTime;
    private boolean processing = false;
    private String errorMessage = null;

    private final List<Runnable> textRenderers = new ArrayList<>();
    /** Точки для F8-дебаг-инструмента (см. DraggableTextProvider) - одна на каждую addCenteredLabel. */
    private final List<dev.flomik.stardew.client.debug.DraggablePoint> debugDraggablePoints = new ArrayList<>();

    /**
     * Верх/низ ОБЕИХ панелей (главной и farm) как отдельные F8-точки - иначе
     * границы рамки вообще нельзя подвигать (сама рамка не widget и не
     * подпись, просто прямой blit по статичным PANEL_X/Y/W/H) - см. чат:
     * "верхний и нижний рамка не работает". X/ширина панели остаются
     * фиксированными (только verticaль двигается) - двигать точку нужно
     * ПО ЦЕНТРУ верхнего/нижнего края, x у неё не используется для рендера.
     */
    private dev.flomik.stardew.client.debug.DraggablePoint panelTopPoint, panelBottomPoint;
    private dev.flomik.stardew.client.debug.DraggablePoint farmPanelTopPoint, farmPanelBottomPoint;

    @Override
    public List<dev.flomik.stardew.client.debug.DraggablePoint> getDebugDraggablePoints() {
        return debugDraggablePoints;
    }

    /** Экран всегда открывается с {@code guiScale=2} (см. {@link dev.flomik.stardew.client.character.ClientCharacterCreationOpener#open}) - тут только запоминаем, что было, чтобы вернуть на выходе. */
    private final int previousGuiScale;

    /**
     * Заморозка времени - по образцу {@code ChestScreen} (см. его javadoc),
     * но с явным флагом: {@code init()} вызывается заново при КАЖДОМ resize
     * окна (стандартное поведение {@code Screen}), а {@code TimeFreezeManager.freeze()}
     * на сервере СЧИТАЕТ вложенные вызовы - без флага несколько resize'ов
     * успели бы "заморозить" счётчик больше раз, чем один {@code removed()}
     * способен разморозить, и время осталось бы замороженным навсегда после
     * закрытия экрана.
     */
    private boolean timeFreezeActive = false;

    public CharacterCreationScreen(int previousGuiScale) {
        super(Component.translatable("gui.stardew.character.title"));
        this.previousGuiScale = previousGuiScale;
    }

    @Override
    public void removed() {
        super.removed();
        // Возвращаем игроку его собственный gui scale при закрытии экрана -
        // фиксированный scale=2 нужен только ПОКА этот экран открыт (ТЗ:
        // "игрок не может менять gui scale в этом меню").
        if (this.minecraft != null && this.minecraft.options.guiScale().get() != previousGuiScale) {
            this.minecraft.options.guiScale().set(previousGuiScale);
            this.minecraft.resizeDisplay();
        }
        CustomCursor.restore();

        if (timeFreezeActive) {
            PacketHandler.sendToServer(new C2STimeFreezePacket(false));
            TimeFreezeManager.unfreezeClient();
            timeFreezeActive = false;
        }
    }

    @Override
    protected void init() {
        // Время должно стоять на месте, пока игрок создаёт персонажа - только
        // в singleplayer (см. javadoc timeFreezeActive) и только ОДИН раз за
        // время жизни экрана, а не при каждом resize.
        if (!timeFreezeActive && this.minecraft != null && this.minecraft.hasSingleplayerServer()) {
            PacketHandler.sendToServer(new C2STimeFreezePacket(true));
            TimeFreezeManager.freezeClient();
            timeFreezeActive = true;
        }
        CustomCursor.apply();
        textRenderers.clear();
        // Раньше НЕ чистился - на каждый resize() (а Screen делает это часто)
        // список только рос, копя дубликаты хитбоксов поверх старых, уже не
        // существующих подписей.
        debugDraggablePoints.clear();
        layout = new CharacterCreationLayout(this.width, this.height);

        int[] panelBounds = layout.real(CharacterCreationLayout.PANEL_X, CharacterCreationLayout.PANEL_Y,
                CharacterCreationLayout.PANEL_W, CharacterCreationLayout.PANEL_H);
        panelTopPoint = new dev.flomik.stardew.client.debug.DraggablePoint("PanelTop",
                panelBounds[0] + panelBounds[2] / 2, panelBounds[1]);
        panelBottomPoint = new dev.flomik.stardew.client.debug.DraggablePoint("PanelBottom",
                panelBounds[0] + panelBounds[2] / 2, panelBounds[1] + panelBounds[3]);
        debugDraggablePoints.add(panelTopPoint);
        debugDraggablePoints.add(panelBottomPoint);

        int[] farmPanelBounds = layout.real(CharacterCreationLayout.FARM_PANEL_X, CharacterCreationLayout.FARM_PANEL_Y,
                CharacterCreationLayout.FARM_PANEL_W, CharacterCreationLayout.FARM_PANEL_H);
        farmPanelTopPoint = new dev.flomik.stardew.client.debug.DraggablePoint("FarmPanelTop",
                farmPanelBounds[0] + farmPanelBounds[2] / 2, farmPanelBounds[1]);
        farmPanelBottomPoint = new dev.flomik.stardew.client.debug.DraggablePoint("FarmPanelBottom",
                farmPanelBounds[0] + farmPanelBounds[2] / 2, farmPanelBounds[1] + farmPanelBounds[3]);
        debugDraggablePoints.add(farmPanelTopPoint);
        debugDraggablePoints.add(farmPanelBottomPoint);

        skinIds = CosmeticRegistry.skinIds();
        hairIds = CosmeticRegistry.hairIds();
        // "none" - валидный id на уровне реестра (голый торс/низ ТОЛЬКО в
        // игре, когда снят реальный предмет одежды - см. чат: "в редакторе
        // голым быть нельзя. только в игре"), но в самом редакторе стрелками
        // до него долистать нельзя - тут всегда настоящая косметика.
        shirtIds = CosmeticRegistry.shirtIds();
        shirtIds.remove("none");
        pantsIds = CosmeticRegistry.pantsIds();
        pantsIds.remove("none");
        accessoryIds = CosmeticRegistry.accessoryIds();

        skinIndex = indexOfOrZero(skinIds, draft.getSkinId());
        hairIndex = indexOfOrZero(hairIds, draft.getHairId());
        shirtIndex = indexOfOrZero(shirtIds, draft.getShirtId());
        pantsIndex = indexOfOrZero(pantsIds, draft.getPantsId());
        accessoryIndex = indexOfOrZero(accessoryIds, draft.getAccessoryId());

        int[] eyeHsv = rgbToHsvPercent(draft.getEyeColor());
        eyeHue = eyeHsv[0]; eyeSat = eyeHsv[1]; eyeVal = eyeHsv[2];
        int[] hairHsv = rgbToHsvPercent(draft.getHairColor());
        hairHue = hairHsv[0]; hairSat = hairHsv[1]; hairVal = hairHsv[2];
        int[] pantsHsv = rgbToHsvPercent(draft.getPantsColor());
        pantsHue = pantsHsv[0]; pantsSat = pantsHsv[1]; pantsVal = pantsHsv[2];

        buildIdentityFields();
        buildGenderRow();
        buildAnimalPreferenceRow();
        buildCosmeticArrowRows();
        buildColorSliders();
        buildFarmTypeCards();
        buildBottomControls();
        updateOkButtonState();

        applyDraftToSkin(true);
    }

    /**
     * OK не должен работать (и должен выглядеть серым, см. {@link IconButton}),
     * пока не заполнены ВСЕ три поля: имя, название фермы, любимая вещь.
     * Вызывается при каждом изменении любого из трёх полей, а не только на
     * попытке нажать OK - кнопка должна реагировать сразу, а не только
     * ругаться постфактум.
     */
    private void updateOkButtonState() {
        if (okButton == null) return;
        boolean filled = isFilled(nameBox) && isFilled(farmNameBox) && isFilled(favoriteThingBox);
        okButton.active = filled;
    }

    private static boolean isFilled(EditBox box) {
        return box != null && !box.getValue().isBlank();
    }

    private static int indexOfOrZero(List<String> ids, String id) {
        int idx = ids.indexOf(id);
        return Math.max(idx, 0);
    }

    private Button addButton(int vx, int vy, int vw, int vh, Component label, Button.OnPress onPress) {
        int[] r = layout.real(vx, vy, vw, vh);
        Button button = Button.builder(label, onPress).bounds(r[0], r[1], r[2], r[3]).build();
        addRenderableWidget(button);
        return button;
    }

    /** Как {@link #addButton}, но без ванильной текстуры фона - см. {@link ImagelessButton}. */
    private Button addImagelessButton(int vx, int vy, int vw, int vh, Button.OnPress onPress) {
        int[] r = layout.real(vx, vy, vw, vh);
        Button button = new ImagelessButton(r[0], r[1], r[2], r[3], onPress);
        addRenderableWidget(button);
        return button;
    }

    /**
     * Кнопка-иконка растянутая на {@code vw}x{@code vh} (layout-размер),
     * независимо от нативного разрешения {@code texture} (см. {@link IconButton}).
     */
    private IconButton addIconButton(int vx, int vy, int vw, int vh, ResourceLocation texture, Button.OnPress onPress) {
        return addIconButton(vx, vy, vw, vh, texture, onPress, IconButton.HOVER_GROWTH_SMALL);
    }

    private IconButton addIconButton(int vx, int vy, int vw, int vh, ResourceLocation texture, Button.OnPress onPress, float hoverGrowth) {
        int[] r = layout.real(vx, vy, vw, vh);
        IconButton button = new IconButton(r[0], r[1], r[2], r[3], texture, onPress, hoverGrowth);
        addRenderableWidget(button);
        return button;
    }

    /**
     * Все текстовые подписи экрана — жирным, без тени, по центру заданной X
     * (не по левому краю). {@code textSupplier} вызывается заново каждый
     * кадр — для статичных подписей (лейблы) достаточно {@code () -> Component.translatable(...)},
     * для динамических (текущий индекс, выбранное животное и т.п.) он же
     * подхватывает актуальное значение.
     */
    private void addCenteredLabel(int centerX, int y, Supplier<Component> textSupplier, int color) {
        // DraggablePoint - изменяемая позиция (не final int из замыкания), чтобы
        // F8-дебаг-инструмент мог её подвинуть мышью (см. DebugLayoutDragTool).
        String debugLabel = textSupplier.get().getString();
        dev.flomik.stardew.client.debug.DraggablePoint point =
                new dev.flomik.stardew.client.debug.DraggablePoint(debugLabel, centerX, y);
        debugDraggablePoints.add(point);
        textRenderers.add(() -> {
            Component bold = textSupplier.get().copy().withStyle(ChatFormatting.BOLD);
            int width = this.font.width(bold);
            drawWithShadow(bold, point.x - width / 2, point.y, color);
        });
    }

    /** Кастомный цвет тени (см. {@link #SHADOW_COLOR}) - у ванильного drawString тень фиксированное автозатемнение, поэтому рисуем тень и текст вручную, оба с dropShadow=false. */
    private void drawWithShadow(Component text, int x, int y, int color) {
        GuiGraphics graphics = currentGuiGraphics();
        graphics.drawString(this.font, text, x + 1, y + 1, SHADOW_COLOR, false);
        graphics.drawString(this.font, text, x, y, color, false);
    }

    // --- Identity (ТЗ §10) ---

    private void buildIdentityFields() {
        nameBox = addEditBox(CharacterCreationLayout.FIELD_INPUT_X, CharacterCreationLayout.NAME_Y, "gui.stardew.character.name");
        nameBox.setValue(draft.getName());
        nameBox.setMaxLength(24);
        setPlaceholder(nameBox, "gui.stardew.character.name");
        nameBox.setResponder(value -> {
            draft.setName(value);
            clearError();
            setPlaceholder(nameBox, "gui.stardew.character.name");
            updateOkButtonState();
        });
        // +15 (было +8) - подогнано мышью через F8-дебаг-инструмент (см. чат:
        // "меняем расположение текста... в имени" + stardew_debug_layout_CharacterCreationScreen.json):
        // однострочная подпись короче двухстрочной (см. addTwoLineFieldLabel),
        // ей нужен больший отступ вниз, чтобы центр текста совпал с центром поля.
        addFieldLabel(CharacterCreationLayout.NAME_Y + 15, "gui.stardew.character.name", nameBox);

        farmNameBox = addEditBox(CharacterCreationLayout.FIELD_INPUT_X, CharacterCreationLayout.FARM_NAME_Y, "gui.stardew.character.farm_name");
        farmNameBox.setValue(draft.getFarmName());
        farmNameBox.setMaxLength(32);
        setPlaceholder(farmNameBox, "gui.stardew.character.farm_name");
        farmNameBox.setResponder(value -> {
            draft.setFarmName(value);
            setPlaceholder(farmNameBox, "gui.stardew.character.farm_name");
            updateOkButtonState();
        });
        addTwoLineFieldLabel(CharacterCreationLayout.FARM_NAME_Y,
                "gui.stardew.character.farm_name_line1", "gui.stardew.character.farm_name_line2", farmNameBox);
        // Инлайн-подсказка "... Ферма" сразу после поля (как в vanilla:
        // текст поля + статичное слово рядом, не часть самого значения).
        addInlineSuffix(CharacterCreationLayout.FIELD_INPUT_X + CharacterCreationLayout.FIELD_INPUT_W,
                CharacterCreationLayout.FARM_NAME_Y, "gui.stardew.character.farm_name_suffix");

        favoriteThingBox = addEditBox(CharacterCreationLayout.FIELD_INPUT_X, CharacterCreationLayout.FAVORITE_THING_Y, "gui.stardew.character.favorite_thing");
        favoriteThingBox.setValue(draft.getFavoriteThing());
        favoriteThingBox.setMaxLength(48);
        setPlaceholder(favoriteThingBox, "gui.stardew.character.favorite_thing");
        favoriteThingBox.setResponder(value -> {
            draft.setFavoriteThing(value);
            setPlaceholder(favoriteThingBox, "gui.stardew.character.favorite_thing");
            updateOkButtonState();
        });
        addTwoLineFieldLabel(CharacterCreationLayout.FAVORITE_THING_Y,
                "gui.stardew.character.favorite_thing_line1", "gui.stardew.character.favorite_thing_line2", favoriteThingBox);
    }

    /** Лейбл красный, пока соответствующее поле пустое - иначе обычный текстовый цвет. */
    private static int fieldLabelColor(EditBox box) {
        return box.getValue().isBlank() ? ERROR_COLOR : TEXT_COLOR;
    }

    /** Левый край подписи (ТЗ: центрирование к левому краю, не по центру зоны). */
    private int fieldLabelLeftX() {
        return layout.realX(CharacterCreationLayout.FIELD_LABEL_LEFT_X);
    }

    private void addFieldLabel(int vy, String key, EditBox box) {
        int x = fieldLabelLeftX();
        int y = layout.realY(vy);
        addLeftAlignedLabel(x, y, () -> Component.translatable(key), () -> fieldLabelColor(box));
    }

    /**
     * "Farm Name"/"Favorite Thing" переносятся на 2 строки, как на референсе —
     * это не эстетика, а необходимость: при низком scale (см. Layout javadoc)
     * однострочная подпись такой длины не помещается в зазор до поля ввода.
     */
    private void addTwoLineFieldLabel(int vy, String line1Key, String line2Key, EditBox box) {
        int x = fieldLabelLeftX();
        // -7 (было +8) - подогнано мышью через F8 (см. чат/JSON): блок из ДВУХ
        // строк выше одной строки (см. addFieldLabel), ему нужен МЕНЬШИЙ
        // отступ вниз (даже отрицательный), чтобы центр 2-строчного блока
        // совпал с центром поля.
        int y1 = layout.realY(vy - 7);
        int y2 = layout.realY(vy - 7 + CharacterCreationLayout.FIELD_LABEL_LINE_GAP);
        addLeftAlignedLabel(x, y1, () -> Component.translatable(line1Key), () -> fieldLabelColor(box));
        addLeftAlignedLabel(x, y2, () -> Component.translatable(line2Key), () -> fieldLabelColor(box));
    }

    /** Как {@link #addCenteredLabel}, но текст растёт от {@code x} вправо (не центрируется). */
    private void addLeftAlignedLabel(int x, int y, Supplier<Component> textSupplier, java.util.function.IntSupplier colorSupplier) {
        dev.flomik.stardew.client.debug.DraggablePoint point =
                new dev.flomik.stardew.client.debug.DraggablePoint(textSupplier.get().getString(), x, y);
        debugDraggablePoints.add(point);
        textRenderers.add(() -> {
            Component bold = textSupplier.get().copy().withStyle(ChatFormatting.BOLD);
            drawWithShadow(bold, point.x, point.y, colorSupplier.getAsInt());
        });
    }

    /**
     * Инлайн-текст ЛЕВЫМ краем в точке {@code vx} (растёт вправо), в отличие
     * от {@link #addCenteredLabel} - для "[поле ввода] Ферма"-паттерна, где
     * слово должно начинаться сразу после поля, а не центрироваться (как в
     * vanilla {@code CharacterCustomization.cs}, см. бриф про Farm Name).
     */
    private void addInlineSuffix(int vx, int vy, String key) {
        int x = layout.realX(vx) + layout.realLen(23);
        int y = layout.realY(vy + 8);
        dev.flomik.stardew.client.debug.DraggablePoint point =
                new dev.flomik.stardew.client.debug.DraggablePoint(Component.translatable(key).getString(), x, y);
        debugDraggablePoints.add(point);
        textRenderers.add(() -> {
            Component bold = Component.translatable(key).copy().withStyle(ChatFormatting.BOLD);
            // Обычный (не красный, как у field-подписей) цвет - суффикс это
            // просто UI-подсказка, а не поле/ошибка.
            drawWithShadow(bold, point.x, point.y, TEXT_COLOR);
        });
    }

    private EditBox addEditBox(int vx, int vy, String narrationKey) {
        int[] r = layout.real(vx, vy, CharacterCreationLayout.FIELD_INPUT_W, CharacterCreationLayout.FIELD_INPUT_H);
        EditBox box = new StardewEditBox(this.font, r[0], r[1], r[2], r[3], Component.translatable(narrationKey));
        addRenderableWidget(box);
        return box;
    }

    /**
     * {@link EditBox#setSuggestion} рисует ghost-текст ПОСЛЕ текущего значения
     * (это autocomplete-continuation, не placeholder) — если не снимать его,
     * как только поле непустое, подсказка слипается с введённым текстом
     * ("BogdanName"). Показываем только пока поле пустое.
     */
    private void setPlaceholder(EditBox box, String key) {
        box.setSuggestion(box.getValue().isEmpty() ? Component.translatable(key).getString() : null);
    }

    /**
     * Звуки этого экрана 1:1 портированы из оригинального
     * {@code StardewValley.Menus.CharacterCustomization} (см. чат) - какое
     * действие какой звук проигрывает, см. по местам вызова ниже
     * (гендер/стрелки/фермы/OK - все "coin"/"skeletonStep"/... из
     * оригинала). Слайдеры цвета в оригинале беззвучные - им звук не добавлен.
     */
    private void playSound(SoundEvent event) {
        if (this.minecraft == null) return;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F));
    }

    // --- Пол персонажа (референс: male/female иконки) ---

    /**
     * Male -> classic (Steve, широкие руки), Female -> slim (Alex, узкие) —
     * реальная смена 3D-модели скина, не только текстуры (см.
     * {@link dev.flomik.stardew.client.character.skin.RuntimeSkinManager}
     * и {@code mixin.client.PlayerInfoSkinLockMixin}).
     */
    private void buildGenderRow() {
        addIconButton(CharacterCreationLayout.GENDER_MALE_X, CharacterCreationLayout.GENDER_Y,
                CharacterCreationLayout.GENDER_SIZE, CharacterCreationLayout.GENDER_SIZE,
                TEX_MALE, b -> { draft.setModelType(CharacterModelType.CLASSIC); markDirty(); playSound(ModSounds.CC_COIN.get()); }, IconButton.HOVER_GROWTH_LARGE);
        addIconButton(CharacterCreationLayout.GENDER_FEMALE_X, CharacterCreationLayout.GENDER_Y,
                CharacterCreationLayout.GENDER_SIZE, CharacterCreationLayout.GENDER_SIZE,
                TEX_FEMALE, b -> { draft.setModelType(CharacterModelType.SLIM); markDirty(); playSound(ModSounds.CC_COIN.get()); }, IconButton.HOVER_GROWTH_LARGE);

        int maleX = layout.realX(CharacterCreationLayout.GENDER_MALE_X);
        int femaleX = layout.realX(CharacterCreationLayout.GENDER_FEMALE_X);
        int y = layout.realY(CharacterCreationLayout.GENDER_Y);
        int size = layout.realLen(CharacterCreationLayout.GENDER_SIZE);
        textRenderers.add(() -> {
            int selectedX = draft.getModelType() == CharacterModelType.CLASSIC ? maleX : femaleX;
            // size,size как texWidth/texHeight (не настоящий 64×64) - тот же
            // трюк с UV=[0,1], что и в IconButton (см. его javadoc).
            currentGuiGraphics().blit(TEX_GENDER_SELECTED, selectedX, y, 0, 0, size, size, size, size);
        });
    }

    // --- Animal preference (ТЗ §11) ---

    private void buildAnimalPreferenceRow() {
        addIconButton(CharacterCreationLayout.ANIMAL_ARROW_LEFT_X, CharacterCreationLayout.ANIMAL_CONTROL_Y,
                CharacterCreationLayout.ANIMAL_ARROW_SIZE, CharacterCreationLayout.ANIMAL_ARROW_SIZE,
                TEX_ARROW_LEFT, b -> cycleAnimalPreference(-1));
        addIconButton(CharacterCreationLayout.ANIMAL_ARROW_RIGHT_X, CharacterCreationLayout.ANIMAL_CONTROL_Y,
                CharacterCreationLayout.ANIMAL_ARROW_SIZE, CharacterCreationLayout.ANIMAL_ARROW_SIZE,
                TEX_ARROW_RIGHT, b -> cycleAnimalPreference(1));

        // Центр между внутренними краями стрелок - подпись и иконка питомца центрируются на одной X.
        int centerX = layout.realX(CharacterCreationLayout.ANIMAL_ICON_X + CharacterCreationLayout.ANIMAL_ICON_SIZE / 2);
        int labelY = layout.realY(CharacterCreationLayout.ANIMAL_LABEL_Y);
        addCenteredLabel(centerX, labelY, () -> Component.translatable("gui.stardew.character.animal_preference"), TEXT_COLOR);

        // pets.png - кадр по индексу AnimalPreference (0=Cat, 1=Dog),
        // растянутый до размера кнопок пола (ТЗ: "аналогично кнопкам смены
        // пола"). Обычный трюк IconButton (dest size == texWidth/texHeight)
        // тут не годится напрямую - нужно выбрать ОДИН кадр из листа, а не
        // весь лист; поэтому texWidth = destSize*кадров, u = индекс*destSize -
        // тот же принцип UV-нормализации, только с учётом конкретного кадра.
        int iconSize = layout.realLen(CharacterCreationLayout.ANIMAL_ICON_SIZE);
        int iconX = layout.realX(CharacterCreationLayout.ANIMAL_ICON_X);
        int iconY = layout.realY(CharacterCreationLayout.ANIMAL_CONTROL_Y)
                - (iconSize - layout.realLen(CharacterCreationLayout.ANIMAL_ARROW_SIZE)) / 2;
        textRenderers.add(() -> {
            int frame = draft.getAnimalPreference().ordinal();
            int fakeTexWidth = iconSize * PETS_TEX_FRAME_COUNT;
            currentGuiGraphics().blit(TEX_PETS, iconX, iconY, (float) (frame * iconSize), 0f,
                    iconSize, iconSize, fakeTexWidth, iconSize);
        });
    }

    private void cycleAnimalPreference(int direction) {
        AnimalPreference[] values = AnimalPreference.values();
        int next = Math.floorMod(draft.getAnimalPreference().ordinal() + direction, values.length);
        draft.setAnimalPreference(values[next]);
        playSound(ModSounds.CC_COIN.get());
    }

    // --- Skin/Hair/Shirt/Pants/Accessory arrow rows (ТЗ §12-16) ---

    private void buildCosmeticArrowRows() {
        int y = CharacterCreationLayout.ARROW_ROW_START_Y;
        int rowHeight = CharacterCreationLayout.ARROW_ROW_HEIGHT;

        addCosmeticRow(y, "gui.stardew.character.skin", () -> skinIds, () -> skinIndex,
                idx -> { skinIndex = idx; draft.setSkinId(skinIds.get(idx)); markDirty(); playSound(ModSounds.CC_SKELETON_STEP.get()); });
        addCosmeticRow(y + rowHeight, "gui.stardew.character.hair", () -> hairIds, () -> hairIndex,
                idx -> { hairIndex = idx; draft.setHairId(hairIds.get(idx)); markDirty(); playSound(ModSounds.CC_GRASSY_STEP.get()); });
        addCosmeticRow(y + rowHeight * 2, "gui.stardew.character.shirt", () -> shirtIds, () -> shirtIndex,
                idx -> { shirtIndex = idx; draft.setShirtId(shirtIds.get(idx)); markDirty(); playSound(ModSounds.CC_COIN.get()); });
        addCosmeticRow(y + rowHeight * 3, "gui.stardew.character.pants", () -> pantsIds, () -> pantsIndex,
                idx -> { pantsIndex = idx; draft.setPantsId(pantsIds.get(idx)); markDirty(); playSound(ModSounds.CC_COIN.get()); });
        addCosmeticRow(y + rowHeight * 4, "gui.stardew.character.accessory", () -> accessoryIds, () -> accessoryIndex,
                idx -> { accessoryIndex = idx; draft.setAccessoryId(accessoryIds.get(idx)); markDirty(); playSound(ModSounds.CC_PURCHASE.get()); });
    }

    private void addCosmeticRow(int vy, String labelKey, Supplier<List<String>> ids, Supplier<Integer> currentIndex, Consumer<Integer> onSelect) {
        int arrowSize = CharacterCreationLayout.ARROW_SIZE;
        int arrowX = CharacterCreationLayout.ARROW_ROW_X;
        int labelX = CharacterCreationLayout.ARROW_LABEL_X;
        int rightArrowX = labelX + CharacterCreationLayout.ARROW_RIGHT_GAP;

        addIconButton(arrowX, vy + 4, arrowSize, arrowSize, TEX_ARROW_LEFT, b -> {
            List<String> list = ids.get();
            int next = Math.floorMod(currentIndex.get() - 1, list.size());
            onSelect.accept(next);
        });
        addIconButton(rightArrowX, vy + 4, arrowSize, arrowSize, TEX_ARROW_RIGHT, b -> {
            List<String> list = ids.get();
            int next = Math.floorMod(currentIndex.get() + 1, list.size());
            onSelect.accept(next);
        });

        // Фиксированная зона сразу после левой стрелки (НЕ midpoint между
        // стрелками - тот уезжает к центру панели вместе с шириной ряда и
        // накладывается на подпись цветовой группы, см. CharacterCreationLayout).
        int centerX = layout.realX(labelX + CharacterCreationLayout.ARROW_LABEL_ZONE_WIDTH / 2);
        int realLabelY = layout.realY(vy + CharacterCreationLayout.ARROW_LABEL_Y_OFFSET);
        int realNumberY = layout.realY(vy + CharacterCreationLayout.ARROW_NUMBER_LINE_GAP);
        addCenteredLabel(centerX, realLabelY, () -> Component.translatable(labelKey), TEXT_COLOR);
        addCenteredLabel(centerX, realNumberY, () -> Component.literal(String.valueOf(currentIndex.get() + 1)), TEXT_COLOR);
    }

    // --- Color sliders (ТЗ §17-19, §21) — стопкой из 3 (H/S/V), как на референсе ---

    private void buildColorSliders() {
        int y = CharacterCreationLayout.COLOR_GROUP_START_Y;
        int groupHeight = CharacterCreationLayout.COLOR_GROUP_HEIGHT;

        addColorSliderGroup(y, "gui.stardew.character.eye_color",
                eyeHue, eyeSat, eyeVal,
                h -> { eyeHue = h; recomputeEyeColor(); },
                s -> { eyeSat = s; recomputeEyeColor(); },
                v -> { eyeVal = v; recomputeEyeColor(); },
                () -> eyeHue, () -> eyeSat);

        addColorSliderGroup(y + groupHeight, "gui.stardew.character.hair_color",
                hairHue, hairSat, hairVal,
                h -> { hairHue = h; recomputeHairColor(); },
                s -> { hairSat = s; recomputeHairColor(); },
                v -> { hairVal = v; recomputeHairColor(); },
                () -> hairHue, () -> hairSat);

        addColorSliderGroup(y + groupHeight * 2, "gui.stardew.character.pants_color",
                pantsHue, pantsSat, pantsVal,
                h -> { pantsHue = h; recomputePantsColor(); },
                s -> { pantsSat = s; recomputePantsColor(); },
                v -> { pantsVal = v; recomputePantsColor(); },
                () -> pantsHue, () -> pantsSat);
    }

    private void addColorSliderGroup(int vy, String labelKey, int hue, int sat, int val,
                                      IntConsumer onHue, IntConsumer onSat, IntConsumer onVal,
                                      IntSupplier hueSupplier, IntSupplier satSupplier) {
        int sliderX = CharacterCreationLayout.COLOR_SLIDER_X;
        int sliderW = CharacterCreationLayout.COLOR_SLIDER_W;
        int sliderH = CharacterCreationLayout.COLOR_SLIDER_H;
        int rowGap = CharacterCreationLayout.COLOR_SLIDER_ROW_GAP;
        int rowY = vy + CharacterCreationLayout.COLOR_SLIDER_FIRST_ROW_OFFSET;

        // Живые градиенты (пересчитываются каждый кадр, см. PercentSlider.TrackGradient):
        // Hue - радуга от красного до красного; Saturation/Value читают ТЕКУЩИЙ
        // hue (и sat для Value) через supplier'ы - при смене Hue дорожки S/V
        // перекрашиваются сами, без пересоздания виджетов.
        PercentSlider.TrackGradient hueGradient = t -> hsvToArgb(t, 1f, 1f);
        PercentSlider.TrackGradient satGradient = t -> hsvToArgb(hueSupplier.getAsInt() / 100f, t, 1f);
        PercentSlider.TrackGradient valGradient = t -> hsvToArgb(hueSupplier.getAsInt() / 100f, satSupplier.getAsInt() / 100f, t);

        addSlider(sliderX, rowY, sliderW, sliderH, "H", hue, i -> { onHue.accept(i); markDirty(); }, hueGradient);
        addSlider(sliderX, rowY + rowGap, sliderW, sliderH, "S", sat, i -> { onSat.accept(i); markDirty(); }, satGradient);
        addSlider(sliderX, rowY + rowGap * 2, sliderW, sliderH, "V", val, i -> { onVal.accept(i); markDirty(); }, valGradient);

        // Один текст на группу из 3 слайдеров ("3 текста на 9 слайдеров") -
        // слева от них, по вертикали центрирован на СРЕДНЕМ (S) слайдере, а
        // не над всей группой.
        int centerX = layout.realX(CharacterCreationLayout.COLOR_GROUP_LABEL_CENTER_X);
        int middleSliderCenterY = layout.realY(rowY + rowGap) + layout.realLen(sliderH) / 2;
        int labelY = middleSliderCenterY - this.font.lineHeight / 2;
        addCenteredLabel(centerX, labelY, () -> Component.translatable(labelKey), TEXT_COLOR);
    }

    private static int hsvToArgb(float h, float s, float v) {
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(h, s, v) & 0xFFFFFF);
    }

    private void addSlider(int vx, int vy, int vw, int vh, String label, int initialPercent, IntConsumer onChange, PercentSlider.TrackGradient gradient) {
        int[] r = layout.real(vx, vy, vw, vh);
        PercentSlider slider = new PercentSlider(r[0], r[1], r[2], r[3], label, initialPercent, onChange, gradient);
        addRenderableWidget(slider);

        // Число 0-99 справа от слайдера (ТЗ: "справа от скролл баров добавь
        // обозначение числа") - отдельно от групповой подписи слева.
        int numberX = r[0] + r[2] + layout.realLen(14);
        int numberY = r[1] + (r[3] - this.font.lineHeight) / 2;
        addCenteredLabel(numberX + this.font.width("100") / 2, numberY, () -> Component.literal(String.valueOf(slider.getPercent())), TEXT_COLOR);
    }

    private void recomputeEyeColor() { draft.setEyeColor(hsvPercentToRgb(eyeHue, eyeSat, eyeVal)); }
    private void recomputeHairColor() { draft.setHairColor(hsvPercentToRgb(hairHue, hairSat, hairVal)); }
    private void recomputePantsColor() { draft.setPantsColor(hsvPercentToRgb(pantsHue, pantsSat, pantsVal)); }

    // --- Farm type cards (ТЗ §38-39) — 6 впритык слева + 1 справа (не сетка) ---

    /**
     * Ширина карточки индивидуально под РОДНУЮ пропорцию её кадра в farms.png
     * (высота у всех одна - {@link CharacterCreationLayout#FARM_CARD_HEIGHT}) -
     * "пропорцию x к y не менять" было явным условием.
     */
    private static int farmCardWidth(int index) {
        return Math.round(CharacterCreationLayout.FARM_CARD_HEIGHT * FARMS_TEX_FRAME_WIDTHS[index] / (float) FARMS_TEX_HEIGHT);
    }

    /** Виртуальные {vx,vy,vw,vh} карточки: индексы 0-5 - впритык друг под другом слева, индекс 6 - отдельно справа. */
    private static int[] farmCardVirtualBounds(int index) {
        int h = CharacterCreationLayout.FARM_CARD_HEIGHT;
        int w = farmCardWidth(index);
        if (index < 6) {
            int vy = CharacterCreationLayout.FARM_CARD_START_Y + index * (h + CharacterCreationLayout.FARM_CARD_GAP);
            return new int[]{CharacterCreationLayout.FARM_CARD_LEFT_X, vy, w, h};
        }
        int maxLeftWidth = 0;
        for (int i = 0; i < 6; i++) maxLeftWidth = Math.max(maxLeftWidth, farmCardWidth(i));
        int vx = CharacterCreationLayout.FARM_CARD_LEFT_X + maxLeftWidth + CharacterCreationLayout.FARM_CARD_RIGHT_GAP;
        return new int[]{vx, CharacterCreationLayout.FARM_CARD_START_Y, w, h};
    }

    private void buildFarmTypeCards() {
        FarmType[] types = FarmType.values();

        for (int i = 0; i < types.length; i++) {
            FarmType type = types[i];
            int[] vb = farmCardVirtualBounds(i);
            // Без текстуры ванильной кнопки - картинка САМА показывает тип
            // фермы (см. renderFarmCardSelection), сама кнопка фона не рисует.
            Button card = addImagelessButton(vb[0], vb[1], vb[2], vb[3], b -> { draft.setFarmType(type); playSound(ModSounds.CC_COIN.get()); });
            card.setTooltip(Tooltip.create(farmTypeTooltip(type)));

            // Мир для остальных типов ферм ещё не реализован
            // (WorldInitializationService, см. docs/architecture.md) - пока
            // выбрать можно только Standard, остальные показаны (с тултипом),
            // но задизейблены.
            if (type != FarmType.STANDARD) {
                card.active = false;
            }
        }
    }

    /** Чисто текстовая линия - у Tooltip нет способа нарисовать настоящее правило между строк, только текст. */
    private static final String TOOLTIP_SEPARATOR = "─────────────";

    private static Component farmTypeTooltip(FarmType type) {
        String key = type.name().toLowerCase(java.util.Locale.ROOT);
        Component name = Component.translatable("gui.stardew.character.farm_type." + key + ".name").copy().withStyle(ChatFormatting.BOLD);
        Component desc = Component.translatable("gui.stardew.character.farm_type." + key + ".desc");
        Component separator = Component.literal(TOOLTIP_SEPARATOR).withStyle(ChatFormatting.DARK_GRAY);
        Component tooltip = name.copy().append("\n").append(separator).append("\n").append(desc);
        if (type != FarmType.STANDARD) {
            tooltip = tooltip.copy().append("\n").append(Component.translatable("gui.stardew.character.farm_type.locked").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        return tooltip;
    }

    // --- Bottom controls: OK / Skip Intro / Randomize / Wrench (ТЗ §41, §45, §46) ---

    private void buildBottomControls() {
        // ok.png уже содержит текст "OK" - отдельная надпись поверх не нужна.
        okButton = addIconButton(CharacterCreationLayout.OK_X, CharacterCreationLayout.OK_Y,
                CharacterCreationLayout.OK_W, CharacterCreationLayout.OK_H,
                TEX_OK, b -> onOkPressed());

        int[] skipR = layout.real(CharacterCreationLayout.SKIP_INTRO_X, CharacterCreationLayout.SKIP_INTRO_Y,
                CharacterCreationLayout.SKIP_INTRO_SIZE, CharacterCreationLayout.SKIP_INTRO_SIZE);
        skipIntroBox = new Checkbox(skipR[0], skipR[1], skipR[2], skipR[3],
                Component.translatable("gui.stardew.character.skip_intro"), false);
        // Интро ещё не реализовано (см. ТЗ §45) - чекбокс скрыт до тех пор.
        skipIntroBox.visible = false;
        addRenderableWidget(skipIntroBox);

        addIconButton(CharacterCreationLayout.DICE_X, CharacterCreationLayout.DICE_Y,
                CharacterCreationLayout.DICE_W, CharacterCreationLayout.DICE_H,
                TEX_DICE, b -> randomizeAppearance());

        // Wrench (кнопка настроек) убрана ПОКА - заглушка без назначения и
        // без текстуры, попросили временно скрыть. Константы WRENCH_* в
        // layout оставлены нетронутыми на случай возврата кнопки.

        // TitleBackButton.bottomRight - тот же размер/позиция, что на
        // StardewLoadWorldScreen (см. чат: "должны быть одинаковы") - привязка
        // к this.width/height НАПРЯМУЮ, в обход virtual-canvas layout (тот на
        // разных экранах считает по-разному, идентичных реальных пикселей
        // так не получить), см. javadoc TitleBackButton.bottomRight.
        addRenderableWidget(dev.flomik.stardew.client.screen.title.TitleBackButton.bottomRight(
                this.width, this.height, b -> onBackPressed()));
    }

    /**
     * Выход отсюда назад - разрыв соединения с уже загруженным миром (тот
     * же порядок вызовов, что у ванильного {@code PauseScreen} "Save and
     * Quit to Title": {@code level.disconnect()} -> {@code clearLevel(...)}
     * -> {@code setScreen(...)}), А ЗАТЕМ - удаление самого save'а целиком
     * (см. чат: "если игрок ВЫХОДИТ из создания персонажа, то и мир не
     * генерится"). Безопасно удалять БЕЗУСЛОВНО: по {@link FirstJoinController}
     * этот экран открывается СТРОГО когда {@code !profile.isCharacterCreated()}
     * (иначе шлётся другой пакет, экран вообще не открывается) - то есть
     * если этот экран сейчас на экране, значит персонаж У ЭТОГО save'а
     * гарантированно ещё не подтверждён, удалять нечего жалко, "настоящих"
     * миров сюда попасть не может.
     * <p>
     * levelId читаем ДО disconnect/clearLevel - после них
     * {@code getSingleplayerServer()} обнуляется. {@code clearLevel(...)}
     * САМ блокируется (крутит {@code runTick} в цикле), пока
     * {@code IntegratedServer} не завершит {@code isShutdown()} - к моменту,
     * когда вызов возвращается, файлы save'а (включая {@code session.lock})
     * уже свободны, удалять безопасно сразу после, без дополнительных ожиданий.
     */
    private void onBackPressed() {
        if (this.minecraft == null || this.minecraft.level == null) return;

        String levelId = null;
        IntegratedServer server = this.minecraft.getSingleplayerServer();
        if (server != null) {
            levelId = server.getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
        }

        this.minecraft.level.disconnect();
        this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));

        if (levelId != null) {
            try (LevelStorageSource.LevelStorageAccess access = this.minecraft.getLevelSource().createAccess(levelId)) {
                access.deleteLevel();
            } catch (IOException e) {
                LOGGER.error("[Stardew] Failed to delete abandoned world {}", levelId, e);
            }
        }

        this.minecraft.setScreen(new StardewTitleScreen(previousGuiScale));
    }

    /**
     * Юбка ({@code pants_003}, miniskirt) и платье ({@code pants_004}, dress) -
     * рандомайзер не должен молча надеть их на "мужскую" (CLASSIC) модель,
     * см. {@code docs/character-randomizer-comparison.md} ("Мужчине рандомно
     * НЕ нужно одевать юбку и платье"). Захардкожено по id, а не по схеме
     * JSON - так же, как в оригинале хардкодится чёрный список ID рубашек
     * для мужского персонажа (см. {@code CharacterCustomization.cs:2341-2345}),
     * а не отдельное поле в данных.
     */
    private static final Set<String> FEMININE_ONLY_PANTS = Set.of("pants_003", "pants_004");

    /**
     * Первые {@value} записей {@code skins.json} - "нормальные" человеческие
     * тона (см. {@code docs/character-randomizer-comparison.md}), всё, что
     * после них, - фэнтезийные (красный/зелёный/бледно-голубой и т.п.) тона,
     * добавленные по тому же принципу, что и в оригинале
     * ({@code Next(6)} обычных, 15% шанс {@code Next(24)} по всему пулу
     * включая редкие - см. {@code Farmer.changeSkinColor}/
     * {@code CharacterCustomization.cs:2269-2276}).
     */
    private static final int NORMAL_SKIN_COUNT = 12;

    private void randomizeAppearance() {
        playSound(diceClickCount == 0 ? ModSounds.CC_DRUMKIT_6.get() : DICE_SOUND_POOL.get(random.nextInt(DICE_SOUND_POOL.size())).get());
        diceClickCount++;

        skinIndex = randomSkinIndex();
        String chosenHairId = randomHairId();
        hairIndex = hairIds.indexOf(chosenHairId);
        shirtIndex = random.nextInt(shirtIds.size());
        String chosenPantsId = randomPantsId();
        pantsIndex = pantsIds.indexOf(chosenPantsId);
        String chosenAccessoryId = randomAccessoryId();
        accessoryIndex = accessoryIds.indexOf(chosenAccessoryId);

        draft.setSkinId(skinIds.get(skinIndex));
        draft.setHairId(chosenHairId);
        draft.setShirtId(shirtIds.get(shirtIndex));
        draft.setPantsId(chosenPantsId);
        draft.setAccessoryId(chosenAccessoryId);

        // Цвет глаз/волос - портированный алгоритм оригинала (RGB со
        // случайными корректировками, не независимый HSV), см.
        // randomEyeColor()/randomHairColor(). Слайдеры (H/S/V) переведены в
        // соответствующее HSV-представление результата, чтобы не "прыгали"
        // при следующем ручном движении - иначе recomputeEyeColor() тут же
        // затёр бы рандомный цвет старым значением слайдера.
        int eyeColor = randomEyeColor();
        int hairColor = randomHairColor(draft.getSkinId());
        draft.setEyeColor(eyeColor);
        draft.setHairColor(hairColor);
        int[] eyeHsv = rgbToHsvPercent(eyeColor);
        eyeHue = eyeHsv[0]; eyeSat = eyeHsv[1]; eyeVal = eyeHsv[2];
        int[] hairHsv = rgbToHsvPercent(hairColor);
        hairHue = hairHsv[0]; hairSat = hairHsv[1]; hairVal = hairHsv[2];

        // Цвет штанов не тронут - ТЗ этой правки касается только пола/юбки,
        // волос/глаз и кожи/аксессуаров (см. чат), не цвета штанов.
        pantsHue = random.nextInt(100); pantsSat = random.nextInt(100); pantsVal = random.nextInt(100);
        recomputePantsColor();

        // Имя/название фермы/любимую вещь не рандомизируем (ТЗ §46).
        this.init(this.minecraft, this.width, this.height);
    }

    private int randomSkinIndex() {
        int normalCount = Math.min(NORMAL_SKIN_COUNT, skinIds.size());
        int index = random.nextInt(normalCount);
        if (random.nextDouble() < 0.15) {
            index = random.nextInt(skinIds.size());
        }
        return index;
    }

    private String randomPantsId() {
        List<String> candidates = pantsIds;
        if (draft.getModelType() == CharacterModelType.CLASSIC) {
            candidates = new ArrayList<>(pantsIds);
            candidates.removeAll(FEMININE_ONLY_PANTS);
            if (candidates.isEmpty()) {
                candidates = pantsIds;
            }
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * Лок по полу - ТОЛЬКО для рандомайзера (кубик), см. чат: "на рандомайзере
     * ставим лок на причёски в зависимости от пола, НО не делаем лок при
     * обычном редактировании" - обычное пролистывание стрелками [←][→]
     * (см. соответствующие обработчики кнопок) продолжает показывать ВСЕ
     * причёски без исключений, независимо от пола.
     *
     * "Подходит под пол" проверяется строго (через {@code male()}/{@code
     * female()} самой записи), а не через {@code variantFor} - тот при
     * отсутствии female-варианта молча возвращает male (это правильно для
     * обычного редактирования/показа, но рандомайзер должен видеть только
     * причёски с РЕАЛЬНЫМ вариантом под текущий пол).
     */
    private String randomHairId() {
        List<String> candidates = new ArrayList<>();
        for (String id : hairIds) {
            CosmeticDefinition definition = CosmeticRegistry.hair(id);
            if (definition == null) {
                continue;
            }
            boolean matchesGender = draft.getModelType() == CharacterModelType.CLASSIC
                    ? definition.male() != null
                    : definition.female() != null;
            if (matchesGender) {
                candidates.add(id);
            }
        }
        if (candidates.isEmpty()) {
            // Пока пользователь не разложил причёски по мужским/женским
            // папкам (см. HairCosmeticProvider) - страховка, чтобы рандомайзер
            // не падал на пустом списке.
            candidates = hairIds;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * В оригинале - взвешенный выбор среди десятков конкретных ID с разными
     * пулами на мужского/женского персонажа (см. docs-сравнение). У нас
     * всего 3 аксессуара без гендерной привязки, так что портируем только
     * СУТЬ поведения: реальный шанс не получить аксессуар вообще (33% что
     * он вообще будет, а не равномерно "1 из 4" как раньше, где "none" был
     * всего лишь одним из вариантов наравне с остальными).
     */
    private String randomAccessoryId() {
        List<String> real = new ArrayList<>(accessoryIds);
        real.remove("none");
        if (!real.isEmpty() && random.nextDouble() < 0.33) {
            return real.get(random.nextInt(real.size()));
        }
        return "none";
    }

    /** Портировано из {@code CharacterCustomization.cs:2396-2430} (см. docs/character-randomizer-comparison.md). */
    private int randomEyeColor() {
        int r = random.nextInt(25, 254) / 2;
        int g = random.nextInt(25, 254) / 2;
        int b = random.nextInt(25, 254) / 2;
        if (random.nextBoolean()) r = random.nextInt(15, 50);
        if (random.nextBoolean()) g = random.nextInt(15, 50);
        if (random.nextBoolean()) b = random.nextInt(15, 50);
        if (random.nextBoolean()) {
            if (b > r) b = Math.max(0, b - 50);
            if (b > g) b = Math.max(0, b - 50);
            if (g > r) g = Math.max(0, r - 50); // да, оригинал тут тоже сравнивает с R, а корректирует через R - повторяем один в один
        }
        return (r << 16) | (g << 8) | b;
    }

    /** Портировано из {@code CharacterCustomization.cs:2277-2336} (см. docs/character-randomizer-comparison.md). */
    private int randomHairColor(String skinId) {
        int r = random.nextInt(25, 254);
        int g = random.nextInt(25, 254);
        int b = random.nextInt(25, 254);
        if (random.nextBoolean()) { r /= 2; g /= 2; b /= 2; }
        if (random.nextBoolean()) r = random.nextInt(15, 50);
        if (random.nextBoolean()) g = random.nextInt(15, 50);
        if (random.nextBoolean()) b = random.nextInt(15, 50);
        if (random.nextBoolean()) {
            if (b > r) b = Math.max(0, b - 50);
            if (b > g) b = Math.max(0, b - 50);
            if (g > r) g = Math.max(0, r - 50); // см. коммент в randomEyeColor - тот же паттерн в оригинале
            r = Math.min(255, r + 50);
            g = Math.min(255, g + 50);
        } else if (random.nextDouble() < 0.33) {
            r = random.nextInt(80, 130);
            g = random.nextInt(35, 70);
            b = 0;
        }
        if (r < 100 && g < 100 && b < 100 && random.nextDouble() < 0.8) {
            // Utility.getBlendedColor(hairColor, Color.Tan) - "прижимает" тёмные
            // случайные цвета к загару, чтобы не было чисто-чёрных/серых волос.
            int[] blended = blendWithTan(r, g, b);
            r = blended[0]; g = blended[1]; b = blended[2];
        }
        if (isDarkSkin(skinId) && random.nextDouble() < 0.5) {
            r = random.nextInt(50, 100);
            g = random.nextInt(25, 40);
            b = 0;
        }
        return (r << 16) | (g << 8) | b;
    }

    /** Utility.getBlendedColor - на канал 50% шанс max(a,b), иначе среднее. Color.Tan = (210,180,140). */
    private int[] blendWithTan(int r, int g, int b) {
        return new int[]{
                random.nextBoolean() ? Math.max(r, 210) : (r + 210) / 2,
                random.nextBoolean() ? Math.max(g, 180) : (g + 180) / 2,
                random.nextBoolean() ? Math.max(b, 140) : (b + 140) / 2,
        };
    }

    /**
     * Оригинальный {@code Farmer.hasDarkSkin()} завязан на конкретные индексы
     * ЕГО собственной 24-тоновой палитры (0-23) - у нас другой набор тонов
     * другой длины, один-в-один индексы не переносятся. Вместо этого судим
     * по яркости базового цвета выбранного тона - функционально то же самое
     * (тёмный тон кожи -> чаще тёмные/каштановые волосы).
     */
    private boolean isDarkSkin(String skinId) {
        SkinToneDefinition tone = CosmeticRegistry.skin(skinId);
        int base = tone.base();
        int r = (base >> 16) & 0xFF, g = (base >> 8) & 0xFF, b = base & 0xFF;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return luminance < 100;
    }

    // --- OK / validation (ТЗ §41-43) ---

    private void onOkPressed() {
        if (processing) return;
        clearError();

        // Кнопка и так задизейблена, пока не заполнены все 3 поля (см.
        // updateOkButtonState) - это дополнительная защита, а не единственная линия.
        if (draft.getName() == null || draft.getName().isBlank()) {
            errorMessage = Component.translatable("gui.stardew.character.error.name_empty").getString();
            return;
        }
        if (draft.getFarmName() == null || draft.getFarmName().isBlank()) {
            errorMessage = Component.translatable("gui.stardew.character.error.farm_name_empty").getString();
            return;
        }
        if (draft.getFavoriteThing() == null || draft.getFavoriteThing().isBlank()) {
            errorMessage = Component.translatable("gui.stardew.character.error.favorite_thing_empty").getString();
            return;
        }

        processing = true;
        okButton.active = false;
        playSound(ModSounds.CC_COIN.get());

        PacketHandler.sendToServer(new C2SFinishCharacterCreation(
                draft.getName(), draft.getFarmName(), draft.getFavoriteThing(),
                draft.getAnimalPreference(), draft.getModelType(),
                draft.getSkinId(), draft.getEyeColor(),
                draft.getHairId(), draft.getHairColor(),
                draft.getShirtId(), draft.getShirtColor(),
                draft.getPantsId(), draft.getPantsColor(),
                draft.getAccessoryId(), draft.getFarmType()
        ));
    }

    private void clearError() {
        errorMessage = null;
    }

    /** Вызывается из {@link dev.flomik.stardew.client.character.ClientCharacterCreationOpener} по S2CCharacterCreationAccepted. */
    public void onServerAccepted() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    /** Вызывается из {@link dev.flomik.stardew.client.character.ClientCharacterCreationOpener} по S2CCharacterCreationRejected. */
    public void onServerRejected(String reasonKey) {
        processing = false;
        updateOkButtonState();
        errorMessage = Component.translatable(reasonKey).getString();
    }

    // --- Preview (ТЗ §22, §29-30) ---

    private void markDirty() {
        previewDirty = true;
        applyDraftToSkin(false);
    }

    private void applyDraftToSkin(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - lastRebuildTime < PREVIEW_REBUILD_DEBOUNCE_MS) {
            return;
        }
        lastRebuildTime = now;
        previewDirty = false;
        // Живой 3D-превью в углу экрана рендерит НАСТОЯЩЕГО локального игрока
        // (см. renderLivePreview) - refresh() тут не только обновляет пиксели
        // DynamicTexture, но и (с самого первого вызова) навсегда включает
        // подмену в mixin.client.PlayerInfoSkinLockMixin (см. его javadoc и
        // RuntimeSkinManager.hasComposedSkin()) - никакого отдельного "apply"
        // шага после refresh() больше не нужно.
        RuntimeSkinManager.refresh(draft);
    }

    private GuiGraphics currentGuiGraphics;

    // --- Вращение превью (замена vanilla "Direction"-стрелок на drag мышью) ---
    /**
     * Текущий угол разворота превью, градусы; 0 = лицом к камере.
     * Знак определяет, в какую сторону слегка развёрнут персонаж по
     * умолчанию - было +20 (разворот влево от игрока), поменяли на -20
     * (разворот вправо, как попросили).
     */
    private float previewYaw = -20f;
    private boolean draggingPreview;
    // Отрицательный знак - инвертированное направление (тянешь вправо, модель
    // крутится в противоположную от "естественной" сторону, как попросили).
    private static final float PREVIEW_DRAG_SENSITIVITY = -0.75f;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverPreview(mouseX, mouseY)) {
            draggingPreview = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPreview) {
            previewYaw = Mth.wrapDegrees(previewYaw + (float) dragX * PREVIEW_DRAG_SENSITIVITY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingPreview) {
            draggingPreview = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isOverPreview(double mouseX, double mouseY) {
        int[] r = layout.real(CharacterCreationLayout.PREVIEW_X, CharacterCreationLayout.PREVIEW_Y,
                CharacterCreationLayout.PREVIEW_W, CharacterCreationLayout.PREVIEW_H);
        return mouseX >= r[0] && mouseX < r[0] + r[2] && mouseY >= r[1] && mouseY < r[1] + r[3];
    }

    private GuiGraphics currentGuiGraphics() {
        return currentGuiGraphics;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.currentGuiGraphics = graphics;

        if (previewDirty) {
            applyDraftToSkin(false);
        }

        renderSky(graphics);

        // Верх/низ панелей читаются из ДВИГАЕМЫХ (F8) точек, а не заново из
        // статичных PANEL_Y/PANEL_H - см. их javadoc про то, почему рамку
        // раньше нельзя было подвинуть/подогнать под сместившийся контент.
        int[] panel = layout.real(CharacterCreationLayout.PANEL_X, CharacterCreationLayout.PANEL_Y,
                CharacterCreationLayout.PANEL_W, CharacterCreationLayout.PANEL_H);
        int panelTop = panelTopPoint.y;
        int panelHeight = panelBottomPoint.y - panelTop;
        StardewFrameRenderer.drawPanel(graphics, panel[0], panelTop, panel[2], panelHeight, FRAME_THICKNESS);

        int[] farmPanel = layout.real(CharacterCreationLayout.FARM_PANEL_X, CharacterCreationLayout.FARM_PANEL_Y,
                CharacterCreationLayout.FARM_PANEL_W, CharacterCreationLayout.FARM_PANEL_H);
        int farmPanelTop = farmPanelTopPoint.y;
        int farmPanelHeight = farmPanelBottomPoint.y - farmPanelTop;
        StardewFrameRenderer.drawPanel(graphics, farmPanel[0], farmPanelTop, farmPanel[2], farmPanelHeight, FRAME_THICKNESS);

        // НАЙДЕНА причина, почему текстура превью не показывалась (проверено
        // диагностикой с малиновой заливкой): StardewFrameRenderer рисует
        // панели через ОТЛОЖЕННЫЙ буфер (graphics.bufferSource().getBuffer(RenderType.gui())),
        // а blit() ниже - через СЫРОЙ Tesselator НЕМЕДЛЕННО. Без явного
        // flush() отложенная заливка панели фактически попадает на экран
        // ПОЗЖЕ (когда буфер сбрасывается чем-то следующим по кадру) и
        // перекрашивает уже нарисованную текстуру у себя же в отведённой
        // области - ровно то, что показал скриншот с малиновым тестом.
        graphics.flush();

        int[] preview = layout.real(CharacterCreationLayout.PREVIEW_X, CharacterCreationLayout.PREVIEW_Y,
                CharacterCreationLayout.PREVIEW_W, CharacterCreationLayout.PREVIEW_H);
        // dest size,dest size как u-width/texWidth и v-height/texHeight -
        // UV=[0,1] вне зависимости от нативных 128×192 файла (тот же приём,
        // что в IconButton, см. его javadoc).
        graphics.blit(TEX_CHARACTER_BG_DAY, preview[0], preview[1], 0, 0, preview[2], preview[3], preview[2], preview[3]);
        renderLivePreview(graphics, preview[0], preview[1], preview[2], preview[3]);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Цвет карточки должен перекрыть ванильную текстуру Button - рисуем
        // ПОСЛЕ super.render(), иначе кнопка перекрывает нашу заливку (виден
        // только стандартный оранжевый button skin, все карточки одинаковые).
        renderFarmCardSelection(graphics);

        for (Runnable r : textRenderers) r.run();

        if (errorMessage != null) {
            Component bold = Component.literal(errorMessage).withStyle(ChatFormatting.BOLD);
            int centerX = panel[0] + panel[2] / 2;
            int textY = panelBottomPoint.y - layout.realLen(CharacterCreationLayout.OK_H) - 10;
            // Тень та же, что у остального текста экрана - см. чат: "тень
            // цветом #dd9454 всему тексту(даже красному)".
            drawWithShadow(bold, centerX - this.font.width(bold) / 2, textY, ERROR_COLOR);
        }

        this.currentGuiGraphics = null;
    }

    private void renderSky(GuiGraphics graphics) {
        // Трёхточечный градиент - середина (SKY_MIDDLE) не лежит на прямой
        // между верхом и низом, обычная 2-цветная линейная интерполяция не
        // могла её задать (в центре получался смешанный #2e869f вместо
        // требуемого #0475e1).
        for (int y = 0; y < this.height; y++) {
            float t = (float) y / this.height;
            int color = t < 0.5f
                    ? lerpColor(SKY_TOP, SKY_MIDDLE, t * 2f)
                    : lerpColor(SKY_MIDDLE, SKY_BOTTOM, (t - 0.5f) * 2f);
            graphics.fill(0, y, this.width, y + 1, color);
        }
    }

    private void renderFarmCardSelection(GuiGraphics graphics) {
        FarmType[] types = FarmType.values();

        for (int i = 0; i < types.length; i++) {
            int[] vb = farmCardVirtualBounds(i);
            int[] r = layout.real(vb[0], vb[1], vb[2], vb[3]);

            // Кадры farms.png РАЗНОЙ ширины (18/19/18×5) при одной высоте
            // (20) - обычный "fakeTexWidth = destSize*count" трюк тут не
            // годится (кадры не одинаковые), формула индивидуальна на кадр:
            // fakeTexW = destW*127/w_i, u = destW*x_i/w_i - тогда
            // UV = [x_i/127, (x_i+w_i)/127] ровно на границу кадра, при любом destW.
            int nativeWidth = FARMS_TEX_FRAME_WIDTHS[i];
            int nativeXOffset = 0;
            for (int k = 0; k < i; k++) nativeXOffset += FARMS_TEX_FRAME_WIDTHS[k];
            int fakeTexWidth = Math.round(r[2] * FARMS_TEX_TOTAL_WIDTH / (float) nativeWidth);
            float u = r[2] * nativeXOffset / (float) nativeWidth;

            // Серый оттенок для пока-недоступных типов фермы - раньше это
            // давала ванильная текстура Button, но убрали её совсем
            // (ImagelessButton), так что тонируем саму картинку.
            boolean locked = types[i] != FarmType.STANDARD;
            if (locked) {
                graphics.setColor(0.45f, 0.45f, 0.45f, 1f);
            }
            graphics.blit(TEX_FARMS, r[0], r[1], u, 0f, r[2], r[3], fakeTexWidth, r[3]);
            if (locked) {
                graphics.setColor(1f, 1f, 1f, 1f);
            }

            if (types[i] == draft.getFarmType()) {
                // Та же текстура рамки выбора, что и у пола (TEX_GENDER_SELECTED),
                // просто под другой масштаб - карточка фермы крупнее и
                // прямоугольная (не квадратная), с отступом в FARM_CARD_SELECTION_PADDING
                // с каждой стороны вместо равнения 1:1 с самой кнопкой.
                int pad = FARM_CARD_SELECTION_PADDING;
                graphics.blit(TEX_GENDER_SELECTED, r[0] - pad, r[1] - pad, 0, 0,
                        r[2] + pad * 2, r[3] + pad * 2, r[2] + pad * 2, r[3] + pad * 2);
            }
        }
    }

    /**
     * Вместо vanilla {@link InventoryScreen#renderEntityInInventoryFollowsMouse} (та
     * даёт только лёгкий "взгляд за курсором" в пределах ±40° - через
     * {@code atan(offset/40)}) - вручную выставляем углы модели из
     * {@link #previewYaw} (крутится драгом мыши по превью, см. {@link #mouseDragged})
     * и рендерим через низкоуровневый {@code renderEntityInInventory}, как
     * это по факту делает сам vanilla-метод под капотом (см.
     * {@code InventoryScreen.renderEntityInInventoryFollowsAngle}) - просто с
     * полным углом вместо capped-мышиного.
     */
    private void renderLivePreview(GuiGraphics graphics, int boxX, int boxY, int boxW, int boxH) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        int centerX = boxX + boxW / 2;
        // -30 (виртуальных единиц) - подняли ТОЛЬКО модель персонажа, не
        // фоновую картинку (та рисуется отдельным блитом раньше, эта правка
        // её не касается).
        int feetY = boxY + boxH - Math.max(4, boxH / 16) - layout.realLen(30);
        int scale = Math.max(8, Math.min(boxW, boxH) / 2);

        var player = this.minecraft.player;
        float oldBodyRot = player.yBodyRot;
        float oldYRot = player.getYRot();
        float oldXRot = player.getXRot();
        float oldHeadRotO = player.yHeadRotO;
        float oldHeadRot = player.yHeadRot;

        player.yBodyRot = 180f + previewYaw;
        player.setYRot(180f + previewYaw);
        player.setXRot(0f);
        player.yHeadRot = player.getYRot();
        player.yHeadRotO = player.getYRot();

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        InventoryScreen.renderEntityInInventory(graphics, centerX, feetY, scale, rotation, null, player);

        player.yBodyRot = oldBodyRot;
        player.setYRot(oldYRot);
        player.setXRot(oldXRot);
        player.yHeadRotO = oldHeadRotO;
        player.yHeadRot = oldHeadRot;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // ТЗ §4: обязательный первый экран нельзя закрыть Escape.
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --- Color math (H/S/V проценты 0-100 <-> RGB int для CharacterProfile - как в vanilla ColorPicker) ---

    private static int hsvPercentToRgb(int huePercent, int satPercent, int valPercent) {
        float h = (huePercent / 100f) * 360f;
        float s = satPercent / 100f;
        float v = valPercent / 100f;
        int rgb = java.awt.Color.HSBtoRGB(h / 360f, s, v);
        return rgb & 0xFFFFFF;
    }

    private static int[] rgbToHsvPercent(int rgb) {
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, hsb);
        return new int[]{
                Math.round(hsb[0] * 100f),
                Math.round(hsb[1] * 100f),
                Math.round(hsb[2] * 100f)
        };
    }

    private static int lerpColor(int from, int to, float t) {
        t = Mth.clamp(t, 0f, 1f);
        int a = lerpChannel(from >> 24, to >> 24, t);
        int r = lerpChannel(from >> 16, to >> 16, t);
        int g = lerpChannel(from >> 8, to >> 8, t);
        int b = lerpChannel(from, to, t);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
    }
}

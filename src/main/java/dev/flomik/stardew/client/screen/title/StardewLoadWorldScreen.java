package dev.flomik.stardew.client.screen.title;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.CustomCursor;
import dev.flomik.stardew.client.TitleScreenReplacer;
import dev.flomik.stardew.client.character.skin.CardPortraitComposer;
import dev.flomik.stardew.client.render.StardewFrameRenderer;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.common.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * "Load" — отдельный от ванильного {@code SelectWorldScreen} экран (см. чат,
 * референс-скриншот оригинального меню Stardew Valley) со своей карточкой на
 * каждое сохранение. Подменяет собой единственный путь, которым игрок вообще
 * попадал на {@code SelectWorldScreen} ({@code StardewTitleScreen} заменяет
 * весь {@code TitleScreen} целиком) — сам ванильный экран выбора миров
 * (и {@code mixin.client.SelectWorldScreenMixin}, который на него навешивал
 * кнопку) больше не используется и удалён.
 *
 * ВАЖНО про {@link ObjectSelectionList}: по умолчанию она САМА рисует
 * ванильный "земляной" фон и градиентные полосы сверху/снизу
 * ({@code setRenderBackground}/{@code setRenderTopAndBottom}, оба true по
 * умолчанию) — первая версия этого экрана эту особенность не учла, из-за
 * чего дефолтный фон рисовался ПОВЕРХ звёздного неба/панели и полностью их
 * скрывал (см. чат — "второй экран вообще не совпадает"). Оба флага теперь
 * явно выключены в {@link #init()}.
 *
 * Рамка панели — {@link StardewFrameRenderer#drawPanel}, ТА ЖЕ самая, что у
 * {@code CharacterCreationScreen} (см. чат: "рамку сделай такую же, как в
 * редакторе персонажа") - тот рендер сам рисует "срезанные" углы/тень наружу
 * от переданного rect'а, содержимое строк позиционируется относительно
 * ИСХОДНОГО (не раздутого рамкой) {@code panelX/Y/Width/Height}. Иконки
 * монеты/часов — реальные текстуры ({@code textures/gui/icons/coin.png}/
 * {@code clock.png}, см. чат), удаление — ещё РИСУЕТСЯ примитивом (нет
 * нарезанной текстуры под неё). "Back" — реальная текстура
 * ({@link TitleBackButton}), как на {@link StardewTitleScreen}.
 *
 * Скроллбар — реальные текстуры из {@code LooseSprites/Cursors.png} (см. чат:
 * "полностью как в оригинале" - сверено по декомпилированному
 * {@code LoadGameMenu.cs}: {@code upArrow}/{@code downArrow}/{@code scrollBar}
 * - {@code ClickableTextureComponent} с UV (421,459,11,12)/(421,472,11,12)/
 * (435,463,6,10) на {@code Game1.mouseCursors}). Своя обводка/заливка/тень
 * "рельса" ({@link #RAIL_OUTLINE}/{@link #RAIL_FILL}/{@link #RAIL_SHADE},
 * см. их javadoc) - в самой Stardew Valley отдельной текстуры рельса нет,
 * {@code scrollBarRunner} там - только Rectangle для хит-теста, не картинка,
 * так что рельс тут - наше решение, не портирование конкретного ассета.
 * Рисуется на ВСЮ высоту трассы (y0..y1) ПЕРЕД текстурой бегунка (см. чат:
 * "забыл убрать ванильный бегунок") - ванильный {@code graphics.fill(...)}
 * (см. {@code AbstractSelectionList.render()}) рисует ДВА прямоугольника, не
 * только сам бегунок, а ЕЩЁ и сплошной чёрный трек на всю высоту под ним;
 * перекрытие ТОЛЬКО бегунка оставляло этот чёрный трек видимым сверху/снизу.
 *
 * Скролл-ФИЗИКА - НЕ ванильная, своя, целиком в {@link WorldList} (см.
 * {@code mouseScrolled}/{@code mouseClicked}/{@code mouseDragged}): ванильная
 * прокрутка непрерывна по пикселям и калибрует скорость драга под
 * ПРОПОРЦИОНАЛЬНЫЙ размеру контента бегунок, которого у нас нет (наш -
 * фиксированного размера, {@link #SCROLL_THUMB_H}, как в оригинале) - отсюда
 * были два разных бага (см. чат): "рамка... при скроле вниз пропадает"
 * (непрерывный scrollAmount редко кратен itemHeight -> верхняя карточка
 * наполовину прокручена -> её верхняя граница обрезается scissor'ом) и
 * "скролл убегает от курсора" (ванильная формула скорости драга откалибрована
 * под чужой размер бегунка). Обе строки - целыми ROW (колесо/стрелки) и
 * честным 1:1 (в единицах scrollAmount/px) драгом бегунка мышью.
 *
 * F8-дебаг ({@code DebugLayoutDragTool}) тут НЕ подключён (в отличие от
 * Title/CharacterCreation) - была попытка привязать его к строке
 * {@code index == 0} через {@code DraggableTextProvider} (см. историю чата),
 * но {@code AbstractSelectionList.getRowTop(0)} зависит от текущего скролла,
 * а сохранённая {@code DraggablePoint}-позиция - абсолютная и "замороженная"
 * на момент создания; при скролле строка 0 уезжала на новый {@code top}, а
 * её иконка/текст оставались на старом месте и визуально "отрывались" от
 * карточки (см. чат: "при скроле у нас иконка персонажа и его инфа на первой
 * карточке улетает"). Позиции элементов - обычные формулы от текущих
 * {@code top}/{@code left} на каждый кадр, как у любой другой строки.
 *
 * Данные по каждой записи — НАСТОЯЩИЕ: имя персонажа и название мира
 * ({@link LevelSummary}) читаются из {@link WorldEntry#readCharacterName}
 * (NBT нашей {@code CharacterProfileProvider}-capability в
 * {@code playerdata/<uuid>.dat} ЗАКРЫТОГО мира); время игры —
 * {@link WorldEntry#readPlayTime} (ванильная статистика
 * {@code stats/<uuid>.json}, {@code minecraft:play_time}); игровая дата —
 * {@link WorldEntry#readGameDate} ({@code data/stardew_date.dat}, наша
 * {@code StardewDateData}); золото — {@link WorldEntry#readMoney}
 * ({@code PlayerStardewState}-capability из того же playerdata-файла).
 */
public class StardewLoadWorldScreen extends Screen {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int ENTRY_HEIGHT = 68;
    private static final int DELETE_HITBOX_SIZE = 18;

    /** Монетка/часы/удаление (см. чат) - реальные текстуры, все три 16×16 нативно. */
    private static final ResourceLocation COIN_ICON = new ResourceLocation(StardewMod.MODID, "textures/gui/icons/coin.png");
    private static final ResourceLocation CLOCK_ICON = new ResourceLocation(StardewMod.MODID, "textures/gui/icons/clock.png");
    private static final ResourceLocation DELETE_ICON = new ResourceLocation(StardewMod.MODID, "textures/gui/icons/delete.png");
    private static final int ICON_NATIVE = 16;
    /** Увеличено (см. чат: "иконки денег и времени увеличь... их размеры должны быть одинаковы") - нативный размер всех трёх иконок, без масштабирования. */
    private static final int ICON_SIZE = ICON_NATIVE;

    /**
     * Скроллбар (см. чат: "можем поменять текстуру скроллбара? где её
     * взять?" -> "полностью как в оригинале") - вырезано из настоящего
     * {@code Cursors.png} (см. javadoc класса про точные UV). Один общий
     * SCALE=2 для стрелок И бегунка - в оригинале тоже единый множитель (4f)
     * на оба, тут просто меньше, под наш экран.
     */
    private static final ResourceLocation SCROLL_UP_ICON = new ResourceLocation(StardewMod.MODID, "textures/gui/scrollbar/up_arrow.png");
    private static final ResourceLocation SCROLL_DOWN_ICON = new ResourceLocation(StardewMod.MODID, "textures/gui/scrollbar/down_arrow.png");
    private static final ResourceLocation SCROLL_THUMB_ICON = new ResourceLocation(StardewMod.MODID, "textures/gui/scrollbar/thumb.png");
    private static final int SCROLL_ARROW_NATIVE_W = 11, SCROLL_ARROW_NATIVE_H = 12;
    private static final int SCROLL_THUMB_NATIVE_W = 6, SCROLL_THUMB_NATIVE_H = 10;
    private static final int SCROLLBAR_SCALE = 2;
    private static final int SCROLL_ARROW_W = SCROLL_ARROW_NATIVE_W * SCROLLBAR_SCALE, SCROLL_ARROW_H = SCROLL_ARROW_NATIVE_H * SCROLLBAR_SCALE;
    private static final int SCROLL_THUMB_W = SCROLL_THUMB_NATIVE_W * SCROLLBAR_SCALE, SCROLL_THUMB_H = SCROLL_THUMB_NATIVE_H * SCROLLBAR_SCALE;
    /** Зазор между стрелками и дорожкой бегунка. */
    private static final int SCROLL_ARROW_GAP = 2;
    // Полупрозрачность стрелок УБРАНА (см. чат: "не надо делать полупрозрачными
    // стрелки. там есть в оригинале альфаканал... не надо полупрозрачности") -
    // у самого up_arrow.png/down_arrow.png УЖЕ есть частично прозрачные пиксели
    // (проверено: альфа-канал содержит не только 0/255, но и промежуточные
    // значения на сглаженных краях) - обычный blit их и так учитывает через
    // стандартный alpha-blend, добавлять свой tint поверх было лишним и только
    // делало все пиксели (в т.ч. уже непрозрачные) тусклее.

    /**
     * Свой "рельс" под бегунком (см. чат: "внутри скроллбара сделай
     * #bdac8f. обводку его делай #44121c. #a58c72 по правому краю и верху
     * внутри"): {@link #RAIL_OUTLINE} - самая внешняя обводка, внутри неё
     * {@link #RAIL_FILL} основной заливкой, а верхний и правый ВНУТРЕННИЙ
     * край перекрашены в {@link #RAIL_SHADE} (тень, будто рельс утоплен) -
     * ПРОСТОЙ прямоугольник со всех 4 сторон, БЕЗ обработки углов (см. чат:
     * "почему у нас всё равно есть уголки у скролл бара? их надо убрать" -
     * прежний срез углов квадратом в RAIL_FILL всё равно читался как уголки).
     * {@link #RAIL_LINE} - толщина ОДНОЙ описанной линии/пикселя (обводка,
     * тень) - {@link #SCROLLBAR_SCALE}, а НЕ 1 (см. чат: "ты
     * сделал неверные размеры. размер одного описанного мной пикселя
     * соответствует размеру линии в окне UI") - весь скроллбар (стрелки,
     * бегунок) уже нарисован в 2x от нативного, свои же добавленные линии
     * рельса должны быть в том же масштабе, а не голым 1px поверх 2x-элементов.
     */
    private static final int RAIL_LINE = SCROLLBAR_SCALE;
    private static final int RAIL_OUTLINE = 0xFF44121C;
    private static final int RAIL_FILL = 0xFFBDAC8F;
    private static final int RAIL_SHADE = 0xFFA58C72;

    private static final float PANEL_WIDTH_RATIO = 0.62f;
    /** РОВНО 5 карточек видно одновременно, не "сколько влезет" по соотношению высоты экрана (см. чат: "увеличить это окно до 5ти РОВНО вкладок"). */
    private static final int VISIBLE_ROWS = 5;
    /** Та же техника рамки, что у {@code CharacterCreationScreen} (см. {@link StardewFrameRenderer}), но толщина на 1 меньше (3 -> 2) - попросили именно тут, только на этом экране. */
    private static final int FRAME_THICKNESS = 2;

    /**
     * СВОИ цвета рамки панели - только у Load-экрана, общая рамка остальных
     * экранов/тултипов не тронута (см. чат: "нужно было создать ОТДЕЛЬНУЮ для
     * Load экрана"). Обводка от края внутрь: PANEL_EDGE -> PANEL_OUTER ->
     * PANEL_MIDDLE -> PANEL_INNER (на одну внешнюю линию больше стандартной),
     * потом тень PANEL_SHADOW и заливка PANEL_BG.
     */
    private static final int PANEL_BG = 0xFFFAE0B6;
    private static final int PANEL_EDGE = 0xFF44121C;
    private static final int PANEL_OUTER = 0xFF853605;
    private static final int PANEL_MIDDLE = 0xFFB14E05;
    private static final int PANEL_INNER = 0xFF853605;
    private static final int PANEL_SHADOW = 0xFF44121C;

    /** Имя персонажа и порядковый номер - жирным, см. чат. */
    private static final int COLOR_TEXT_NAME = 0xFF56160C;
    /** Название фермы/дата/деньги+время - см. чат. */
    private static final int COLOR_TEXT_MUTED = 0xFF221122;
    private static final int COLOR_CARD_BG = 0xFFFFD284;
    /** Цвета карточки ПРИ НАВЕДЕНИИ - подмена, а не статичное затемнение поверх (см. чат: "смена цвета карточки ПРИ наведении, а не статика"). */
    private static final int HOVER_CARD_BG = 0xFFF5B75D;
    private static final int HOVER_FRAME_INNER = 0xFFF5C782;
    /** Кастомная тень (не автозатемнение drawString) - см. чат: "замени цвет теней". */
    private static final int SHADOW_NAME = 0xFFE09650;
    private static final int SHADOW_MUTED = 0xFFDD9454;

    /**
     * Обрамление САМОЙ карточки - ОДИНАКОВО на ВСЕХ 4 сторонах (верх/лево/
     * право/низ): коричневый ({@link #FRAME_OUTER}) примыкает к контенту
     * карточки, белый/кремовый ({@link #FRAME_INNER}) лежит СНАРУЖИ него (см.
     * чат: "верх лево право и низ коричневый, а после идёт белый. со всех
     * сторон, а не с трёх"). Каждая полоса - {@link #BAND_THICKNESS}px,
     * {@link #CARD_BORDER} = обе полосы вместе. Рисуется ВЛОЖЕННЫМИ
     * прямоугольниками (белый, в нём коричневый, в нём фон), а не полосами по
     * сторонам - иначе полосы на углах выезжали одна за другую (см. чат:
     * "каждая линия выходит на пиксель дальше").
     *
     * Шов между двумя соседними карточками ({@link #SEPARATOR}) - НЕ часть
     * границы самой карточки (см. чат: "больше ничего нет снизу"), это
     * отдельная ОДНА полоса ({@link #SEPARATOR_THICKNESS}) в зазоре между
     * низом одной карточки и верхом следующей: у ПОСЛЕДНЕЙ карточки шва нет
     * ("у неё нет соседа"), и клики по шву мёртвые (см. mouseClicked).
     */
    private static final int FRAME_OUTER = 0xFF843605;
    private static final int FRAME_INNER = 0xFFFAE0B6;
    private static final int SEPARATOR = 0xFF44121C;
    private static final int BAND_THICKNESS = 2;
    private static final int CARD_BORDER = BAND_THICKNESS * 2; // коричневый+белый, одинаково на всех 4 сторонах
    private static final int SEPARATOR_THICKNESS = BAND_THICKNESS; // шов МЕЖДУ карточками, не их собственная граница

    /** Фиксированный узор звёзд (доли ширины/высоты, не абсолютные пиксели) - стабилен между resize'ами окна, см. render(). */
    private static final int STAR_COUNT = 70;
    private static final float[] STAR_FX = new float[STAR_COUNT];
    private static final float[] STAR_FY = new float[STAR_COUNT];

    static {
        Random random = new Random(20260707L);
        for (int i = 0; i < STAR_COUNT; i++) {
            STAR_FX[i] = random.nextFloat();
            STAR_FY[i] = random.nextFloat();
        }
    }

    private final Screen lastScreen;
    private final int previousGuiScale;
    private WorldList list;
    private ScrollArrowButton scrollUpButton, scrollDownButton;

    private int panelX, panelY, panelWidth, panelHeight;

    /** {@code previousGuiScale} - см. {@link StardewTitleScreen#StardewTitleScreen(int)} и {@link TitleScreenReplacer} - тот же guiScale-контракт, продолженный со Title на Load. */
    public StardewLoadWorldScreen(Screen lastScreen, int previousGuiScale) {
        super(Component.translatable("gui.stardew.title.load"));
        this.lastScreen = lastScreen;
        this.previousGuiScale = previousGuiScale;
    }

    @Override
    protected void init() {
        // Тот же кастомный курсор, что в редакторе персонажа (см. чат).
        CustomCursor.apply();

        this.panelWidth = Math.round(this.width * PANEL_WIDTH_RATIO);
        // РОВНО VISIBLE_ROWS карточек, не доля высоты экрана - см. javadoc VISIBLE_ROWS.
        this.panelHeight = VISIBLE_ROWS * ENTRY_HEIGHT;
        this.panelX = (this.width - panelWidth) / 2;
        this.panelY = Math.round(this.height * 0.14f);

        // Карточки ровно впритык к панели со ВСЕХ сторон (см. чат: "не должно
        // быть пустого места вне карточки. всё нужно впритык. и по бокам и по
        // верх низ"). -4 тут - это НЕ отступ, а компенсация чужого, ванильного:
        // AbstractSelectionList.render() всегда рисует первую строку от
        // {@code y0 + 4}, жёстко (см. декомпилированный исходник, не
        // переопределяемо) - без этого -4 карточки были бы на 4px ниже, чем
        // panelY, и сверху просвечивал бы bgColor панели. Скроллбар сдвинут в
        // свой собственный отступ через getScrollbarPosition() (+40, см. её
        // javadoc), сюда его больше закладывать не нужно.
        int innerTop = panelY - 4;
        int innerBottom = panelY + panelHeight;
        int rowWidth = panelWidth;

        this.list = new WorldList(this.minecraft, this.width, this.height, innerTop, innerBottom, ENTRY_HEIGHT, previousGuiScale, rowWidth);
        this.list.setRenderBackground(false);
        this.list.setRenderTopAndBottom(false);
        addWidget(this.list);
        reload();

        // Стрелки скроллбара (см. javadoc класса про "полностью как в
        // оригинале") - центрированы по X над/под дорожкой бегунка (та же
        // scrollbarPosition, что у бегунка), сразу за пределами track'а
        // (innerTop/innerBottom, см. выше) - SCROLL_ARROW_GAP зазор.
        int scrollbarX = this.list.getScrollbarPosition();
        int arrowX = scrollbarX + SCROLL_THUMB_W / 2 - SCROLL_ARROW_W / 2;
        this.scrollUpButton = new ScrollArrowButton(arrowX, innerTop - SCROLL_ARROW_H - SCROLL_ARROW_GAP,
                SCROLL_ARROW_W, SCROLL_ARROW_H, SCROLL_UP_ICON, b -> this.list.scrollByRow(-1));
        this.scrollDownButton = new ScrollArrowButton(arrowX, innerBottom + SCROLL_ARROW_GAP,
                SCROLL_ARROW_W, SCROLL_ARROW_H, SCROLL_DOWN_ICON, b -> this.list.scrollByRow(1));
        addRenderableWidget(this.scrollUpButton);
        addRenderableWidget(this.scrollDownButton);

        // TitleBackButton.bottomRight - тот же размер/позиция, что на
        // CharacterCreationScreen (см. чат: "должны быть одинаковы"), не
        // свой расчёт от panelWidth.
        addRenderableWidget(TitleBackButton.bottomRight(this.width, this.height, b -> this.minecraft.setScreen(this.lastScreen)));
    }

    private void reload() {
        LevelStorageSource.LevelCandidates candidates;
        try {
            candidates = this.minecraft.getLevelSource().findLevelCandidates();
        } catch (Exception e) {
            LOGGER.error("[Stardew] Failed to list saves", e);
            return;
        }
        this.minecraft.getLevelSource().loadLevelSummaries(candidates)
                .thenAcceptAsync(this.list::reloadEntries, this.minecraft)
                .exceptionally(e -> {
                    LOGGER.error("[Stardew] Failed to load save summaries", e);
                    return null;
                });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Звёздное небо (тёмно-синий -> фиолетовый), см. референс-скриншот.
        graphics.fillGradient(0, 0, this.width, this.height, 0xFF16213E, 0xFF4A3466);
        for (int i = 0; i < STAR_COUNT; i++) {
            int sx = (int) (STAR_FX[i] * this.width);
            int sy = (int) (STAR_FY[i] * this.height);
            int shade = (i % 3 == 0) ? 0xFFFFFFFF : 0x99FFFFFF;
            graphics.fill(sx, sy, sx + 1, sy + 1, shade);
        }

        // Отдельная 4-линейная рамка со своими цветами - только у этого экрана (см. javadoc PANEL_BG).
        StardewFrameRenderer.drawPanel(graphics, panelX, panelY, panelWidth, panelHeight, FRAME_THICKNESS,
                PANEL_BG, PANEL_EDGE, PANEL_OUTER, PANEL_MIDDLE, PANEL_INNER, PANEL_SHADOW);
        // ОБЯЗАТЕЛЬНЫЙ flush() - та же причина, что задокументирована в
        // CharacterCreationScreen.render(): StardewFrameRenderer копит вершины
        // в ОТЛОЖЕННЫЙ буфер (RenderType.gui()). Без явного сброса ЗДЕСЬ он
        // фактически сбрасывается позже, уже ВНУТРИ scissor-области списка
        // (list.render() ограничивает видимую область по вертикали rows'ов)
        // - верхняя/нижняя планки рамки лежат СНАРУЖИ этой вертикальной
        // scissor-полосы и обрезаются подчистую, а левая/правая (бегут по
        // всей высоте) видны частично - ровно то, что было на скриншоте
        // ("верх и низ рамок нет, только боковые").
        graphics.flush();

        this.list.render(graphics, mouseX, mouseY, partialTick);

        // Свой рельс + текстурный бегунок ПОВЕРХ ванильного (см. javadoc
        // класса про скроллбар). Ванильный fill() - это НЕ только прямоугольник
        // бегунка: {@code AbstractSelectionList.render()} рисует ЕЩЁ и сплошной
        // чёрный трек НА ВСЮ высоту (y0..y1), под бегунком - если перекрыть
        // текстурой только сам бегунок (маленький прямоугольник), этот чёрный
        // трек остаётся видно сверху/снизу от него (см. чат: "забыл убрать
        // ванильный бегунок"). Поэтому сначала красим ВЕСЬ трек своим
        // "рельсом" (на всю высоту, шире ванильных 6px, чтобы точно перекрыть
        // его), и только потом - текстуру бегунка поверх (см. чат: "добавить
        // текстуру по которой едет скролл").
        int maxScroll = this.list.getMaxScroll();
        boolean scrollable = maxScroll > 0;
        this.scrollUpButton.visible = scrollable;
        this.scrollDownButton.visible = scrollable;
        if (scrollable) {
            int trackTop = panelY - 4; // та же формула, что innerTop в init()
            int trackBottom = panelY + panelHeight; // та же формула, что innerBottom в init()
            int thumbX = this.list.getScrollbarPosition();
            int railHeight = trackBottom - trackTop;

            // Обводка - СПЛОШНАЯ, ПРОСТОЙ ПРЯМОУГОЛЬНИК, БЕЗ какой-либо
            // обработки углов (см. чат: "почему у нас всё равно есть уголки
            // у скролл бара? их надо убрать") - раньше углы красились
            // квадратом в цвет заливки RAIL_FILL ("срезанный" вид), но это
            // всё равно читалось как заметные уголки - убрали decor вообще,
            // прямоугольник теперь честно прямоугольный со всех 4 сторон.
            graphics.fill(thumbX, trackTop, thumbX + SCROLL_THUMB_W, trackBottom, RAIL_OUTLINE);
            graphics.fill(thumbX + RAIL_LINE, trackTop + RAIL_LINE, thumbX + SCROLL_THUMB_W - RAIL_LINE, trackBottom - RAIL_LINE, RAIL_FILL);
            // Верхний внутренний край - тень (свет как бы слева-снизу).
            graphics.fill(thumbX + RAIL_LINE, trackTop + RAIL_LINE, thumbX + SCROLL_THUMB_W - RAIL_LINE, trackTop + 2 * RAIL_LINE, RAIL_SHADE);
            // Правый внутренний край - тень.
            graphics.fill(thumbX + SCROLL_THUMB_W - 2 * RAIL_LINE, trackTop + RAIL_LINE, thumbX + SCROLL_THUMB_W - RAIL_LINE, trackBottom - RAIL_LINE, RAIL_SHADE);

            double fraction = this.list.getScrollAmount() / maxScroll;
            int travel = railHeight - SCROLL_THUMB_H;
            int thumbY = trackTop + (int) Math.round(fraction * travel);
            graphics.blit(SCROLL_THUMB_ICON, thumbX, thumbY, SCROLL_THUMB_W, SCROLL_THUMB_H,
                    0, 0, SCROLL_THUMB_NATIVE_W, SCROLL_THUMB_NATIVE_H, SCROLL_THUMB_NATIVE_W, SCROLL_THUMB_NATIVE_H);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        CustomCursor.restore();
        if (this.list != null) {
            for (WorldEntry entry : this.list.children()) {
                entry.close();
            }
        }
    }

    private static class WorldList extends ObjectSelectionList<WorldEntry> {

        private final int previousGuiScale;
        private final int rowWidth;

        WorldList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, int previousGuiScale, int rowWidth) {
            super(minecraft, width, height, y0, y1, itemHeight);
            this.previousGuiScale = previousGuiScale;
            this.rowWidth = rowWidth;
        }

        /** Ширина строки — внутренняя область ПАНЕЛИ, а не весь экран (ванильные 220 по умолчанию) - {@link #getRowLeft()} центрирует её в {@code this.width} (полная ширина экрана), которая совпадает с центровкой панели, потому что панель тоже центрирована на весь экран. */
        @Override
        public int getRowWidth() {
            return rowWidth;
        }

        /**
         * Формула НАМЕРЕННО {@code (width - rowWidth) / 2}, а не ванильное
         * {@code width/2 - rowWidth/2}: при ЧЁТНОЙ ширине экрана и НЕЧЁТНОЙ
         * ширине панели целочисленное деление даёт разницу в 1px с формулой
         * позиции панели ({@code panelX = (width - panelWidth) / 2}), и весь
         * ряд вставал на 1px правее панели - слева просвечивала колонка её
         * фона (см. чат: "слева сепаратор чуть укорочен буквально на
         * пиксель"). Одинаковая формула = побитово одинаковый результат.
         *
         * Ванильный {@code AbstractSelectionList.getRowLeft()} ещё и жёстко
         * прибавляет {@code +2} ({@code x0 + width/2 - rowWidth/2 + 2}) - не
         * настраиваемо, зашито в декомпилированном исходнике. Из-за этого
         * карточки были на 2px правее panelX, оставляя просвечивающий зазор
         * bgColor панели слева (см. чат: "что за пустоты? почему есть
         * отступы?"). Убираем это +2 тут же.
         */
        @Override
        public int getRowLeft() {
            return (this.width - rowWidth) / 2;
        }

        /**
         * Ванильная формула ({@code width/2 + 124}) считает позицию скроллбара
         * от СВОЕГО дефолтного row width (220) - с нашим гораздо более широким
         * {@link #getRowWidth()} скроллбар без этого override рисовался
         * посреди строки, наезжая на текст (см. чат со скриншотом). Ставим
         * его за правым краем строки, +40 сверх исходного отступа (см. чат:
         * "скролл бар передвинь вправо на 40" - строки теперь идут ВПРИТЫК к
         * панели, без своего гуттера под скроллбар, поэтому его увели дальше
         * вправо отдельно, а не расчётом от rowWidth).
         */
        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + rowWidth / 2 + 4 + 40;
        }

        /**
         * Обрезаем на {@code y0 + 4}, НЕ на {@code y0} (см. чат: "снизу
         * рисуешь низ карточки + сепаратор + верх карточки... надо обрезать
         * по низу карточки и всё" + скриншот с обведённой белой полосой) -
         * тот же {@code y0+4}, что уже компенсируем в {@code StardewLoadWorldScreen.init()}
         * ({@code innerTop = panelY - 4}) для ПОЗИЦИИ первой строки - но
         * ванильный {@code enableScissor} использует "сырой" {@code y0}
         * (БЕЗ +4), оставляя зазор в 4 GUI-юнита НАД настоящим верхом карточек.
         * В этом зазоре умещается и остаётся видимым "хвост" уже прокрученной
         * прошлой строки - её собственные нижняя граница + сепаратор (после
         * скролла ровно на 1 карточку они заканчиваются точно на границе
         * зазора, а не раньше). Тот же самый паттерн фикса уже есть в
         * ВАНИЛЬНОМ {@code SocialInteractionsPlayerList.enableScissor}
         * ({@code enableScissor(x0, y0 + 4, x1, y1)}) - не самодеятельность.
         */
        @Override
        protected void enableScissor(GuiGraphics graphics) {
            graphics.enableScissor(this.x0, this.y0 + 4, this.x1, this.y1);
        }

        /** Доступ к protected {@code getRowTop} для {@link WorldEntry#mouseClicked} (мёртвая зона шва). */
        int rowTop(int index) {
            return this.getRowTop(index);
        }

        /** Доступ к protected {@code y1} - нижняя граница ВИДИМОЙ области списка, см. {@link WorldEntry#render} про подавление сепаратора у последней видимой строки. */
        int visibleBottom() {
            return this.y1;
        }

        /**
         * Свой драг бегунка мышью (см. чат: "чтоб НЕ УБЕГАЛ наш скролл от
         * курсора" + "во время скрола" - белая полоса/чёрная линия карточки
         * съезжает) - ДВЕ отдельные проблемы решаются одним и тем же
         * механизмом:
         * 1) ванильный {@code mouseDragged} считает скорость по ФОРМУЛЕ ДЛЯ
         *    ВАНИЛЬНОГО (пропорционального размеру контента) бегунка, а наш -
         *    фиксированного размера ({@link #SCROLL_THUMB_H}) - отсюда
         *    рассинхрон (курсор двигается на 1px, бегунок - на другое
         *    количество px);
         * 2) ванильный драг - непрерывный по пикселям, а НЕ по целым строкам
         *    (см. тот же баг, что уже чинили в {@link #mouseScrolled} для
         *    колеса - "верхняя карточка наполовину прокручена, её граница
         *    обрезается scissor'ом"). Драг раньше это НЕ чинил (только
         *    колесо/стрелки) - отсюда артефакт именно "во время" перетаскивания.
         * Оба фикса - через {@link #scrollByRow}: копим пройденный курсором
         * путь в {@link #dragPixelAccumulator} и коммитим ЦЕЛЫМИ строками,
         * как только накопится "цена" одной строки в пикселях - и скорость
         * 1:1, и позиция всегда кратна itemHeight, разом. Перекрыть ТОЛЬКО
         * скорость (оставив ванильный click/drag) нельзя - {@code updateScrollingState}
         * мутирует {@code private boolean scrolling}, недоступное даже из
         * переопределения, поэтому клик и драг по всей колонке рельса
         * перехватываются здесь целиком.
         */
        private boolean draggingThumb = false;
        private double dragPixelAccumulator = 0;

        private boolean isOverRailColumn(double mouseX, double mouseY) {
            int x = getScrollbarPosition();
            return mouseX >= x && mouseX < x + StardewLoadWorldScreen.SCROLL_THUMB_W
                    && mouseY >= this.y0 && mouseY < this.y1;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && getMaxScroll() > 0 && isOverRailColumn(mouseX, mouseY)) {
                this.draggingThumb = true;
                this.dragPixelAccumulator = 0;
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (this.draggingThumb) {
                int maxScroll = getMaxScroll();
                int travel = (this.y1 - this.y0) - StardewLoadWorldScreen.SCROLL_THUMB_H;
                int numRows = Math.max(1, maxScroll / this.itemHeight);
                if (maxScroll > 0 && travel > 0) {
                    double pixelsPerRow = travel / (double) numRows;
                    this.dragPixelAccumulator += dragY;
                    while (this.dragPixelAccumulator >= pixelsPerRow) {
                        scrollByRow(1);
                        this.dragPixelAccumulator -= pixelsPerRow;
                    }
                    while (this.dragPixelAccumulator <= -pixelsPerRow) {
                        scrollByRow(-1);
                        this.dragPixelAccumulator += pixelsPerRow;
                    }
                }
                return true;
            }
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (this.draggingThumb) {
                this.draggingThumb = false;
                this.dragPixelAccumulator = 0;
                return true;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }

        /**
         * ЦЕЛЫМИ строками, НЕ ванильной непрерывной прокруткой по пикселям
         * (см. чат: "звук шшш при скроле как в стардью" + "сверху внутренняя
         * тёмная рамка при скроле вниз пропадает"). Ваниль крутит на
         * произвольное количество пикселей ({@code itemHeight/2} за "тик"
         * колеса), из-за чего scrollAmount почти никогда не кратен
         * itemHeight - верхняя видимая карточка оказывается НАПОЛОВИНУ
         * прокрученной, и её верхняя граница (FRAME_OUTER-полоса у самого
         * top) обрезается scissor'ом списка (см. {@code AbstractSelectionList.enableScissor}),
         * визуально "пропадая". У настоящей Stardew Valley
         * ({@code LoadGameMenu.currentItemIndex}) прокрутка ВООБЩЕ целочисленная
         * по индексу строки - карточка либо показана целиком, либо не
         * показана вообще, обрезки середины строки не бывает В ПРИНЦИПЕ.
         * Переиспользуем {@link #scrollByRow} вместо ванильной формулы -
         * тот же эффект и для звука тоже.
         */
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (delta > 0) {
                scrollByRow(-1);
            } else if (delta < 0) {
                scrollByRow(1);
            }
            return true;
        }

        /** Клик по стрелке скроллбара (см. {@link ScrollArrowButton}) - на одну строку, тот же звук, что у колеса мыши. */
        void scrollByRow(int direction) {
            double before = this.getScrollAmount();
            this.setScrollAmount(before + direction * (double) this.itemHeight);
            playScrollSoundIfChanged(before);
        }

        private void playScrollSoundIfChanged(double before) {
            if (this.getScrollAmount() != before) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.MENU_SCROLL.get(), 1.0F));
            }
        }

        void reloadEntries(List<LevelSummary> summaries) {
            this.clearEntries();
            for (int i = 0; i < summaries.size(); i++) {
                LevelSummary summary = summaries.get(i);
                if (summary.isDisabled()) continue;
                this.addEntry(new WorldEntry(this, summary, i + 1));
            }
        }
    }

    /**
     * Стрелка скроллбара (см. javadoc класса) - одна текстура, без
     * hover-кадра (в оригинале тоже нет отдельного hover-спрайта у стрелок,
     * только {@code tryHover} масштаб) - растёт на {@link HoverScale} при
     * наведении, звук клика ОТКЛЮЧЁН (свой "shwip" уже играет
     * {@link WorldList#scrollByRow}, дублировать ванильный клик не нужно).
     */
    private static class ScrollArrowButton extends Button {

        private final ResourceLocation texture;

        ScrollArrowButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.texture = texture;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            HoverScale.Rect rect = HoverScale.scaled(getX(), getY(), getWidth(), getHeight(), this.isHovered ? 1.1f : 1.0f);
            // БЕЗ ручного tint'а (см. чат: "не надо делать полупрозрачными
            // стрелки... не надо полупрозрачности") - у самой текстуры уже
            // есть частично прозрачные пиксели на сглаженных краях, обычный
            // blit их и так честно смешивает через альфа-канал.
            graphics.blit(texture, rect.x(), rect.y(), rect.width(), rect.height(),
                    0, 0, SCROLL_ARROW_NATIVE_W, SCROLL_ARROW_NATIVE_H, SCROLL_ARROW_NATIVE_W, SCROLL_ARROW_NATIVE_H);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            // Намеренно тихо - см. javadoc класса.
        }
    }

    private static class WorldEntry extends ObjectSelectionList.Entry<WorldEntry> implements AutoCloseable {

        private final WorldList list;
        private final LevelSummary summary;
        private final ResourceLocation portraitLocation;
        private final DynamicTexture portraitTexture;
        private final int number;

        private final String characterName;
        private final String playTimeText;
        private final String gameDateText;
        private final long money;
        /** Звук при наведении на карточку мира (см. чат: "звук при наведении на мир как в стардью") - тот же приём/звук, что у кнопок главного меню, см. {@link HoverSound}. */
        private boolean wasHovered = false;

        WorldEntry(WorldList list, LevelSummary summary, int number) {
            this.list = list;
            this.summary = summary;
            this.number = number;
            this.money = readMoney(summary);
            this.characterName = readCharacterName(summary);
            this.playTimeText = readPlayTime(summary);
            this.gameDateText = readGameDate(summary);

            // Портрет по пояс (см. чат), свой, не 64×64-иконка мира - см.
            // CardPortraitComposer. Своя текстура на запись, ключ - как у
            // ванильного FaviconTexture.forWorld: levelId (папка мира) может
            // содержать заглавные буквы/пробелы/что угодно - НЕЛЬЗЯ класть его
            // в ResourceLocation как есть (тот требует строго lowercase путь,
            // иначе бросает ResourceLocationException) - раньше именно это и
            // ломало ВЕСЬ список карточек целиком (исключение в конструкторе
            // WorldEntry рушило reloadEntries на любом мире с "нехорошим"
            // именем папки, см. чат: "карточки вообще не показываются").
            // Санитизируем + добавляем hash (на случай двух разных имён,
            // санитизирующихся в одну и ту же строку).
            String safeName = Util.sanitizeName(summary.getLevelId(), ResourceLocation::validPathChar);
            String hash = Hashing.sha1().hashUnencodedChars(summary.getLevelId()).toString();
            this.portraitLocation = new ResourceLocation(StardewMod.MODID, "card_portrait/" + safeName + "/" + hash);
            CharacterProfile profile = readFullProfile(summary);
            this.portraitTexture = new DynamicTexture(CardPortraitComposer.compose(profile));
            Minecraft.getInstance().getTextureManager().register(portraitLocation, portraitTexture);
        }

        /**
         * Текст у иконки — это ИМЯ ПЕРСОНАЖА (фермера), а не мира (см. чат) -
         * настоящее имя лежит в {@code playerdata/<uuid>.dat} ЗАКРЫТОГО мира,
         * в NBT нашей capability ({@code CharacterProfileProvider}, путь
         * {@code ForgeCaps -> stardew:character_profile -> Identity -> Name} -
         * см. {@code CharacterProfile#saveNBT}). Синглплеер => UUID игрока
         * тут всегда текущий залогиненный аккаунт. Если файла/тега ещё нет
         * (мир не открывался ни разу) - fallback на имя мира, чтобы строка не
         * оставалась пустой.
         */
        private static String readCharacterName(LevelSummary summary) {
            UUID uuid = Minecraft.getInstance().getUser().getGameProfile().getId();
            Path playerFile = Minecraft.getInstance().getLevelSource().getBaseDir()
                    .resolve(summary.getLevelId()).resolve("playerdata").resolve(uuid + ".dat");
            if (!Files.isRegularFile(playerFile)) return summary.getLevelName();
            try {
                CompoundTag root = NbtIo.readCompressed(playerFile.toFile());
                String name = root.getCompound("ForgeCaps").getCompound("stardew:character_profile").getCompound("Identity").getString("Name");
                return name.isBlank() ? summary.getLevelName() : name;
            } catch (IOException e) {
                LOGGER.warn("[Stardew] Failed to read character name from {}", playerFile, e);
                return summary.getLevelName();
            }
        }

        /**
         * Полный профиль внешности (не только имя, см. {@link #readCharacterName})
         * - для {@link CardPortraitComposer}, который сам заново
         * прогоняет {@code SkinComposer.compose} по нему, ровно как рисуется
         * настоящий скин игрока. Та же капа/файл, что и у имени - {@code
         * CharacterProfile.loadNBT} сам разбирает Identity/Appearance/World.
         */
        private static CharacterProfile readFullProfile(LevelSummary summary) {
            UUID uuid = Minecraft.getInstance().getUser().getGameProfile().getId();
            Path playerFile = Minecraft.getInstance().getLevelSource().getBaseDir()
                    .resolve(summary.getLevelId()).resolve("playerdata").resolve(uuid + ".dat");
            if (!Files.isRegularFile(playerFile)) return new CharacterProfile();
            try {
                CompoundTag root = NbtIo.readCompressed(playerFile.toFile());
                CompoundTag profileTag = root.getCompound("ForgeCaps").getCompound("stardew:character_profile");
                return CharacterProfile.loadNBT(profileTag);
            } catch (IOException e) {
                LOGGER.warn("[Stardew] Failed to read character profile from {}", playerFile, e);
                return new CharacterProfile();
            }
        }

        /**
         * Деньги - та же схема чтения, что {@link #readCharacterName}, другая
         * капа: {@code ForgeCaps -> stardew:player_state -> Money} (см.
         * {@code PlayerProvider}/{@code PlayerStardewState} - ключ капы
         * "player_state" задаётся в {@code CapabilityEvents.addCapability}).
         */
        private static long readMoney(LevelSummary summary) {
            UUID uuid = Minecraft.getInstance().getUser().getGameProfile().getId();
            Path playerFile = Minecraft.getInstance().getLevelSource().getBaseDir()
                    .resolve(summary.getLevelId()).resolve("playerdata").resolve(uuid + ".dat");
            if (!Files.isRegularFile(playerFile)) return 0L;
            try {
                CompoundTag root = NbtIo.readCompressed(playerFile.toFile());
                return root.getCompound("ForgeCaps").getCompound("stardew:player_state").getLong("Money");
            } catch (IOException e) {
                LOGGER.warn("[Stardew] Failed to read money from {}", playerFile, e);
                return 0L;
            }
        }

        /**
         * "Таймер" — статистика наигранного времени (не какой-то обратный
         * отсчёт), см. чат. Ваниль уже сама её считает и хранит для КАЖДОГО
         * мира/игрока в {@code stats/<uuid>.json} ({@code minecraft:custom ->
         * minecraft:play_time}, тики - 20/сек) - читать НАШУ capability тут
         * не нужно вообще, значение уже готовое. Формат "часы:минуты" (не
         * минуты:секунды) - так его показывает сама Stardew Valley (ср.
         * "66:26" на давно играемой ферме на референс-скриншоте).
         */
        private static String readPlayTime(LevelSummary summary) {
            UUID uuid = Minecraft.getInstance().getUser().getGameProfile().getId();
            Path statsFile = Minecraft.getInstance().getLevelSource().getBaseDir()
                    .resolve(summary.getLevelId()).resolve("stats").resolve(uuid + ".json");
            if (!Files.isRegularFile(statsFile)) return "0:00";
            try (Reader reader = Files.newBufferedReader(statsFile)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject custom = root.getAsJsonObject("stats").getAsJsonObject("minecraft:custom");
                long ticks = custom.has("minecraft:play_time") ? custom.get("minecraft:play_time").getAsLong() : 0L;
                long totalMinutes = ticks / 20 / 60;
                return (totalMinutes / 60) + ":" + String.format("%02d", totalMinutes % 60);
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("[Stardew] Failed to read play time from {}", statsFile, e);
                return "0:00";
            }
        }

        /**
         * Дата захода — игровая ("Day n of Season, Year n"), не реальная дата
         * последнего запуска (см. чат). Читается из {@code StardewDateData}
         * ЗАКРЫТОГО мира ({@code data/stardew_date.dat}, тот же формат
         * SavedData - корень->"data"->поля, что и у {@code StardewWorldMarker},
         * см. {@code WorldTemplateManager.seedWorldMarker}). Год считается из
         * {@code totalDays} (28 дней/сезон, 4 сезона/год - см. {@code Season}
         * и {@code StardewDateData.syncTotalDaysFromDate}).
         */
        private static String readGameDate(LevelSummary summary) {
            Path dateFile = Minecraft.getInstance().getLevelSource().getBaseDir()
                    .resolve(summary.getLevelId()).resolve("data").resolve("stardew_date.dat");
            if (!Files.isRegularFile(dateFile)) return "Day 1 of Spring, Year 1";
            try {
                CompoundTag data = NbtIo.readCompressed(dateFile.toFile()).getCompound("data");
                String season = data.getString("season");
                int day = data.getInt("day");
                int totalDays = data.getInt("totalDays");
                int year = totalDays / (28 * 4) + 1;
                String seasonDisplay = season.isEmpty() ? "Spring"
                        : season.charAt(0) + season.substring(1).toLowerCase();
                return "Day " + day + " of " + seasonDisplay + ", Year " + year;
            } catch (IOException e) {
                LOGGER.warn("[Stardew] Failed to read game date from {}", dateFile, e);
                return "Day 1 of Spring, Year 1";
            }
        }

        @Override
        public Component getNarration() {
            return Component.literal(summary.getLevelName());
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            // Переданный height игнорируется НАМЕРЕННО: ваниль отдаёт сюда
            // itemHeight-4 и оставляет между строками 4px "своего" зазора, в
            // котором просвечивал bgColor панели (см. чат: "после него есть
            // ещё и текстура какая-то фона"). Рисуем карточку на ПОЛНЫЙ шаг
            // строки (ENTRY_HEIGHT, минус шов) - следующая строка начинается
            // ровно через ENTRY_HEIGHT, так что карточки стоят впритык.
            //
            // lastVisible - см. чат со скриншотом ("не должно быть видно
            // верха другой карточки + сепаратора"): последняя ВИДИМАЯ строка
            // (нижний край её слота достиг нижней границы списка) - это НЕ
            // обязательно последняя запись В ДАННЫХ (miров может быть
            // больше 5) - но сепаратор всё равно указывал бы на карточку,
            // которая физически не показана (проскроллена вниз), и торчал
            // прямо перед рамкой панели. Подавляем сепаратор в ОБОИХ
            // случаях - симметрично тому, как верхняя видимая строка уже не
            // показывает хвост скрытой сверху (см. enableScissor).
            boolean lastEntry = index == this.list.children().size() - 1;
            boolean lastVisible = top + ENTRY_HEIGHT >= this.list.visibleBottom();
            int cardBottom = top + ENTRY_HEIGHT - (lastEntry || lastVisible ? 0 : SEPARATOR_THICKNESS);
            // "Настоящий" hover - НЕ шов между карточками (mouseY < cardBottom,
            // та же граница, что у подсветки ниже) - звук не должен играть на
            // мёртвой зоне шва. Свой cue, ДРУГОЙ, чем у кнопок меню (см.
            // ModSounds.MENU_HOVER_WORLD - LoadGameMenu использует "Cowboy_gunshot", не "Cowboy_Footstep").
            boolean reallyHovered = hovered && mouseY < cardBottom;
            wasHovered = HoverSound.update(reallyHovered, wasHovered, ModSounds.MENU_HOVER_WORLD);

            // Обрамление - три ВЛОЖЕННЫХ прямоугольника (см. javadoc FRAME_OUTER).
            // Цвета ПОДМЕНЯЮТСЯ целиком при наведении (см. чат: "смена цвета
            // карточки ПРИ наведении, а не статика") - не полупрозрачный
            // оверлей поверх статичного цвета, как было раньше.
            int frameInner = reallyHovered ? HOVER_FRAME_INNER : FRAME_INNER;
            int cardBg = reallyHovered ? HOVER_CARD_BG : COLOR_CARD_BG;
            graphics.fill(left, top, left + width, cardBottom, frameInner);
            graphics.fill(left + BAND_THICKNESS, top + BAND_THICKNESS, left + width - BAND_THICKNESS, cardBottom - BAND_THICKNESS, FRAME_OUTER);
            int contentLeft = left + CARD_BORDER;
            int contentTop = top + CARD_BORDER;
            int contentRight = left + width - CARD_BORDER;
            int contentBottom = cardBottom - CARD_BORDER;
            graphics.fill(contentLeft, contentTop, contentRight, contentBottom, cardBg);

            // Шов МЕЖДУ этой и следующей карточкой - только если сосед снизу
            // ВИДИМ (см. javadoc SEPARATOR_THICKNESS и lastVisible выше).
            if (!lastEntry && !lastVisible) {
                graphics.fill(left, cardBottom, left + width, cardBottom + SEPARATOR_THICKNESS, SEPARATOR);
            }

            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int contentHeight = contentBottom - contentTop;

            Component numberText = Component.literal(number + ".").withStyle(ChatFormatting.BOLD);
            int[] numberPos = {contentLeft + 2, contentTop + (contentHeight - font.lineHeight) / 2};
            drawShadowed(graphics, font, numberText, numberPos[0], numberPos[1], COLOR_TEXT_NAME, SHADOW_NAME);

            // Портрет по пояс (см. чат: "чтоб не было такого лютого
            // искажения" - у бюста сжатие даёт целый масштаб). СТРОГО 1:1 - иконка
            // уже сжата до финального размера внутри CardPortraitComposer
            // (см. его javadoc: "не надо сразу в масштабе иконки рисовать"),
            // никакого дополнительного масштабирования при отрисовке.
            int portraitBaseX = contentLeft + 12;
            int portraitBaseY = contentTop + (contentHeight - CardPortraitComposer.HEIGHT) / 2;
            int[] portraitPos = {portraitBaseX, portraitBaseY};
            graphics.blit(portraitLocation, portraitPos[0], portraitPos[1], 0, 0,
                    CardPortraitComposer.WIDTH, CardPortraitComposer.HEIGHT,
                    CardPortraitComposer.WIDTH, CardPortraitComposer.HEIGHT);

            // textX/rowRight - от БАЗОВЫХ (без debug-смещения) позиций порт-
            // рета/удаления, иначе перетаскивание одного элемента через F8
            // сдвигало бы ещё и точку отсчёта для всех остальных.
            int textX = portraitBaseX + CardPortraitComposer.WIDTH + 8;
            int rowRight = contentRight - DELETE_HITBOX_SIZE - 10;

            // Слева - имя ПЕРСОНАЖА (фермера), справа - название МИРА/фермы
            // (см. javadoc readCharacterName). ОБЕ строки правого столбца
            // выровнены по ПРАВОМУ краю (rowRight), а не по общему левому
            // (см. чат: "давай лучше по правому центрировать") - каждая сама
            // считает свою ширину и стартует с {@code rowRight - width}.
            String farmText = summary.getLevelName() + " Farm";
            Component characterNameText = Component.literal(characterName).withStyle(ChatFormatting.BOLD);
            int[] namePos = {textX + 2, contentTop + 14};
            drawShadowed(graphics, font, characterNameText, namePos[0], namePos[1], COLOR_TEXT_NAME, SHADOW_NAME);

            int[] farmPos = {rowRight - font.width(farmText), contentTop + 14};
            drawShadowed(graphics, font, Component.literal(farmText), farmPos[0], farmPos[1], COLOR_TEXT_MUTED, SHADOW_MUTED);

            int[] datePos = {textX + 1, contentTop + 32};
            drawShadowed(graphics, font, Component.literal(gameDateText), datePos[0], datePos[1], COLOR_TEXT_MUTED, SHADOW_MUTED);

            // Часы -> деньги, слева направо (см. чат: "монетку перед
            // балансом, а часы перед [временем]" + "расположение денег
            // зависит от длины часов игры") - блок целиком выровнен по
            // ПРАВОМУ краю: сперва считается его полная ширина, потом старт
            // отсчитывается от rowRight назад (см. чат: "по правому
            // центрировать"), а не строится слева направо с фиксированным x.
            // Иконки (ICON_SIZE=16) заметно выше строки текста (9px) -
            // смещение "-3" центрирует их по тексту.
            int rowY = contentTop + 32;
            String moneyText = money + "g";
            int rowWidth = ICON_SIZE + 2 + font.width(playTimeText) + 6 + ICON_SIZE + 2 + font.width(moneyText);
            int rowStartX = rowRight - rowWidth;

            int[] clockPos = {rowStartX, rowY - 3};
            graphics.blit(CLOCK_ICON, clockPos[0], clockPos[1], ICON_SIZE, ICON_SIZE, 0, 0, ICON_NATIVE, ICON_NATIVE, ICON_NATIVE, ICON_NATIVE);

            int[] timePos = {clockPos[0] + ICON_SIZE + 2, rowY};
            drawShadowed(graphics, font, Component.literal(playTimeText), timePos[0], timePos[1], COLOR_TEXT_MUTED, SHADOW_MUTED);

            int[] coinPos = {timePos[0] + font.width(playTimeText) + 6, rowY - 3};
            graphics.blit(COIN_ICON, coinPos[0], coinPos[1], ICON_SIZE, ICON_SIZE, 0, 0, ICON_NATIVE, ICON_NATIVE, ICON_NATIVE, ICON_NATIVE);

            int[] moneyPos = {coinPos[0] + ICON_SIZE + 2, rowY};
            drawShadowed(graphics, font, Component.literal(moneyText), moneyPos[0], moneyPos[1], COLOR_TEXT_MUTED, SHADOW_MUTED);

            // Отступ от contentRight/contentTop РАВНЫЙ (7px) с обеих сторон -
            // см. чат: "равноудалить от краёв" (про кнопку удаления, не
            // "назад" - было 2px справа/0px сверху, визуально несимметрично).
            // Реальная текстура (см. чат: "добавь иконку delete... размеры
            // должны быть одинаковы") вместо красного квадрата с "x".
            int[] deletePos = {contentRight - ICON_SIZE - 7, contentTop + 7};
            graphics.blit(DELETE_ICON, deletePos[0], deletePos[1], ICON_SIZE, ICON_SIZE, 0, 0, ICON_NATIVE, ICON_NATIVE, ICON_NATIVE, ICON_NATIVE);
        }


        /** Кастомный цвет тени (у ванильного drawString тень - фиксированное автозатемнение цвета, тут нужен НЕЗАВИСИМЫЙ цвет, см. чат) - рисуем тень и текст вручную, оба с dropShadow=false. */
        private static void drawShadowed(GuiGraphics graphics, Font font, Component text, int x, int y, int color, int shadowColor) {
            graphics.drawString(font, text, x + 1, y + 1, shadowColor, false);
            graphics.drawString(font, text, x, y, color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Шов между карточками - НЕ часть карточки, клики по нему мёртвые
            // (см. чат: "должен быть некликабельным"). Ванильный hit-test
            // относит эти пиксели к строке ВЕРХНЕЙ карточки - отсекаем тут.
            int index = this.list.children().indexOf(this);
            boolean lastEntry = index == this.list.children().size() - 1;
            int rowTop = this.list.rowTop(index);
            boolean lastVisible = rowTop + ENTRY_HEIGHT >= this.list.visibleBottom();
            if (!lastEntry && !lastVisible && mouseY >= rowTop + ENTRY_HEIGHT - SEPARATOR_THICKNESS) {
                return false;
            }

            // Зона клика - ТА ЖЕ формула, что позиция иконки в render() (см.
            // чат: "кнопка удаления не полностью работает") - раньше тут была
            // независимая формула через DELETE_HITBOX_SIZE от края всей
            // строки, которая рассинхронизировалась с иконкой после
            // нескольких правок её размера/позиции: реально кликабельной
            // была только часть иконки, остальное считалось кликом "войти в
            // мир".
            int contentRight = this.list.getRowLeft() + this.list.getRowWidth() - CARD_BORDER;
            int deleteX = contentRight - ICON_SIZE - 7;
            if (mouseX >= deleteX) {
                confirmDelete();
            } else {
                joinWorld();
            }
            return true;
        }

        private void joinWorld() {
            Minecraft minecraft = Minecraft.getInstance();
            Screen loadScreen = minecraft.screen;
            // Клик по валидной карточке - LoadGameMenu.receiveLeftClick: Game1.playSound("select").
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.MENU_SELECT.get(), 1.0F));
            // Реальный выход в геймплей - тут возвращаем guiScale игрока (см. javadoc StardewLoadWorldScreen/TitleScreenReplacer).
            if (minecraft.options.guiScale().get() != this.list.previousGuiScale) {
                minecraft.options.guiScale().set(this.list.previousGuiScale);
                minecraft.resizeDisplay();
            }
            minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
            minecraft.createWorldOpenFlows().loadLevel(loadScreen, summary.getLevelId());
        }

        private void confirmDelete() {
            Minecraft minecraft = Minecraft.getInstance();
            Screen current = minecraft.screen;
            // Открытие подтверждения - LoadGameMenu.receiveLeftClick (клик по
            // иконке удаления): Game1.playSound("drumkit6") - тот же звук,
            // что уже зарегистрирован для character creation (см. CC_DRUMKIT_6).
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.CC_DRUMKIT_6.get(), 1.0F));
            minecraft.setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    // Подтверждение - LoadGameMenu: Game1.playSound("trashcan").
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.MENU_TRASHCAN.get(), 1.0F));
                    try (LevelStorageSource.LevelStorageAccess access = minecraft.getLevelSource().createAccess(summary.getLevelId())) {
                        access.deleteLevel();
                    } catch (IOException e) {
                        LOGGER.error("[Stardew] Failed to delete world {}", summary.getLevelId(), e);
                    }
                    if (current instanceof StardewLoadWorldScreen loadScreen) {
                        loadScreen.reload();
                    }
                } else {
                    // Отмена - LoadGameMenu.receiveLeftClick (cancelDeleteButton): Game1.playSound("smallSelect").
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.MENU_CANCEL.get(), 1.0F));
                }
                minecraft.setScreen(current);
            }, Component.translatable("selectWorld.deleteQuestion"),
                    Component.translatable("selectWorld.deleteWarning", summary.getLevelName()),
                    Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
        }

        @Override
        public void close() {
            this.portraitTexture.close();
        }
    }
}

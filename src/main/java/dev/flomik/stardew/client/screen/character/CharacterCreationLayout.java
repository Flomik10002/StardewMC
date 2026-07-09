package dev.flomik.stardew.client.screen.character;

/**
 * Virtual-canvas layout (ТЗ §7): все координаты заданы в фиксированном
 * виртуальном холсте {@link #VIRTUAL_WIDTH}x{@link #VIRTUAL_HEIGHT} (1920×1080,
 * подобраны под референс расстановки), который целиком масштабируется и
 * центрируется (letterbox) под фактический {@code width}/{@code height}
 * экрана через {@link #scale}/{@link #offsetX}/{@link #offsetY}.
 *
 * Ключевое отличие от матричного scale ({@code PoseStack.scale()}), которого
 * сознательно избегали раньше: здесь КАЖДЫЙ widget (Button/EditBox/Slider)
 * получает уже готовые РЕАЛЬНЫЕ пиксельные координаты через {@link #real(int, int, int, int)}
 * при создании — клики не могут рассинхронизироваться с рендером.
 *
 * Отступы здесь заметно щедрее, чем в самой первой попытке 1:1 повторить
 * референс: {@code this.width}/{@code this.height} — это уже ЛОГИЧЕСКИЕ
 * GUI-Scale координаты (при {@code guiScale=2} на 1280×720 логический канвас
 * всего 640×360, т.е. {@link #scale} тут ~0.33, а не ~0.67, как можно было бы
 * подумать по физическим пикселям окна). Текст же у Minecraft рисуется
 * ФИКСИРОВАННЫМ пиксельным размером шрифта, который вместе с холстом не
 * уменьшается — поэтому зазоры между подписями и полями посчитаны с запасом
 * под низкий scale, а не впритык под референс.
 */
final class CharacterCreationLayout {

    static final int VIRTUAL_WIDTH = 1920;
    static final int VIRTUAL_HEIGHT = 1200;

    // --- Панели ---
    static final int PANEL_X = 560, PANEL_Y = 90, PANEL_W = 800, PANEL_H = 950;
    /**
     * H обрезан под контент (6 карточек + зазоры + отступы) - было 950,
     * оставалось много пустого места сверху/снизу. X сдвинут вправо на 40
     * (было 1390). Y центрирует панель по высоте ГЛАВНОЙ панели (PANEL_Y/PANEL_H),
     * а не совпадает с её верхним краем, как раньше.
     */
    static final int FARM_PANEL_X = 1430, FARM_PANEL_W = 260, FARM_PANEL_H = 820;
    static final int FARM_PANEL_Y = PANEL_Y + (PANEL_H - FARM_PANEL_H) / 2;

    // --- Верхние декоративные кнопки ---
    /** W/H увеличены на 16 (было 64) - попросили. */
    static final int DICE_X = 565, DICE_Y = 115, DICE_W = 80, DICE_H = 80;
    /** Y подвинут вверх на ~46 (было 1060) - подогнано мышью через F8-дебаг-инструмент. */
    static final int WRENCH_X = 460, WRENCH_Y = 1014, WRENCH_W = 55, WRENCH_H = 55;
    // BACK_X/Y/W/H убраны - Back теперь TitleBackButton.bottomRight(this.width, this.height, ...),
    // привязан напрямую к экрану (см. чат "должны быть одинаковы"), а не к virtual-canvas.

    // --- Превью персонажа ---
    /**
     * W/H под пропорции character_backgrounds.png (128×192 на кадр = 2:3) -
     * иначе текстура растягивается с искажением. Масштаб ×1.5 (было 140×210) -
     * персонаж масштабируется пропорционально коробке в {@code renderLivePreview}
     * ({@code scale = min(boxW,boxH)/2}), так что увеличение коробки
     * автоматически увеличивает и картинку, и модель персонажа вместе. X
     * центрирован над рядом male/female (центр ряда =
     * (GENDER_MALE_X + GENDER_FEMALE_X + GENDER_SIZE)/2 = 747, PREVIEW_X = 747 - PREVIEW_W/2).
     */
    static final int PREVIEW_X = 642, PREVIEW_Y = 125, PREVIEW_W = 210, PREVIEW_H = 315;

    // --- Identity-поля (Name / Farm Name / Favorite Thing) ---
    /** X подвинут ещё левее на ~13 (было 1050 -> 1032 -> 1019) - подогнано мышью через F8-дебаг-инструмент. */
    static final int FIELD_INPUT_X = 1019, FIELD_INPUT_W = 240, FIELD_INPUT_H = 32;
    /**
     * Левый край подписи слева от поля (центрирование к левому краю, не по
     * центру зоны) - ЗАФИКСИРОВАН литералом (не формула от {@link #FIELD_INPUT_X}):
     * при подгонке мышью подвинули только сами поля ввода, подписи
     * "Name"/"Farm Name"/"Favorite Thing" остались на месте - если бы позиция
     * считалась от FIELD_INPUT_X, подписи сдвинулись бы следом непреднамеренно.
     * Сдвинуто ещё левее на ~23 (было 950 -> 900 -> 877) по новому дампу.
     */
    static final int FIELD_LABEL_LEFT_X = 877;
    static final int NAME_Y = 130;
    /** Farm Name / Favorite Thing переносятся на 2 строки (как на референсе) - иначе не помещаются по ширине при низком scale. */
    static final int FARM_NAME_Y = 205;
    static final int FAVORITE_THING_Y = 280;
    static final int FIELD_LABEL_LINE_GAP = 26;

    // --- Пол персонажа (male/female) ---
    // Подогнано мышью через F8-дебаг-инструмент: строка опущена ниже (была
    // Y=360, потом 400) и иконки раздвинуты шире друг от друга (было 70 между ними).
    static final int GENDER_Y = 460, GENDER_SIZE = 82;
    static final int GENDER_MALE_X = 650, GENDER_FEMALE_X = 762;

    /**
     * Animal Preference — подпись отдельной строкой НАД стрелками/иконкой, а
     * не сбоку: в один ряд с длинной подписью "Animal Preference" стрелки не
     * помещались (см. чат) — заезжали друг на друга при низком scale.
     *
     * Иконка питомца больше НЕ завязана на {@link #GENDER_SIZE} - своя
     * отдельная константа, чуть меньше кнопок пола (попросили "чуть меньше").
     *
     * Весь блок перенесён гораздо правее и сильно раздвинут (подогнано мышью
     * через F8-дебаг-инструмент) - было {@code ANIMAL_ARROW_LEFT_X=850},
     * зазор между стрелкой/иконкой был по 10.
     */
    static final int ANIMAL_LABEL_X = 850, ANIMAL_LABEL_Y = 360;
    static final int ANIMAL_CONTROL_Y = 400;
    /**
     * Возвращён исходный размер (46 было ошибкой - "сузить" значило про
     * зазор, не про размер кнопок), затем увеличен на 12 (было 56), тем же
     * приращением, что и {@link #ARROW_SIZE} у Skin/Hair/.../Acc - попросили
     * увеличить стрелки и тут тоже, не только там.
     */
    static final int ANIMAL_ARROW_SIZE = 68;
    /** Чуть меньше кнопок пола (было = GENDER_SIZE=82). */
    static final int ANIMAL_ICON_SIZE = 68;
    static final int ANIMAL_ARROW_LEFT_X = 1036;
    /** Зазор до морды питомца уменьшен (было 50) - стрелки должны стоять БЛИЖЕ к питомцу, не быть меньше размером. */
    static final int ANIMAL_ARROW_GAP = 30;
    static final int ANIMAL_ICON_X = ANIMAL_ARROW_LEFT_X + ANIMAL_ARROW_SIZE + ANIMAL_ARROW_GAP;
    static final int ANIMAL_ARROW_RIGHT_X = ANIMAL_ICON_X + ANIMAL_ICON_SIZE + ANIMAL_ARROW_GAP;

    // --- Skin/Hair/Shirt/Pants/Acc (левая колонка со стрелками) ---
    // Прибиты к ЛЕВОМУ краю панели (там было много свободного места) и до
    // упора вниз к OK. Подпись/номер центрируются в ФИКСИРОВАННОЙ зоне сразу
    // после левой стрелки ({@link #ARROW_LABEL_ZONE_WIDTH}), а не по центру
    // всего (широкого) ряда - иначе при растягивании ряда текст уезжает к
    // центру панели и сталкивается с подписью цветовой группы (была именно
    // такая проблема - оба текста "наслаивались" в середине панели).
    static final int ARROW_ROW_X = 590;
    /** Опущено ниже (было 500) - подогнано мышью через F8-дебаг-инструмент, вслед за гендер-рядом. */
    static final int ARROW_ROW_START_Y = 550;
    /** Слегка сжато (было 88) - иначе последний ряд (Acc.) упирается в кнопку OK при новом ARROW_ROW_START_Y. */
    static final int ARROW_ROW_HEIGHT = 84;
    /** Увеличено (было 64, попросили "чуть больше") - (ARROW_ROW_HEIGHT-ARROW_SIZE)/2=4 совпадает с отступом +4 в addCosmeticRow, поэтому кнопка остаётся вертикально центрированной в строке без правки самого addCosmeticRow. */
    static final int ARROW_SIZE = 76;
    static final int ARROW_LABEL_X = 665;
    static final int ARROW_LABEL_ZONE_WIDTH = 140;
    /**
     * Правая стрелка обязана закончиться ДО {@link #COLOR_SLIDER_X} - иначе
     * наезжает на колонку Color. Уменьшен с 180 до 154 - подогнано мышью
     * через F8-дебаг-инструмент (все 5 строк подвинуты чуть ближе к подписи).
     */
    static final int ARROW_RIGHT_GAP = 154;
    /** Подпись вровень с центром стрелки (не с верхним краем ряда) - подогнано мышью через F8-дебаг-инструмент. */
    static final int ARROW_LABEL_Y_OFFSET = 27;
    static final int ARROW_NUMBER_LINE_GAP = 67;

    // --- Eye/Hair/Pants Color (правая колонка, стопкой из 3 слайдеров) ---
    // Прибита к ПРАВОМУ краю панели (свободное место было и справа) - чем
    // дальше от стрелочной колонки, тем больше зазор между ними в центре
    // панели (было мало, и обе подписи наслаивались друг на друга).
    static final int COLOR_GROUP_START_Y = 470;
    static final int COLOR_GROUP_HEIGHT = 150;
    /** X подвинут вправо на ~35 (было 1070) - подогнано мышью через F8-дебаг-инструмент; число справа следует автоматически (считается от позиции слайдера). */
    static final int COLOR_SLIDER_X = 1105, COLOR_SLIDER_W = 190, COLOR_SLIDER_H = 22;
    static final int COLOR_SLIDER_ROW_GAP = 38;
    static final int COLOR_SLIDER_FIRST_ROW_OFFSET = 24;
    /**
     * Один текст на всю группу из 3 слайдеров (не на каждый слайдер отдельно:
     * "3 текста на 9 слайдеров") — слева от них, по центру среднего (S)
     * слайдера. Зафиксирован ЛИТЕРАЛОМ (а не формулой от {@link #COLOR_SLIDER_X}) -
     * при подгонке мышью подпись группы НЕ двигали, только сами слайдеры уехали
     * правее, так что зазор между подписью и слайдерами теперь больше, чем
     * раньше, и это осознанно.
     */
    static final int COLOR_GROUP_LABEL_ZONE_WIDTH = 100;
    static final int COLOR_GROUP_LABEL_CENTER_X = 1010;

    // --- Skip Intro / OK ---
    static final int SKIP_INTRO_X = 850, SKIP_INTRO_Y = 965, SKIP_INTRO_SIZE = 24;
    /** ok.png квадратный (64×64) - кнопка тоже квадратная, иначе IconButton растягивает её в овал. */
    static final int OK_SIZE = 72;
    /** X подвинут вправо на ~34 (было 1230) - подогнано мышью через F8-дебаг-инструмент. */
    static final int OK_X = 1264, OK_Y = 955, OK_W = OK_SIZE, OK_H = OK_SIZE;

    /**
     * Farm type карточки - 6 штук в левой колонке (с зазором {@link #FARM_CARD_GAP}
     * между ними, раньше было впритык без зазора - попросили добавить), 7-я
     * (Beach) отдельно справа. Высота у всех одна ({@link #FARM_CARD_HEIGHT} =
     * native 20px в farms.png), ширина считается индивидуально под РОДНУЮ
     * пропорцию каждого кадра (18 или 19 к 20) - см.
     * {@code CharacterCreationScreen#farmCardWidth} - пропорцию менять нельзя
     * (попросили явно). Блок центрирован по вертикали в {@link #FARM_PANEL_H}
     * (который сам обрезан под контент - см. его javadoc), поэтому
     * FARM_CARD_START_Y = FARM_PANEL_Y + (FARM_PANEL_H - contentHeight)/2, где
     * contentHeight = 6×HEIGHT + 5×GAP = 6×120+5×10 = 770.
     */
    /** Формула от FARM_PANEL_X (не литерал) - чтобы сдвиг панели автоматически тащил за собой карточки. */
    static final int FARM_CARD_LEFT_X = FARM_PANEL_X + 15;
    static final int FARM_CARD_HEIGHT = 120;
    static final int FARM_CARD_GAP = 10;
    static final int FARM_CARD_START_Y = FARM_PANEL_Y + (FARM_PANEL_H - (6 * FARM_CARD_HEIGHT + 5 * FARM_CARD_GAP)) / 2;
    static final int FARM_CARD_RIGHT_GAP = 12;

    final float scale;
    final int offsetX;
    final int offsetY;

    CharacterCreationLayout(int screenWidth, int screenHeight) {
        this.scale = Math.min((float) screenWidth / VIRTUAL_WIDTH, (float) screenHeight / VIRTUAL_HEIGHT);
        this.offsetX = Math.round((screenWidth - VIRTUAL_WIDTH * scale) / 2f);
        this.offsetY = Math.round((screenHeight - VIRTUAL_HEIGHT * scale) / 2f);
    }

    /** Переводит виртуальные координаты в реальные экранные пиксели — единая арифметика для рендера и hit-test'а. */
    int[] real(int vx, int vy, int vw, int vh) {
        int x = offsetX + Math.round(vx * scale);
        int y = offsetY + Math.round(vy * scale);
        int w = Math.round(vw * scale);
        int h = Math.round(vh * scale);
        return new int[]{x, y, w, h};
    }

    int realX(int vx) { return offsetX + Math.round(vx * scale); }
    int realY(int vy) { return offsetY + Math.round(vy * scale); }
    int realLen(int v) { return Math.round(v * scale); }
}

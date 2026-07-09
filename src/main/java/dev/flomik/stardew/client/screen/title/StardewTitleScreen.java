package dev.flomik.stardew.client.screen.title;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.CustomCursor;
import dev.flomik.stardew.client.TitleScreenReplacer;
import dev.flomik.stardew.client.character.worldtemplate.StardewPlayButtonHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Полная замена ванильного {@code TitleScreen} (подмена происходит в
 * {@code client.TitleScreenReplacer} через {@code ScreenEvent.Opening} - сам
 * этот класс НЕ наследует {@code TitleScreen}, специально, чтобы событие не
 * подменяло само себя рекурсивно) под макет New/Load/Co-op/Exit (см. чат,
 * референс-скриншот оригинальной Stardew Valley).
 *
 * Лого/кнопки — реальные ассеты ({@code textures/gui/title/title.png} 400×187,
 * {@code buttons.png}/{@code buttons_active.png} 4×74×58 см.
 * {@link TitleMainButton}) — размеры считаются от {@link #width}, но
 * {@code guiScale} форсирован в {@link TitleScreenReplacer#FORCED_GUI_SCALE}
 * (см. его javadoc - макет верстался под этот масштаб, "родной" guiScale
 * игрока давал бы либо блёклый нецелочисленный stretch пиксель-арта, либо
 * непропорционально огромный/мелкий интерфейс).
 */
public class StardewTitleScreen extends Screen {

    private static final ResourceLocation LOGO = new ResourceLocation(StardewMod.MODID, "textures/gui/title/title.png");
    private static final int LOGO_NATIVE_W = 400, LOGO_NATIVE_H = 187;
    private static final int BUTTON_NATIVE_W = 74, BUTTON_NATIVE_H = 58;

    private static final ResourceLocation OPTIONS_TEX = new ResourceLocation(StardewMod.MODID, "textures/gui/title/options.png");
    private static final int OPTIONS_NATIVE_W = 27, OPTIONS_NATIVE_H = 27;
    private static final ResourceLocation QUESTION_TEX = new ResourceLocation(StardewMod.MODID, "textures/gui/title/question.png");
    private static final int QUESTION_NATIVE_W = 22, QUESTION_NATIVE_H = 24;
    /**
     * Раньше обе кнопки насильно растягивались в общий квадрат 44×44,
     * искажая пропорции (question - 22×24, НЕ квадрат) - см. чат: "ОБЯЗАТЕЛЬНО
     * подгоняй все кнопки по ИХ реальному размеру". Теперь размер каждой
     * кнопки - её СОБСТВЕННЫЙ нативный width/height (никакого общего
     * квадратного бокса), масштабированный этим одним множителем - 1
     * (нативный пиксель в пиксель) уже вдвое меньше прежних 44 (у question
     * нативная ширина ровно 22 = 44/2, см. чат "сделай их меньше в 2 раза").
     */
    private static final int SMALL_BUTTON_SCALE = 1;
    private static final int SMALL_BUTTON_MARGIN = 20;
    private static final int SMALL_BUTTON_GAP = 8;

    /**
     * Уменьшено обратно (было 22) - в дампе F8 ({@code stardew_debug_layout_StardewTitleScreen.json})
     * реальный зазор между сдвинутыми пользователем кнопками оказался ~3-5px
     * (спейсинг 111 при ширине кнопки 108), а не 22 - "пространство подышать"
     * имелось в виду про отступы ОТ КРАЁВ ЭКРАНА (см. {@link #LAYOUT_SCALE}),
     * не про зазор между самими кнопками - как в референсе, они стоят почти
     * впритык, единой "доской".
     */
    private static final int BUTTON_GAP = 4;
    /** Итоговый макет уменьшен до 0.8 от максимального, что влезает - иначе логотип/кнопки упираются в края экрана без отступов. */
    private static final float LAYOUT_SCALE = 0.8f;

    /**
     * См. {@link TitleScreenReplacer} - тут только запоминаем, что было, и
     * прокидываем дальше (в {@code StardewLoadWorldScreen}, в pre-world
     * {@code CharacterCreationScreen} через {@code StardewPlayButtonHandler}) -
     * реальный откат на пользовательский guiScale происходит там, в момент
     * настоящего перехода в геймплей, а не на этом экране.
     */
    private final int previousGuiScale;

    public StardewTitleScreen(int previousGuiScale) {
        super(Component.literal("Stardew Valley"));
        this.previousGuiScale = previousGuiScale;
    }

    private int logoWidth, logoHeight, logoX, logoY;
    private int buttonWidth, buttonHeight, buttonsY;
    private TitleIconButton optionsButton, questionButton;

    @Override
    protected void init() {
        // Кастомный курсор - тот же, что в редакторе персонажа (см. чат:
        // "замени курсор на этих скринах на тот курсор, как в редакторе
        // персонажа"), см. CustomCursor javadoc.
        CustomCursor.apply();

        // logoWidth раньше считался ТОЛЬКО от this.width - на невысоких
        // окнах (низкий this.height при широком this.width) кнопки уезжали
        // за нижний край экрана, обрезанные рамкой окна (см. чат: "главный
        // экран не изменился"/скриншот). Теперь logoWidth ограничен ЕЩЁ и
        // сверху - высотой, которую логотип+кнопки+отступы займут вместе,
        // так что раскладка гарантированно влезает по вертикали тоже.
        int topMargin = Math.max(10, this.height / 20);
        int bottomMargin = Math.max(10, this.height / 20);
        int gap = Math.max(15, this.height / 15);
        int availableHeight = this.height - topMargin - gap - bottomMargin;

        float logoHeightRatio = LOGO_NATIVE_H / (float) LOGO_NATIVE_W;
        float buttonHeightRatio = (BUTTON_NATIVE_H / (float) BUTTON_NATIVE_W) / 5f; // buttonWidth = logoWidth/5
        int maxLogoWidthByHeight = (int) (availableHeight / (logoHeightRatio + buttonHeightRatio));
        int maxLogoWidthByWidth = Math.min(this.width - 80, 720);

        this.logoWidth = Math.round(Math.max(160, Math.min(maxLogoWidthByWidth, maxLogoWidthByHeight)) * LAYOUT_SCALE);
        this.logoHeight = Math.round(logoWidth * logoHeightRatio);
        this.buttonWidth = logoWidth / 5;
        this.buttonHeight = Math.round(buttonWidth * BUTTON_NATIVE_H / (float) BUTTON_NATIVE_W);

        // Центрируем весь блок (лого+кнопки) в отведённой по вертикали
        // зоне - после LAYOUT_SCALE там остаётся запас, и он должен
        // распределиться СИММЕТРИЧНО (пусто сверху и снизу), а не просто
        // висеть внизу, как было бы при logoY=topMargin всегда.
        int blockHeight = logoHeight + gap + buttonHeight;
        int extraSpace = Math.max(0, availableHeight - blockHeight);
        // -50 - попросили поднять весь блок повыше (было ровно по центру
        // отведённой зоны); floor на 5, чтобы не улетело за верхний край
        // на низких окнах.
        this.logoY = Math.max(5, topMargin + extraSpace / 2 - 50);
        this.logoX = (this.width - logoWidth) / 2;
        // +100 - попросили сдвинуть блок кнопок ниже (независимо от подъёма
        // лого выше); floor так, чтобы кнопки не съехали за нижний край окна.
        this.buttonsY = Math.min(logoY + logoHeight + gap + 100, this.height - buttonHeight - bottomMargin);

        int totalWidth = buttonWidth * 4 + BUTTON_GAP * 3;
        int startX = (this.width - totalWidth) / 2;

        addRenderableWidget(new TitleMainButton(startX, buttonsY, buttonWidth, buttonHeight, 0,
                b -> StardewPlayButtonHandler.onClick(this, previousGuiScale)));

        addRenderableWidget(new TitleMainButton(startX + (buttonWidth + BUTTON_GAP), buttonsY, buttonWidth, buttonHeight, 1,
                b -> {
                    // Форсируем guiScale=2 ЗАНОВО прямо тут, тем же приёмом,
                    // что TitleScreenReplacer/ClientCharacterCreationOpener -
                    // ДО создания экрана, а не внутри его init() (внутри
                    // resizeDisplay() рекурсивно передёрнул бы init() того же
                    // экрана с ещё не обновлёнными this.width/height - см.
                    // чат: "не формирует 2й скейл"). Раньше Load полагался,
                    // что раз мы уже на Title, значит guiScale и так уже 2 -
                    // не всегда верно.
                    if (this.minecraft.options.guiScale().get() != TitleScreenReplacer.FORCED_GUI_SCALE) {
                        this.minecraft.options.guiScale().set(TitleScreenReplacer.FORCED_GUI_SCALE);
                        this.minecraft.resizeDisplay();
                    }
                    this.minecraft.setScreen(new StardewLoadWorldScreen(this, previousGuiScale));
                }));

        // Co-op ещё не реализован (см. docs/architecture.md - "не
        // реализовано") - кнопка присутствует по макету, но неактивна.
        TitleMainButton coop = new TitleMainButton(startX + (buttonWidth + BUTTON_GAP) * 2, buttonsY, buttonWidth, buttonHeight, 2, b -> {});
        coop.active = false;
        addRenderableWidget(coop);

        addRenderableWidget(new TitleMainButton(startX + (buttonWidth + BUTTON_GAP) * 3, buttonsY, buttonWidth, buttonHeight, 3,
                b -> this.minecraft.stop()));

        // options/question - см. чат: "добавь снизу справа 2 кнопки" -
        // стопкой у правого нижнего угла, options сверху, question под ней.
        // Размер и ширины, и высоты - СВОИ у каждой (см. javadoc SMALL_BUTTON_SCALE),
        // а не общий квадратный бокс.
        int optionsWidth = OPTIONS_NATIVE_W * SMALL_BUTTON_SCALE;
        int optionsHeight = OPTIONS_NATIVE_H * SMALL_BUTTON_SCALE;
        int questionWidth = QUESTION_NATIVE_W * SMALL_BUTTON_SCALE;
        int questionHeight = QUESTION_NATIVE_H * SMALL_BUTTON_SCALE;

        int questionY = this.height - questionHeight - SMALL_BUTTON_MARGIN;
        int optionsY = questionY - optionsHeight - SMALL_BUTTON_GAP;

        // options - просто открывает ванильные настройки (см. чат: "добавь просто кнопку 'настройки' из ванильного майнкрафта").
        this.optionsButton = new TitleIconButton(this.width - optionsWidth - SMALL_BUTTON_MARGIN, optionsY, optionsWidth, optionsHeight,
                OPTIONS_TEX, OPTIONS_NATIVE_W, OPTIONS_NATIVE_H,
                b -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)));
        addRenderableWidget(optionsButton);

        // question - чёрное окно поверх меню с пролистываемым текстом (см. StardewAboutScreen).
        this.questionButton = new TitleIconButton(this.width - questionWidth - SMALL_BUTTON_MARGIN, questionY, questionWidth, questionHeight,
                QUESTION_TEX, QUESTION_NATIVE_W, QUESTION_NATIVE_H,
                b -> this.minecraft.setScreen(new StardewAboutScreen(this)));
        addRenderableWidget(questionButton);
    }

    /**
     * Скрывает options/question, пока открыт {@link StardewAboutScreen} (тот
     * рендерит ЭТОТ экран как задник, см. его javadoc) - см. чат: "прятать
     * кнопки вопроса и настроек в этом чёрном меню". Сам этот экран при этом
     * УЖЕ некликабелен, пока активен AboutScreen - Minecraft шлёт ввод только
     * в ТЕКУЩИЙ {@code Screen}, а тут текущий - AboutScreen, не этот (см.
     * его же javadoc) - отдельно защищать клики не от чего.
     */
    void setSmallButtonsHidden(boolean hidden) {
        if (optionsButton != null) optionsButton.visible = !hidden;
        if (questionButton != null) questionButton.visible = !hidden;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xFF1E3A5F, 0xFF6FA8D8);
        graphics.blit(LOGO, logoX, logoY, logoWidth, logoHeight, 0, 0, LOGO_NATIVE_W, LOGO_NATIVE_H, LOGO_NATIVE_W, LOGO_NATIVE_H);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        CustomCursor.restore();
    }
}

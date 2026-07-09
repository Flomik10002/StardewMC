package dev.flomik.stardew.client.screen.title;

import com.mojang.logging.LogUtils;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.CustomCursor;
import dev.flomik.stardew.client.render.StardewFrameRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * "?" — чёрное окно ПОВЕРХ главного меню (не на весь экран, см. чат) с
 * пролистываемым текстом. Заголовок держит ссылку на {@link #parent}
 * ({@link StardewTitleScreen}) и в {@link #render} рисует его ПЕРВЫМ, с
 * заведомо недостижимыми mouseX/mouseY (-1,-1, чтобы ни одна его кнопка не
 * подсветилась как hovered) — простой и надёжный способ получить "оверлей
 * поверх меню" без реального стекинга экранов (Minecraft держит только один
 * активный {@code Screen}; кликов на {@code parent} при этом не будет — ввод
 * идёт только в ТЕКУЩИЙ экран, то есть в этот).
 *
 * Текст читается из ресурса мода ({@link #ABOUT_RESOURCE_PATH}, см.
 * {@code docs/about-screen-text-guide.md} для синтаксиса разметки) через
 * {@link #loadAboutText()} - {@link #FALLBACK_TEXT} используется только если
 * ресурс почему-то отсутствует в jar'е. Пагинация настоящая - каждая
 * логическая строка оборачивается через {@link Font#split} под ширину
 * панели, и накопленные визуальные строки режутся на страницы по количеству
 * строк, которое реально помещается по высоте панели.
 */
public class StardewAboutScreen extends Screen {

    /**
     * Группы: 1=жирный ({@code **text**}), 2=зачёркнутый ({@code ~~text~~}),
     * 3+4=ссылка ({@code [label](url)} - label/url), 5=курсив ({@code _text_}).
     * Курсив на одинарном {@code _}, а не {@code *} - тот уже занят маркером
     * пункта списка (см. {@link #parseLine}), конфликт по стартовому символу
     * с bold/strike/link исключён (у всех разные открывающие символы), так
     * что порядок альтернатив тут не имеет значения.
     */
    private static final Pattern INLINE_PATTERN = Pattern.compile(
            "\\*\\*(.+?)\\*\\*" + "|~~(.+?)~~" + "|\\[(.+?)]\\((.+?)\\)" + "|_(.+?)_");

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Реальный текст - см. {@code docs/about-screen-text-guide.md} для синтаксиса. */
    private static final String ABOUT_RESOURCE_PATH = "/assets/stardew/text/about.md";

    /** Только на случай, если {@link #ABOUT_RESOURCE_PATH} почему-то отсутствует в jar'е - см. {@link #loadAboutText()}. */
    private static final String FALLBACK_TEXT = "**StardewMC**\nНе удалось загрузить about.md - см. лог.";

    private final Screen parent;
    private final List<List<FormattedCharSequence>> pages = new ArrayList<>();
    private int currentPage = 0;

    private int panelX, panelY, panelWidth, panelHeight;
    private static final float PANEL_WIDTH_RATIO = 0.6f;
    private static final float PANEL_HEIGHT_RATIO = 0.65f;
    /** На 1 меньше, чем у CharacterCreationScreen/StardewLoadWorldScreen (3) - попросили именно тут. */
    private static final int FRAME_THICKNESS = 2;
    private static final int TEXT_PAD = 16;
    private static final int BLACK_BG = 0xF0111111;
    /** Затемнение фона (parent) под чёрной панелью - см. чат: "накладывать затемнение". */
    private static final int DIM_OVERLAY = 0x90000000;

    private Button prevButton, nextButton;

    public StardewAboutScreen(Screen parent) {
        super(Component.literal("About"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Тот же кастомный курсор, что в редакторе персонажа (см. чат).
        CustomCursor.apply();

        // Прячем options/question у parent'а, пока мы открыты - см. чат
        // "прятать кнопки вопроса и настроек в этом чёрном меню" и javadoc
        // StardewTitleScreen#setSmallButtonsHidden. Восстанавливаются в
        // removed() - это надёжнее, чем только в обработчике кнопки "Назад"
        // (сработает и при выходе по Esc/любым другим путём).
        if (parent instanceof StardewTitleScreen titleScreen) {
            titleScreen.setSmallButtonsHidden(true);
        }

        this.panelWidth = Math.round(this.width * PANEL_WIDTH_RATIO);
        this.panelHeight = Math.round(this.height * PANEL_HEIGHT_RATIO);
        this.panelX = (this.width - panelWidth) / 2;
        this.panelY = (this.height - panelHeight) / 2;

        buildPages();
        currentPage = Math.min(currentPage, Math.max(0, pages.size() - 1));

        int navY = panelY + panelHeight - TEXT_PAD - 12;
        int navCenterX = panelX + panelWidth / 2;
        prevButton = Button.builder(Component.literal("<"), b -> { currentPage = Math.max(0, currentPage - 1); })
                .bounds(navCenterX - 60, navY, 20, 16).build();
        nextButton = Button.builder(Component.literal(">"), b -> { currentPage = Math.min(pages.size() - 1, currentPage + 1); })
                .bounds(navCenterX + 40, navY, 20, 16).build();
        addRenderableWidget(prevButton);
        addRenderableWidget(nextButton);

        addRenderableWidget(TitleBackButton.bottomRight(this.width, this.height, b -> this.minecraft.setScreen(this.parent)));
    }

    /** Читает {@link #ABOUT_RESOURCE_PATH} из ресурсов мода (classpath, переживает упаковку в jar) - см. {@link #FALLBACK_TEXT}. */
    private static String loadAboutText() {
        try (InputStream stream = StardewAboutScreen.class.getResourceAsStream(ABOUT_RESOURCE_PATH)) {
            if (stream == null) {
                LOGGER.warn("[Stardew] {} не найден, показываю заглушку", ABOUT_RESOURCE_PATH);
                return FALLBACK_TEXT;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[Stardew] Не удалось прочитать {}", ABOUT_RESOURCE_PATH, e);
            return FALLBACK_TEXT;
        }
    }

    /** Разбивает {@link #loadAboutText()} на визуальные строки (перенос по ширине панели) и режет их на страницы по высоте панели. */
    private void buildPages() {
        pages.clear();
        int textAreaWidth = panelWidth - TEXT_PAD * 2;
        int textAreaHeight = panelHeight - TEXT_PAD * 2 - 20; // -20 - место под навигацию снизу
        int linesPerPage = Math.max(1, textAreaHeight / (this.font.lineHeight + 2));

        List<FormattedCharSequence> allLines = new ArrayList<>();
        for (String rawLine : loadAboutText().split("\n", -1)) {
            if (rawLine.isBlank()) {
                allLines.add(FormattedCharSequence.EMPTY);
                continue;
            }
            Component parsed = parseLine(rawLine);
            for (FormattedCharSequence wrapped : this.font.split(parsed, textAreaWidth)) {
                allLines.add(wrapped);
            }
        }

        for (int i = 0; i < allLines.size(); i += linesPerPage) {
            pages.add(new ArrayList<>(allLines.subList(i, Math.min(i + linesPerPage, allLines.size()))));
        }
        if (pages.isEmpty()) {
            pages.add(new ArrayList<>());
        }
    }

    /**
     * {@code * text} -> маркер списка "•", {@code **text**} -> жирный,
     * {@code ~~text~~} -> зачёркнутый, {@code _text_} -> курсив,
     * {@code [label](url)} -> кликабельная ссылка (открывает URL через
     * штатный ванильный {@link #handleComponentClicked} - с подтверждением
     * и уважением настройки "chat links", как обычная ссылка в чате; см.
     * {@link #mouseClicked} и {@link #styleAt}). Больше правил нет - см.
     * javadoc класса.
     */
    private static Component parseLine(String rawLine) {
        String line = rawLine;
        boolean bullet = line.trim().startsWith("* ") || line.trim().equals("*");
        if (bullet) {
            line = "• " + line.trim().substring(1).trim();
        }

        MutableComponent result = Component.empty();
        Matcher matcher = INLINE_PATTERN.matcher(line);
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result.append(line.substring(lastEnd, matcher.start()));
            }
            if (matcher.group(1) != null) {
                result.append(Component.literal(matcher.group(1)).withStyle(ChatFormatting.BOLD));
            } else if (matcher.group(2) != null) {
                result.append(Component.literal(matcher.group(2)).withStyle(ChatFormatting.STRIKETHROUGH));
            } else if (matcher.group(3) != null) {
                String url = matcher.group(4);
                Style linkStyle = Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url)));
                result.append(Component.literal(matcher.group(3)).setStyle(linkStyle));
            } else if (matcher.group(5) != null) {
                result.append(Component.literal(matcher.group(5)).withStyle(ChatFormatting.ITALIC));
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < line.length()) {
            result.append(line.substring(lastEnd));
        }
        return result;
    }

    /** Style под курсором на ТЕКУЩЕЙ странице - общий код для клика и hover-тултипа ссылок. */
    @Nullable
    private Style styleAt(double mouseX, double mouseY) {
        List<FormattedCharSequence> page = pages.get(currentPage);
        int textX = panelX + TEXT_PAD;
        int textY = panelY + TEXT_PAD;
        for (FormattedCharSequence line : page) {
            if (mouseY >= textY && mouseY < textY + this.font.lineHeight && mouseX >= textX) {
                return this.font.getSplitter().componentStyleAtWidth(line, (int) (mouseX - textX));
            }
            textY += this.font.lineHeight + 2;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Style style = styleAt(mouseX, mouseY);
            if (openMailto(style) || handleComponentClicked(style)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Ванильный {@link #handleComponentClicked} пускает в открытие только
     * http/https ({@code Screen.ALLOWED_PROTOCOLS}) - {@code mailto:} он
     * молча отвергает (см. чат: "не обрабатывает mailto как ссылку").
     * Обрабатываем его сами - тем же confirm-диалогом ({@link ConfirmLinkScreen})
     * и тем же {@link Util#getPlatform()}{@code .openUri(...)}, которым
     * пользуется сам {@code Screen} внутри {@code openLink} - просто без
     * протокольного фильтра.
     */
    private boolean openMailto(@Nullable Style style) {
        if (style == null || style.getClickEvent() == null || style.getClickEvent().getAction() != ClickEvent.Action.OPEN_URL) {
            return false;
        }
        String value = style.getClickEvent().getValue();
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            return false;
        }
        if (!"mailto".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        this.minecraft.setScreen(new ConfirmLinkScreen(confirmed -> {
            if (confirmed) {
                Util.getPlatform().openUri(uri);
            }
            this.minecraft.setScreen(this);
        }, value, false));
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Title-экран как задник - см. javadoc класса. mouseX/Y=-1 - ни одна
        // его кнопка не подсветится под курсором (курсор физически наведён
        // на ЭТОТ экран, а не на parent).
        this.parent.render(graphics, -1, -1, partialTick);
        graphics.fill(0, 0, this.width, this.height, DIM_OVERLAY);

        StardewFrameRenderer.drawPanel(graphics, panelX, panelY, panelWidth, panelHeight, FRAME_THICKNESS,
                BLACK_BG, StardewFrameRenderer.BORDER_OUTER, StardewFrameRenderer.BORDER_MIDDLE, StardewFrameRenderer.BORDER_INNER);
        graphics.flush(); // см. StardewLoadWorldScreen - без этого рамка уезжает под текст/скролл ниже.

        int textX = panelX + TEXT_PAD;
        int textY = panelY + TEXT_PAD;
        List<FormattedCharSequence> page = pages.get(currentPage);
        for (FormattedCharSequence line : page) {
            graphics.drawString(this.font, line, textX, textY, 0xFFE0D5C0);
            textY += this.font.lineHeight + 2;
        }

        if (pages.size() > 1) {
            String pageLabel = (currentPage + 1) + " / " + pages.size();
            graphics.drawCenteredString(this.font, pageLabel, panelX + panelWidth / 2, panelY + panelHeight - TEXT_PAD - 10, 0xFFE0D5C0);
        }
        prevButton.visible = pages.size() > 1;
        nextButton.visible = pages.size() > 1;
        prevButton.active = currentPage > 0;
        nextButton.active = currentPage < pages.size() - 1;

        String version = ModList.get().getModContainerById(StardewMod.MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("dev");
        graphics.drawString(this.font, "v" + version, 10, this.height - 16, 0xFFB0B0B0);

        // Тултип с URL при наведении на ссылку - тот же приём, что у обычных
        // кликабельных ссылок в чате (renderComponentHoverEffect сама ничего
        // не покажет без HoverEvent на стиле - он проставлен в parseLine).
        graphics.renderComponentHoverEffect(this.font, styleAt(mouseX, mouseY), mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Возвращает options/question у parent'а обратно - см. init(); срабатывает при ЛЮБОМ выходе (кнопка "Назад", Esc, что угодно ещё), не только по клику. */
    @Override
    public void removed() {
        CustomCursor.restore();
        if (parent instanceof StardewTitleScreen titleScreen) {
            titleScreen.setSmallButtonsHidden(false);
        }
    }
}

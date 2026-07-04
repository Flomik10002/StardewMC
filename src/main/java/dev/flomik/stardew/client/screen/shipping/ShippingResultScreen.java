package dev.flomik.stardew.client.screen.shipping;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.module.time.TimeFreezeManager;
import dev.flomik.stardew.common.module.time.network.C2STimeFreezePacket;
import dev.flomik.stardew.common.registry.framework.IStardewItem;
import dev.flomik.stardew.common.registry.framework.tooltip.StardewTooltip;
import dev.flomik.stardew.common.registry.framework.tooltip.TooltipPresets;
import dev.flomik.stardew.core.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Экран результатов продаж как в Stardew Valley.
 * Показывает все проданные предметы по категориям с анимациями.
 * 
 * Фазы:
 * 1. Intro (3500ms) - категории появляются одна за другой
 * 2. Main - игрок может просматривать категории
 * 3. Outro - fade out, смена даты, переход к следующему дню
 */
public class ShippingResultScreen extends Screen {

    // Константы времени (в миллисекундах)
    private static final int INTRO_TIME = 3500;
    private static final int TIME_PER_CATEGORY = 500;
    private static final int OUTRO_FADE_TIME = 800;
    private static final int OUTRO_PAUSE_BEFORE_DATE = 700;
    private static final int FINAL_OUTRO_TIME = 2000;
    private static final int CATEGORY_COUNT_DURATION_MS = 900;
    private static final int TOTAL_COUNT_DURATION_MS = 1500;
    
    // Текстуры
    private static final ResourceLocation SCROLL_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/shipping/scroll.png");
    private static final ResourceLocation PLATE_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/shipping/plate.png");
    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/shipping/slot.png");
    private static final ResourceLocation OK_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/shipping/ok.png");
    private static final ResourceLocation PLUS_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/shipping/plus.png");
    private static final ResourceLocation DIGITS_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/clock/digits.png");
    
    // Размеры текстур
    private static final int DATE_SCROLL_WIDTH = 158;
    private static final int DATE_SCROLL_HEIGHT = 18;
    private static final int CATEGORY_PLATE_WIDTH = 128;
    private static final int CATEGORY_PLATE_HEIGHT = 26;
    private static final int SCROLL_TEXT_COLOR = 0xFF5C3A21;
    private static final int OK_BUTTON_SIZE = 16; // Исходный размер
    private static final int OK_BUTTON_DISPLAY_SIZE = 20;
    private static final int PLUS_BUTTON_SOURCE_WIDTH = 10;
    private static final int PLUS_BUTTON_SOURCE_HEIGHT = 11;
    private static final int PLUS_BUTTON_DISPLAY_WIDTH = PLUS_BUTTON_SOURCE_WIDTH;
    private static final int PLUS_BUTTON_DISPLAY_HEIGHT = PLUS_BUTTON_SOURCE_HEIGHT;
    private static final int ITEM_SLOT_SIZE = 24;
    private static final int SLOT_OFFSET = 1;
    private static final int OK_BUTTON_MARGIN = 6;
    private static final int PLUS_BUTTON_MARGIN = -1;
    private static final int MONEY_DIGITS_OFFSET_X = 9;
    private static final int MONEY_DIGITS_OFFSET_Y = 1;
    private static final int SCROLL_TEXT_OFFSET_Y = 0;
    private static final int MINECRAFT_TEXT_OFFSET_Y = 2;
    private static final int DATE_TO_PLATES_GAP = 31;
    private static final int PLATE_GAP = 1;
    private static final int MAIN_ROW_COUNT = 6;
    private static final int MAIN_CONTENT_HEIGHT = DATE_SCROLL_HEIGHT + DATE_TO_PLATES_GAP
            + MAIN_ROW_COUNT * CATEGORY_PLATE_HEIGHT + (MAIN_ROW_COUNT - 1) * PLATE_GAP;
    private static final int MAIN_CONTENT_WIDTH = CATEGORY_PLATE_WIDTH + SLOT_OFFSET + ITEM_SLOT_SIZE;
    private static final int MIN_TOP_MARGIN = 12;
    
    // Размеры цифр
    private static final int DIGIT_WIDTH = 5;
    private static final int DIGIT_HEIGHT = 8;
    private static final int MONEY_DIGIT_WAVE_AMPLITUDE = 1;
    private static final float MONEY_DIGIT_WAVE_SPEED = 0.006f;
    
    // Позиция чисел внутри плашки (относительно плашки)
    private static final int MONEY_X_RIGHT = 104; // Правая позиция для начала чисел
    private static final int MONEY_Y = (CATEGORY_PLATE_HEIGHT - DIGIT_HEIGHT) / 2; // Центрирование цифр по плашке
    
    // Размеры
    private static final int ITEMS_PER_PAGE = 9;
    
    // Данные
    private final List<List<ItemStack>> categoryItems = new ArrayList<>();
    private final List<Integer> categoryTotals = new ArrayList<>();
    private final List<MoneyDial> categoryDials = new ArrayList<>();
    private final Map<ItemStack, Integer> itemValues = new HashMap<>();
    private final Map<ItemStack, Integer> singleItemValues = new HashMap<>();
    
    // Состояние
    private int introTimer = INTRO_TIME;
    private int outroFadeTimer = 0;
    private int outroPauseTimer = 0;
    private int finalOutroTimer = 0;
    private boolean outro = false;
    private boolean newDayPlaque = false;
    private boolean finished = false;
    private boolean totalCountingStarted = false;
    private float totalCountingTimer = 0f;
    
    // Навигация
    private int currentPage = -1; // -1 = главный экран, 0-5 = страницы категорий
    private int currentTab = 0; // Пагинация внутри категории
    
    // Позиционирование
    private int centerX;
    private int centerY;
    private int dayPlaqueY;
    
    // UI элементы
    private Button backButton;
    private Button forwardButton;
    private boolean okButtonVisible = false;
    private int okButtonX, okButtonY;
    
    // Анимации
    private final List<TemporaryAnimation> animations = new ArrayList<>();
    private float weatherX = 0;
    private float moneyWaveTimer = 0;
    private int smokeTimer = 0;
    
    // Дата (формат: "Day {day} of {Season}, Year {year}", например "Day 3 of Winter, Year 1")
    private final String yesterdayDate;
    private final String todayDate;
    private final long totalEarnings;
    
    // Callback при завершении
    private final Runnable onComplete;
    
    public ShippingResultScreen(List<ItemStack> shippedItems, String yesterdayDate, String todayDate, Runnable onComplete) {
        super(Component.translatable("gui.stardew.shipping_results"));
        this.yesterdayDate = yesterdayDate;
        this.todayDate = todayDate;
        this.onComplete = onComplete;
        
        // Инициализируем категории
        for (int i = 0; i < 6; i++) {
            categoryItems.add(new ArrayList<>());
            categoryTotals.add(0);
            categoryDials.add(new MoneyDial(7, i == 5));
        }
        
        // Парсим предметы
        parseItems(shippedItems);
        
        // Считаем общий заработок
        this.totalEarnings = categoryTotals.get(5);
    }
    
    /**
     * Распределяет предметы по категориям и считает стоимость.
     */
    private void parseItems(List<ItemStack> items) {
        // Консолидируем стаки
        Map<String, ItemStack> consolidated = new LinkedHashMap<>();
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            
            String key = stack.getItem().getDescriptionId() + "_" + Quality.get(stack).ordinal();
            if (consolidated.containsKey(key)) {
                ItemStack existing = consolidated.get(key);
                existing.grow(stack.getCount());
            } else {
                consolidated.put(key, stack.copy());
            }
        }
        
        // Распределяем по категориям
        for (ItemStack stack : consolidated.values()) {
            ShippingCategory category = ShippingCategory.categorize(stack);
            int catIndex = category.getIndex();
            
            categoryItems.get(catIndex).add(stack);
            
            int basePrice = getBasePrice(stack);
            Quality quality = Quality.get(stack);
            int singlePrice = (int) quality.calculatePrice(basePrice);
            int totalPrice = singlePrice * stack.getCount();
            
            categoryTotals.set(catIndex, categoryTotals.get(catIndex) + totalPrice);
            itemValues.put(stack, totalPrice);
            singleItemValues.put(stack, singlePrice);
        }
        
        // Считаем общую сумму
        int total = 0;
        for (int i = 0; i < 5; i++) {
            total += categoryTotals.get(i);
            categoryItems.get(5).addAll(categoryItems.get(i));
            categoryDials.get(i).currentValue = categoryTotals.get(i);
            categoryDials.get(i).previousTargetValue = categoryTotals.get(i);
        }
        categoryTotals.set(5, total);
        categoryDials.get(5).currentValue = total;
    }
    
    private int getBasePrice(ItemStack stack) {
        if (stack.getItem() instanceof IStardewItem stardewItem) {
            List<StardewTooltip> tooltips = stardewItem.getTooltips();
            if (tooltips != null) {
                for (StardewTooltip tooltip : tooltips) {
                    if (tooltip instanceof TooltipPresets.PriceTooltip priceTooltip) {
                        return priceTooltip.getBasePrice();
                    }
                }
            }
        }
        return 0;
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            PacketHandler.sendToServer(new C2STimeFreezePacket(true));
            TimeFreezeManager.freezeClient();
        }
        
        centerX = this.width / 2;
        centerY = this.height / 2;
        dayPlaqueY = Math.max(MIN_TOP_MARGIN, centerY - MAIN_CONTENT_HEIGHT / 2);
        
        // OK кнопка будет рисоваться текстурой (без стандартного Button)
        okButtonVisible = false;
        
        // Кнопка назад
        backButton = Button.builder(Component.literal("<"), btn -> onBackClicked())
                .bounds(20, this.height - 40, 40, 20)
                .build();
        this.addRenderableWidget(backButton);
        backButton.visible = false;
        
        // Кнопка вперед
        forwardButton = Button.builder(Component.literal(">"), btn -> onForwardClicked())
                .bounds(this.width - 60, this.height - 40, 40, 20)
                .build();
        this.addRenderableWidget(forwardButton);
        forwardButton.visible = false;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        float deltaTime = 50f; // ~20 ticks per second = 50ms per tick
        
        // Обновляем анимации
        weatherX += deltaTime * 0.03f;
        moneyWaveTimer += deltaTime;
        
        // Удаляем завершенные анимации
        animations.removeIf(anim -> anim.update(deltaTime));
        
        if (outro) {
            updateOutro(deltaTime);
        } else if (introTimer > 0) {
            updateIntro(deltaTime);
        } else {
            updateMain(deltaTime);
        }
        
        updateMoneyCounters(deltaTime);
    }

    private void updateMoneyCounters(float deltaTime) {
        if (introTimer <= 0) {
            for (int i = 0; i < 5; i++) {
                categoryDials.get(i).update(categoryTotals.get(i), deltaTime, CATEGORY_COUNT_DURATION_MS);
            }
            updateTotalCounter(deltaTime);
            return;
        }

        // Во время интро обновляем только те категории, которые уже появились.
        for (int i = 0; i < 5; i++) {
            if (introTimer <= 2500 - i * TIME_PER_CATEGORY) {
                categoryDials.get(i).update(categoryTotals.get(i), deltaTime, CATEGORY_COUNT_DURATION_MS);
            }
        }
    }

    private void updateTotalCounter(float deltaTime) {
        MoneyDial totalDial = categoryDials.get(5);
        int availableTarget = getCountedCategoryTotal();
        int finalTarget = categoryTotals.get(5);

        if (!totalCountingStarted) {
            totalDial.currentValue = 0;
            totalCountingStarted = true;
            totalCountingTimer = 0f;
        }

        if (availableTarget <= 0) {
            totalDial.currentValue = 0;
            totalDial.previousTargetValue = 0;
            totalCountingTimer = 0f;
            return;
        }

        totalDial.previousTargetValue = availableTarget;
        if (totalDial.currentValue > availableTarget) {
            totalDial.currentValue = availableTarget;
        }

        if (totalDial.currentValue < availableTarget) {
            int step = Math.max(1,
                    (int)Math.ceil(Math.max(1, finalTarget) * deltaTime / TOTAL_COUNT_DURATION_MS));
            totalDial.currentValue = Math.min(availableTarget, totalDial.currentValue + step);
            totalCountingTimer = 0f;
        } else if (availableTarget >= finalTarget && allCategoryCountersFinished()) {
            totalCountingTimer = TOTAL_COUNT_DURATION_MS;
        }
    }

    private int getCountedCategoryTotal() {
        int total = 0;
        for (int i = 0; i < 5; i++) {
            total += categoryDials.get(i).currentValue;
        }
        return total;
    }

    private boolean allCategoryCountersFinished() {
        for (int i = 0; i < 5; i++) {
            if (categoryDials.get(i).currentValue != categoryTotals.get(i)) {
                return false;
            }
        }
        return true;
    }
    
    private void updateIntro(float deltaTime) {
        int oldTimer = introTimer;
        
        // Ускорение при зажатой ЛКМ (как в оригинале)
        boolean mousePressed = Minecraft.getInstance().mouseHandler.isLeftPressed();
        int speed = mousePressed ? 3 : 1;
        introTimer -= (int)(deltaTime * speed);
        
        // Звук появления категории каждые 500ms
        if (oldTimer % TIME_PER_CATEGORY < introTimer % TIME_PER_CATEGORY && introTimer <= 3000) {
            int categoryIndex = 4 - introTimer / TIME_PER_CATEGORY;
            if (categoryIndex >= 0 && categoryIndex < 6) {
                if (!categoryItems.get(categoryIndex).isEmpty()) {
                    playSound(ShippingCategory.fromIndex(categoryIndex).getSound());
                    categoryDials.get(categoryIndex).currentValue = 0;
                    categoryDials.get(categoryIndex).previousTargetValue = 0;
                } else {
                    playSound("block.stone.step");
                }
            }
        }
        
        // Конец интро
        if (introTimer <= 0) {
            introTimer = 0;
            playSound("entity.experience_orb.pickup");
            categoryDials.get(5).currentValue = 0;
            categoryDials.get(5).previousTargetValue = 0;
            totalCountingStarted = false;
            totalCountingTimer = 0f;
            okButtonVisible = true;
        }
    }
    
    private void updateMain(float deltaTime) {
        // Добавляем дым из трубы
        smokeTimer -= (int)deltaTime;
        if (smokeTimer <= 0) {
            smokeTimer = 100;
            // Можно добавить частицы дыма
        }
        
        // Обновляем видимость кнопок
        okButtonVisible = currentPage == -1;
        backButton.visible = currentPage != -1;
        forwardButton.visible = currentPage != -1 && showForwardButton();
    }
    
    private void updateOutro(float deltaTime) {
        if (outroFadeTimer > 0) {
            outroFadeTimer -= (int)deltaTime;
        } else if (dayPlaqueY < centerY - 32) {
            // Плашка с датой движется вниз
            animations.clear();
            dayPlaqueY += (int)Math.ceil(deltaTime * 0.35f);
            if (dayPlaqueY >= centerY - 32) {
                outroPauseTimer = OUTRO_PAUSE_BEFORE_DATE;
            }
        } else if (outroPauseTimer > 0) {
            outroPauseTimer -= (int)deltaTime;
            if (outroPauseTimer <= 0) {
                newDayPlaque = true;
                playSound("entity.player.levelup");
                finalOutroTimer = FINAL_OUTRO_TIME;
            }
        } else if (finalOutroTimer > 0) {
            finalOutroTimer -= (int)deltaTime;
            if (finalOutroTimer <= 0) {
                finished = true;
                this.onClose();
            }
        }
    }
    
    private void okClicked() {
        if (introTimer > 0) return;
        
        outro = true;
        outroFadeTimer = OUTRO_FADE_TIME;
        playSound("ui.button.click");
    }
    
    private void onBackClicked() {
        if (currentTab > 0) {
            currentTab--;
        } else {
            currentPage = -1;
        }
        playSound("ui.button.click");
    }
    
    private void onForwardClicked() {
        if (showForwardButton()) {
            currentTab++;
            playSound("ui.button.click");
        }
    }
    
    private boolean showForwardButton() {
        if (currentPage < 0 || currentPage >= categoryItems.size()) return false;
        return categoryItems.get(currentPage).size() > ITEMS_PER_PAGE * (currentTab + 1);
    }
    
    private void playSound(String soundId) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(
                SoundEvents.UI_BUTTON_CLICK.value(), 1.0f));
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Рисуем фон
        renderBackground(graphics);
        
        // Рисуем ночное небо
        renderNightSky(graphics, partialTick);
        
        // Рисуем анимации (звезды, облака и т.д.)
        for (TemporaryAnimation anim : animations) {
            anim.render(graphics, partialTick);
        }
        
        // Рисуем основной контент
        if (currentPage == -1) {
            renderMainPage(graphics, mouseX, mouseY, partialTick);
        } else {
            renderCategoryPage(graphics, mouseX, mouseY, partialTick);
        }
        
        // Рисуем outro эффекты
        if (outro) {
            renderOutro(graphics, partialTick);
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private void renderNightSky(GuiGraphics graphics, float partialTick) {
        // Градиент ночного неба
        float alpha = 1f - (float)introTimer / INTRO_TIME;
        
        // Темно-синий фон
        graphics.fill(0, 0, this.width, this.height, 
                ((int)(alpha * 255) << 24) | 0x0A1628);
        
        // Звезды (простые точки)
        Random rand = new Random(12345); // Фиксированный seed для стабильных звезд
        for (int i = 0; i < 50; i++) {
            int starX = rand.nextInt(this.width);
            int starY = rand.nextInt(this.height / 2);
            float twinkle = (float)Math.sin((System.currentTimeMillis() + i * 100) / 500.0) * 0.3f + 0.7f;
            int starAlpha = (int)(alpha * twinkle * 255);
            graphics.fill(starX, starY, starX + 1, starY + 1, (starAlpha << 24) | 0xFFFFFF);
        }
        
        // Холмы/земля внизу
        int groundY = this.height - 60;
        graphics.fill(0, groundY, this.width, this.height, 
                ((int)(alpha * 255) << 24) | 0x1A3320);
    }
    
    private void renderMainPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Дата вчерашнего дня на свитке (158x18) - ВСЕГДА отображается в меню
        // Позиционирование строится от свитка
        int scrollX = centerX - DATE_SCROLL_WIDTH / 2;
        int scrollY = dayPlaqueY;
        
        // Рисуем текстуру свитка (всегда в меню, не только в outro)
        if (!outro) {
            graphics.blit(SCROLL_TEXTURE, scrollX, scrollY, 0, 0, DATE_SCROLL_WIDTH, DATE_SCROLL_HEIGHT, DATE_SCROLL_WIDTH, DATE_SCROLL_HEIGHT);
            // Текст даты поверх (формат: "Day {day} of {Season}, Year {year}")
            drawCenteredScrollText(graphics, yesterdayDate,
                    centerX,
                    centeredTextY(scrollY, DATE_SCROLL_HEIGHT) + SCROLL_TEXT_OFFSET_Y,
                    SCROLL_TEXT_COLOR);
        }
        
        // Плашки располагаются под свитком с отступом, вертикально (в колонну)
        int platesStartY = scrollY + DATE_SCROLL_HEIGHT + DATE_TO_PLATES_GAP;
        
        // Центрируем только список категорий и slot. OK не должен смещать основной блок.
        int platesStartX = centerX - MAIN_CONTENT_WIDTH / 2;
        
        // Рисуем плашки вертикально (в колонну) с отступом 1 пиксель
        for (int i = 0; i < 6; i++) {
            // Появляются постепенно
            boolean plateVisible = introTimer <= 2500 - i * TIME_PER_CATEGORY;
            if (!plateVisible) continue;
            
            ShippingCategory cat = ShippingCategory.fromIndex(i);
            boolean hasItems = !categoryItems.get(i).isEmpty();
            
            // Y позиция плашки: начальная позиция + i * (высота плашки + отступ)
            int plateX = platesStartX;
            int plateY = platesStartY + i * (CATEGORY_PLATE_HEIGHT + PLATE_GAP);
            
            // Плашка категории (128x26) - одна текстура для всех
            graphics.blit(PLATE_TEXTURE, plateX, plateY, 0, 0, CATEGORY_PLATE_WIDTH, CATEGORY_PLATE_HEIGHT, CATEGORY_PLATE_WIDTH, CATEGORY_PLATE_HEIGHT);
            
            // Название категории (слева на плашке)
            String catName = Component.translatable(cat.getTranslationKey()).getString();
            drawMinecraftString(graphics, catName, plateX + 8,
                    centeredTextY(plateY, CATEGORY_PLATE_HEIGHT) + MINECRAFT_TEXT_OFFSET_Y,
                    SCROLL_TEXT_COLOR);
            
            // Счетчик денег с использованием digits, справа налево.
            if (hasItems) {
                renderMoneyDigits(graphics,
                        plateX + MONEY_X_RIGHT + MONEY_DIGITS_OFFSET_X,
                        plateY + MONEY_Y + MONEY_DIGITS_OFFSET_Y,
                        categoryDials.get(i).currentValue,
                        isMoneyCounterAnimating(i));
            } else {
                renderMoneyDigits(graphics,
                        plateX + MONEY_X_RIGHT + MONEY_DIGITS_OFFSET_X,
                        plateY + MONEY_Y + MONEY_DIGITS_OFFSET_Y,
                        0,
                        false);
            }
            
            // Иконка слота и предмет справа от плашки - ТОЛЬКО если плашка загрузилась
            int slotX = plateX + CATEGORY_PLATE_WIDTH + SLOT_OFFSET;
            int slotY = plateY + 1; // Выровнено по центру плашки (26-24)/2 = 1
            
            graphics.blit(SLOT_TEXTURE, slotX, slotY, 0, 0, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE, ITEM_SLOT_SIZE);
            
            // При outro item preview скрывается, чтобы затемнение и свиток не конфликтовали с иконками.
            if (hasItems && !outro) {
                ItemStack previewStack = categoryItems.get(i).get(0);
                graphics.renderItem(previewStack, slotX + 4, slotY + 4);
                graphics.renderItemDecorations(this.font, previewStack, slotX + 4, slotY + 4);
            }
            
            // Кнопка "+" для просмотра деталей: справа от item slot, без смещения основного блока.
            if (hasItems && i < 5) {
                int btnX = slotX + ITEM_SLOT_SIZE + PLUS_BUTTON_MARGIN;
                int btnY = slotY + (ITEM_SLOT_SIZE - PLUS_BUTTON_DISPLAY_HEIGHT) / 2;
                boolean hovered = mouseX >= btnX && mouseX <= btnX + PLUS_BUTTON_DISPLAY_WIDTH &&
                                  mouseY >= btnY && mouseY <= btnY + PLUS_BUTTON_DISPLAY_HEIGHT;

                graphics.blit(PLUS_TEXTURE, btnX, btnY, PLUS_BUTTON_DISPLAY_WIDTH, PLUS_BUTTON_DISPLAY_HEIGHT,
                        0, 0, PLUS_BUTTON_SOURCE_WIDTH, PLUS_BUTTON_SOURCE_HEIGHT, 20, 11);
                if (hovered) {
                    graphics.fill(btnX, btnY, btnX + PLUS_BUTTON_DISPLAY_WIDTH,
                            btnY + PLUS_BUTTON_DISPLAY_HEIGHT, 0x22FFFFFF);
                }
            }
        }
        
        // Разделительная линия перед Total (перед последней плашкой)
        if (introTimer <= 0) {
            int dividerY = platesStartY + 5 * (CATEGORY_PLATE_HEIGHT + PLATE_GAP) - 1;
            graphics.fill(platesStartX, dividerY, platesStartX + CATEGORY_PLATE_WIDTH, dividerY + 1, 0xFFFFFFFF);
        }
        
        // Кнопка OK ставится после slot, без наложения на Total плашку.
        if (okButtonVisible && introTimer <= 0) {
            // Позиция Total плашки
            int totalPlateY = platesStartY + 5 * (CATEGORY_PLATE_HEIGHT + PLATE_GAP);
            
            okButtonX = platesStartX + CATEGORY_PLATE_WIDTH + SLOT_OFFSET + ITEM_SLOT_SIZE + OK_BUTTON_MARGIN;
            okButtonY = totalPlateY + (CATEGORY_PLATE_HEIGHT - OK_BUTTON_DISPLAY_SIZE) / 2;
            
            boolean hovered = mouseX >= okButtonX && mouseX <= okButtonX + OK_BUTTON_DISPLAY_SIZE &&
                             mouseY >= okButtonY && mouseY <= okButtonY + OK_BUTTON_DISPLAY_SIZE;
            
            graphics.blit(OK_TEXTURE, okButtonX, okButtonY, OK_BUTTON_DISPLAY_SIZE, OK_BUTTON_DISPLAY_SIZE,
                    0, 0, OK_BUTTON_SIZE, OK_BUTTON_SIZE, OK_BUTTON_SIZE, OK_BUTTON_SIZE);
            if (hovered) {
                graphics.fill(okButtonX, okButtonY, okButtonX + OK_BUTTON_DISPLAY_SIZE,
                        okButtonY + OK_BUTTON_DISPLAY_SIZE, 0x22FFFFFF);
            }
        }
    }
    
    /**
     * Рисует число используя digits текстуру, справа налево.
     * @param graphics GuiGraphics
     * @param xRight Правая позиция (отсюда начинаем рисовать справа налево)
     * @param y Y позиция
     * @param value Значение для отображения
     */
    private void renderMoneyDigits(GuiGraphics graphics, int xRight, int y, int value, boolean wave) {
        String moneyStr = String.valueOf(value);
        int len = moneyStr.length();
        
        for (int i = 0; i < len; i++) {
            char c = moneyStr.charAt(len - 1 - i);
            int digit = c - '0';
            
            // Позиция справа налево: xRight - (i * (ширина + отступ))
            int x = xRight - DIGIT_WIDTH - (i * (DIGIT_WIDTH + 1));
            
            // В текстуре digits.png цифры расположены вертикально
            // vIndex = 9 - digit означает что 0 находится внизу (y=9*8), 9 вверху (y=0)
            int vIndex = 9 - digit;
            int v = vIndex * DIGIT_HEIGHT;
            int waveY = y;
            if (wave) {
                float phase = moneyWaveTimer * MONEY_DIGIT_WAVE_SPEED + i * 0.75f;
                waveY += Math.round((float)Math.sin(phase) * MONEY_DIGIT_WAVE_AMPLITUDE);
            }
            
            graphics.blit(DIGITS_TEXTURE, x, waveY, 0, v, DIGIT_WIDTH, DIGIT_HEIGHT, 5, 80);
        }
    }

    private boolean isMoneyCounterAnimating(int index) {
        if (index == 5) {
            return introTimer <= 0 && categoryTotals.get(5) > 0;
        }
        return categoryDials.get(index).currentValue != categoryTotals.get(index)
                || index == getMostValuableCategoryIndex();
    }

    private int getMostValuableCategoryIndex() {
        int bestIndex = -1;
        int bestValue = 0;
        for (int i = 0; i < 5; i++) {
            int value = categoryTotals.get(i);
            if (value > bestValue) {
                bestValue = value;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
    
    private void renderCategoryPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (currentPage < 0 || currentPage >= categoryItems.size()) return;
        
        List<ItemStack> items = categoryItems.get(currentPage);
        ShippingCategory cat = ShippingCategory.fromIndex(currentPage);
        
        // Заголовок
        String title = Component.translatable(cat.getTranslationKey()).getString();
        drawCenteredString(graphics, this.font, title, centerX,
                30 + MINECRAFT_TEXT_OFFSET_Y, 0xFFFFFF);
        
        // Рамка
        int boxX = centerX - 200;
        int boxY = 60;
        int boxWidth = 400;
        int boxHeight = this.height - 120;
        
        graphics.fill(boxX - 2, boxY - 2, boxX + boxWidth + 2, boxY + boxHeight + 2, 0xFFFFFFFF);
        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF2A2A2A);
        
        // Предметы
        int startIndex = currentTab * ITEMS_PER_PAGE;
        int itemY = boxY + 10;
        int itemIconSize = 16;
        int itemPadding = 4;
        int textStartX = boxX + itemIconSize + itemPadding * 2;
        
        for (int i = startIndex; i < Math.min(startIndex + ITEMS_PER_PAGE, items.size()); i++) {
            ItemStack stack = items.get(i);
            
            // Иконка предмета
            graphics.renderItem(stack, boxX + itemPadding, itemY);
            graphics.renderItemDecorations(this.font, stack, boxX + itemPadding, itemY);
            
            // Название с количеством и ценой за единицу (как в оригинале: "Name x15")
            String name = stack.getHoverName().getString();
            int singlePrice = singleItemValues.getOrDefault(stack, 0);
            String subtotalStr = name + " x" + formatNumber(singlePrice);
            
            // Общая стоимость
            int totalPrice = itemValues.getOrDefault(stack, 0);
            String totalStr = formatNumber(totalPrice) + "g";
            int totalWidth = this.font.width(totalStr);
            
            // Позиция для итоговой цены (справа с отступом)
            int totalPosX = boxX + boxWidth - 64 - totalWidth;
            
            // Добавляем точки пока не упремся в правую границу
            String dotsAndName = subtotalStr;
            int maxWidth = boxWidth - 192; // Отступы слева и справа
            while (this.font.width(dotsAndName + totalStr) < maxWidth) {
                dotsAndName += " .";
            }
            
            // Если переполнили, убираем последнюю точку
            if (this.font.width(dotsAndName + totalStr) >= boxWidth - 64) {
                if (dotsAndName.endsWith(" .")) {
                    dotsAndName = dotsAndName.substring(0, dotsAndName.length() - 2);
                } else if (dotsAndName.endsWith(".")) {
                    dotsAndName = dotsAndName.substring(0, dotsAndName.length() - 1);
                }
            }
            
            // Рисуем название с точками
            graphics.drawString(this.font, dotsAndName, textStartX,
                    itemY + 4 + MINECRAFT_TEXT_OFFSET_Y, 0xFFFFFF);
            
            // Рисуем итоговую цену справа
            graphics.drawString(this.font, totalStr, totalPosX,
                    itemY + 4 + MINECRAFT_TEXT_OFFSET_Y, 0xFFD700);
            
            itemY += 68; // Как в оригинале (68 пикселей между строками)
        }
        
        // Номер страницы
        int totalPages = (int)Math.ceil(items.size() / (float)ITEMS_PER_PAGE);
        String pageStr = (currentTab + 1) + "/" + totalPages;
        graphics.drawString(this.font, pageStr,
                centerX - this.font.width(pageStr) / 2,
                this.height - 55 + MINECRAFT_TEXT_OFFSET_Y,
                0xAAAAAA);
    }
    
    private void renderOutro(GuiGraphics graphics, float partialTick) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Затемнение
        float fadeProgress = 1f - (float)outroFadeTimer / OUTRO_FADE_TIME;
        int alpha = (int)(fadeProgress * 255);
        graphics.fill(0, 0, this.width, this.height, (alpha << 24) | 0x000000);
        
        // Плашка с датой на свитке (158x18)
        String dateText = newDayPlaque ? todayDate : yesterdayDate;
        int scrollX = centerX - DATE_SCROLL_WIDTH / 2;
        graphics.blit(SCROLL_TEXTURE, scrollX, dayPlaqueY, 0, 0, DATE_SCROLL_WIDTH, DATE_SCROLL_HEIGHT, DATE_SCROLL_WIDTH, DATE_SCROLL_HEIGHT);
        drawCenteredScrollText(graphics, dateText,
                centerX,
                centeredTextY(dayPlaqueY, DATE_SCROLL_HEIGHT) + SCROLL_TEXT_OFFSET_Y,
                SCROLL_TEXT_COLOR);
        
        // Финальное затемнение
        if (finalOutroTimer > 0) {
            float finalFade = 1f - (float)finalOutroTimer / FINAL_OUTRO_TIME;
            int finalAlpha = (int)(finalFade * 255);
            graphics.fill(0, 0, this.width, this.height, (finalAlpha << 24) | 0x000000);
        }
    }
    
    /**
     * Форматирует число с запятыми как разделителями тысяч (как в оригинале).
     */
    private String formatNumber(int value) {
        if (value < 1000) {
            return String.valueOf(value);
        }
        
        String numStr = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        
        for (int i = numStr.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                sb.insert(0, ',');
            }
            sb.insert(0, numStr.charAt(i));
            count++;
        }
        
        return sb.toString();
    }
    
    private void drawCenteredString(GuiGraphics graphics, net.minecraft.client.gui.Font font, 
                                     String text, int x, int y, int color) {
        Component boldText = Component.literal(text).withStyle(ChatFormatting.BOLD);
        graphics.drawString(font, boldText, x - font.width(boldText) / 2, y, color, false);
    }

    private void drawCenteredScrollText(GuiGraphics graphics, String text, int x, int y, int color) {
        Component boldText = Component.literal(text).withStyle(ChatFormatting.BOLD);
        graphics.drawString(this.font, boldText, x - this.font.width(boldText) / 2, y, color, false);
    }

    private void drawMinecraftString(GuiGraphics graphics, String text, int x, int y, int color) {
        Component boldText = Component.literal(text).withStyle(ChatFormatting.BOLD);
        graphics.drawString(this.font, boldText, x, y, color, false);
    }

    private int centeredTextY(int y, int height) {
        return y + (height - this.font.lineHeight) / 2;
    }
    
    private boolean isLeftClickDown() {
        return Minecraft.getInstance().mouseHandler.isLeftPressed();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (outro) return false;
        
        // Клик на категориях главной страницы
        if (currentPage == -1 && introTimer <= 0) {
            // Клик по кнопке OK
            if (okButtonVisible) {
                if (mouseX >= okButtonX && mouseX <= okButtonX + OK_BUTTON_DISPLAY_SIZE &&
                    mouseY >= okButtonY && mouseY <= okButtonY + OK_BUTTON_DISPLAY_SIZE) {
                    okClicked();
                    return true;
                }
            }
            
            // Клик по кнопке "+" категорий (вертикальное расположение)
            // Вычисляем позиции плашек (та же логика что в renderMainPage)
            int scrollY = dayPlaqueY;
            int platesStartY = scrollY + DATE_SCROLL_HEIGHT + DATE_TO_PLATES_GAP;
            int platesStartX = centerX - MAIN_CONTENT_WIDTH / 2;
            
            for (int i = 0; i < 5; i++) {
                if (categoryItems.get(i).isEmpty()) continue;
                
                // Плашки расположены вертикально
                int plateX = platesStartX;
                int plateY = platesStartY + i * (CATEGORY_PLATE_HEIGHT + PLATE_GAP);
                int slotX = plateX + CATEGORY_PLATE_WIDTH + SLOT_OFFSET;
                int slotY = plateY + 1;
                int btnX = slotX + ITEM_SLOT_SIZE + PLUS_BUTTON_MARGIN;
                int btnY = slotY + (ITEM_SLOT_SIZE - PLUS_BUTTON_DISPLAY_HEIGHT) / 2;
                
                if (mouseX >= btnX && mouseX <= btnX + PLUS_BUTTON_DISPLAY_WIDTH &&
                    mouseY >= btnY && mouseY <= btnY + PLUS_BUTTON_DISPLAY_HEIGHT) {
                    currentPage = i;
                    currentTab = 0;
                    playSound("ui.button.click");
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // Escape
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public void onClose() {
        super.onClose();
        
        // Размораживаем время на сервере
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            PacketHandler.sendToServer(new C2STimeFreezePacket(false));
            TimeFreezeManager.unfreezeClient();
        }
        
        if (onComplete != null && finished) {
            onComplete.run();
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public long getTotalEarnings() {
        return totalEarnings;
    }
    
    /**
     * Простой класс для временных анимаций (звезды, облака и т.д.)
     */
    public static class TemporaryAnimation {
        public float x, y;
        public float motionX, motionY;
        public float alpha = 1.0f;
        public float alphaFade = 0;
        public int color = 0xFFFFFF;
        public int lifetime;
        public int age = 0;
        
        public boolean update(float deltaTime) {
            age += (int)deltaTime;
            x += motionX * deltaTime / 50f;
            y += motionY * deltaTime / 50f;
            alpha -= alphaFade * deltaTime / 50f;
            return age >= lifetime || alpha <= 0;
        }
        
        public void render(GuiGraphics graphics, float partialTick) {
            int a = (int)(alpha * 255);
            graphics.fill((int)x, (int)y, (int)x + 2, (int)y + 2, (a << 24) | color);
        }
    }
}

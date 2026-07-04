package dev.flomik.stardew.client.screen.shipping;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.flomik.stardew.StardewMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Анимированный счетчик денег как в Stardew Valley.
 * Цифры "крутятся" от текущего значения к целевому.
 */
public class MoneyDial {
    
    private static final ResourceLocation DIGITS_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/shipping/digits.png");
    
    // Размеры одной цифры в текстуре (7x11 пикселей, масштаб 4x)
    private static final int DIGIT_WIDTH = 7;
    private static final int DIGIT_HEIGHT = 11;
    private static final float SCALE = 2.0f;
    
    private final int numDigits;
    private final boolean isTotal;
    
    public int currentValue = 0;
    public int previousTargetValue = 0;
    private int animationStartValue = 0;
    private float animationElapsed = 0f;
    
    // Анимация каждой цифры
    private final float[] digitYOffsets;
    private final int[] displayedDigits;
    
    // Скорость анимации
    private static final float SPIN_SPEED = 0.15f;
    
    public MoneyDial(int numDigits, boolean isTotal) {
        this.numDigits = numDigits;
        this.isTotal = isTotal;
        this.digitYOffsets = new float[numDigits];
        this.displayedDigits = new int[numDigits];
    }
    
    /**
     * Обновляет анимацию счетчика.
     * @param targetValue Целевое значение
     * @param deltaTime Время с последнего кадра в миллисекундах
     */
    public void update(int targetValue, float deltaTime) {
        if (currentValue != targetValue) {
            // Постепенно увеличиваем текущее значение
            int diff = targetValue - currentValue;
            int step = Math.max(1, Math.abs(diff) / 20);
            
            if (diff > 0) {
                currentValue = Math.min(currentValue + step, targetValue);
            } else {
                currentValue = Math.max(currentValue - step, targetValue);
            }
        }

        updateDigitAnimation(deltaTime);
    }

    public void update(int targetValue, float deltaTime, float durationMs) {
        if (previousTargetValue != targetValue) {
            previousTargetValue = targetValue;
            animationStartValue = currentValue;
            animationElapsed = 0f;
        }

        if (currentValue != targetValue) {
            animationElapsed = Math.min(animationElapsed + deltaTime, Math.max(1f, durationMs));
            float progress = animationElapsed / Math.max(1f, durationMs);
            currentValue = animationStartValue + Math.round((targetValue - animationStartValue) * progress);
            if (progress >= 1f) {
                currentValue = targetValue;
            }
        }

        updateDigitAnimation(deltaTime);
    }

    private void updateDigitAnimation(float deltaTime) {
        // Обновляем анимацию цифр
        int tempValue = currentValue;
        for (int i = numDigits - 1; i >= 0; i--) {
            int targetDigit = tempValue % 10;
            tempValue /= 10;
            
            // Плавная анимация к целевой цифре
            if (displayedDigits[i] != targetDigit) {
                digitYOffsets[i] += SPIN_SPEED * deltaTime;
                if (digitYOffsets[i] >= 1.0f) {
                    digitYOffsets[i] = 0;
                    displayedDigits[i] = (displayedDigits[i] + 1) % 10;
                }
            } else {
                digitYOffsets[i] = 0;
            }
        }
    }
    
    /**
     * Рисует счетчик денег.
     * @param graphics GuiGraphics
     * @param x X позиция
     * @param y Y позиция
     * @param targetValue Целевое значение для отображения
     */
    public void draw(GuiGraphics graphics, int x, int y, int targetValue) {
        Font font = Minecraft.getInstance().font;
        
        // Форматируем число с разделителями
        String valueStr = formatWithCommas(currentValue);
        
        // Рисуем как текст (можно заменить на текстуру цифр)
        int color = isTotal ? 0xFFD700 : 0xFFFFFF; // Золотой для Total, белый для остальных
        
        // Рисуем справа налево
        int drawX = x + (int)(numDigits * DIGIT_WIDTH * SCALE);
        
        // Простой текстовый вариант (можно заменить на спрайты)
        graphics.drawString(font, valueStr, x, y, color, true);
    }
    
    /**
     * Рисует счетчик с использованием спрайтов цифр.
     */
    public void drawWithSprites(GuiGraphics graphics, int x, int y, int targetValue) {
        int tempValue = currentValue;
        int drawX = x + (int)((numDigits - 1) * DIGIT_WIDTH * SCALE);
        
        for (int i = numDigits - 1; i >= 0; i--) {
            int digit = tempValue % 10;
            tempValue /= 10;
            
            // Если значение 0 и это ведущий ноль (кроме последней цифры) - не рисуем
            if (tempValue == 0 && digit == 0 && i < numDigits - 1 && currentValue < Math.pow(10, numDigits - 1 - i)) {
                drawX -= (int)(DIGIT_WIDTH * SCALE);
                continue;
            }
            
            // Рисуем цифру с учетом анимации
            float yOffset = digitYOffsets[i] * DIGIT_HEIGHT * SCALE;
            
            RenderSystem.setShaderTexture(0, DIGITS_TEXTURE);
            
            // Текущая цифра
            graphics.blit(DIGITS_TEXTURE, 
                drawX, (int)(y - yOffset),
                digit * DIGIT_WIDTH, 0,
                DIGIT_WIDTH, DIGIT_HEIGHT,
                70, 11); // 10 цифр по 7 пикселей = 70 ширина
            
            // Следующая цифра (для анимации перехода)
            if (digitYOffsets[i] > 0) {
                int nextDigit = (digit + 1) % 10;
                graphics.blit(DIGITS_TEXTURE,
                    drawX, (int)(y + DIGIT_HEIGHT * SCALE - yOffset),
                    nextDigit * DIGIT_WIDTH, 0,
                    DIGIT_WIDTH, DIGIT_HEIGHT,
                    70, 11);
            }
            
            drawX -= (int)(DIGIT_WIDTH * SCALE);
        }
    }
    
    /**
     * Форматирует число с запятыми как разделителями тысяч.
     */
    private String formatWithCommas(int value) {
        if (value < 1000) return String.valueOf(value);
        
        StringBuilder sb = new StringBuilder();
        String numStr = String.valueOf(value);
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
    
    /**
     * Сбрасывает счетчик для новой анимации.
     */
    public void reset() {
        currentValue = 0;
        previousTargetValue = 0;
        animationStartValue = 0;
        animationElapsed = 0f;
        for (int i = 0; i < numDigits; i++) {
            digitYOffsets[i] = 0;
            displayedDigits[i] = 0;
        }
    }
}

package dev.flomik.stardew.client.screen.shipping;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

/**
 * Полная реализация TemporaryAnimatedSprite из Stardew Valley.
 * Поддерживает:
 * - Кадровую анимацию из атласа
 * - Движение и ускорение
 * - Периодическое движение (синусоида)
 * - Тряска
 * - Вращение
 * - Масштабирование
 * - Прозрачность и затухание
 * - Зеркалирование
 * - Зацикливание
 */
public class TemporaryAnimatedSprite {

    // Текстура
    private final ResourceLocation texture;
    private int sourceRectX;
    private int sourceRectY;
    private final int sourceRectWidth;
    private final int sourceRectHeight;
    
    // Позиция
    public float x;
    public float y;
    public Vector2f motion = new Vector2f(0, 0);
    public Vector2f acceleration = new Vector2f(0, 0);
    
    // Анимация
    private final float animationInterval; // ms между кадрами
    private final int animationLength; // количество кадров
    private int currentFrame = 0;
    private float timer = 0;
    private int loops; // сколько раз повторить (-1 = бесконечно)
    private int currentLoop = 0;
    
    // Визуальные эффекты
    public float scale = 1f;
    public float scaleChange = 0f;
    public float rotation = 0f;
    public float rotationChange = 0f;
    public float alpha = 1f;
    public float alphaFade = 0f;
    public int color = 0xFFFFFF;
    public float colorAlpha = 1f;
    public boolean flipped = false;
    public float layerDepth = 0.01f;
    
    // Периодическое движение
    public boolean yPeriodic = false;
    public float yPeriodicLoopTime = 0;
    public float yPeriodicRange = 0;
    private float yPeriodicTimer = 0;
    private float baseY;
    
    // Тряска
    public float shakeIntensity = 0f;
    
    // Задержка перед стартом
    public int delayBeforeAnimationStart = 0;
    
    // Локальные координаты (относительно экрана, не мира)
    public boolean local = false;
    
    // Флаг мерцания
    public boolean flicker = false;
    private boolean flickerOn = true;
    
    // Завершён ли спрайт
    private boolean finished = false;

    /**
     * Полный конструктор.
     */
    public TemporaryAnimatedSprite(
            ResourceLocation texture,
            int sourceX, int sourceY, int sourceWidth, int sourceHeight,
            float animationInterval, int animationLength, int loops,
            float x, float y,
            boolean flicker, boolean flipped,
            float layerDepth, float alphaFade,
            int color, float colorAlpha,
            float scale, float scaleChange,
            float rotation, float rotationChange,
            boolean local
    ) {
        this.texture = texture;
        this.sourceRectX = sourceX;
        this.sourceRectY = sourceY;
        this.sourceRectWidth = sourceWidth;
        this.sourceRectHeight = sourceHeight;
        this.animationInterval = animationInterval;
        this.animationLength = animationLength;
        this.loops = loops;
        this.x = x;
        this.y = y;
        this.baseY = y;
        this.flicker = flicker;
        this.flipped = flipped;
        this.layerDepth = layerDepth;
        this.alphaFade = alphaFade;
        this.color = color;
        this.colorAlpha = colorAlpha;
        this.scale = scale;
        this.scaleChange = scaleChange;
        this.rotation = rotation;
        this.rotationChange = rotationChange;
        this.local = local;
    }

    /**
     * Упрощённый конструктор для простых спрайтов.
     */
    public TemporaryAnimatedSprite(
            ResourceLocation texture,
            int sourceX, int sourceY, int sourceWidth, int sourceHeight,
            float animationInterval, int animationLength, int loops,
            float x, float y
    ) {
        this(texture, sourceX, sourceY, sourceWidth, sourceHeight,
                animationInterval, animationLength, loops, x, y,
                false, false, 0.01f, 0f,
                0xFFFFFF, 1f, 4f, 0f, 0f, 0f, true);
    }

    /**
     * Конструктор для статичного спрайта (1 кадр).
     */
    public TemporaryAnimatedSprite(ResourceLocation texture, int sourceX, int sourceY, 
                                    int sourceWidth, int sourceHeight, float x, float y) {
        this(texture, sourceX, sourceY, sourceWidth, sourceHeight,
                9999f, 1, 10000, x, y);
    }

    /**
     * Обновляет спрайт. Возвращает true если спрайт завершён и должен быть удалён.
     */
    public boolean update(float deltaTime) {
        if (finished) return true;

        // Задержка перед стартом
        if (delayBeforeAnimationStart > 0) {
            delayBeforeAnimationStart -= (int) deltaTime;
            return false;
        }

        // Движение
        x += motion.x * deltaTime / 50f;
        y += motion.y * deltaTime / 50f;
        motion.x += acceleration.x * deltaTime / 50f;
        motion.y += acceleration.y * deltaTime / 50f;

        // Периодическое движение по Y
        if (yPeriodic && yPeriodicLoopTime > 0) {
            yPeriodicTimer += deltaTime;
            float phase = (yPeriodicTimer / yPeriodicLoopTime) * (float) Math.PI * 2f;
            y = baseY + (float) Math.sin(phase) * yPeriodicRange;
        }

        // Масштаб
        scale += scaleChange * deltaTime / 50f;
        if (scale <= 0) {
            finished = true;
            return true;
        }

        // Вращение
        rotation += rotationChange * deltaTime / 50f;

        // Прозрачность
        alpha -= alphaFade * deltaTime / 50f;
        if (alpha <= 0) {
            finished = true;
            return true;
        }

        // Анимация кадров
        timer += deltaTime;
        if (timer >= animationInterval) {
            timer = 0;
            currentFrame++;
            
            // Мерцание
            if (flicker) {
                flickerOn = !flickerOn;
            }

            if (currentFrame >= animationLength) {
                currentFrame = 0;
                currentLoop++;
                if (loops > 0 && currentLoop >= loops) {
                    finished = true;
                    return true;
                }
            }
        }

        // Проверка выхода за экран
        if (x < -500 || x > 3000 || y < -500 || y > 2000) {
            finished = true;
            return true;
        }

        return false;
    }

    /**
     * Рисует спрайт.
     */
    public void draw(GuiGraphics graphics, boolean localPosition) {
        if (finished || (flicker && !flickerOn)) return;
        if (delayBeforeAnimationStart > 0) return;

        float drawX = x;
        float drawY = y;

        // Тряска
        if (shakeIntensity > 0) {
            drawX += (float) (Math.random() * 2 - 1) * shakeIntensity;
            drawY += (float) (Math.random() * 2 - 1) * shakeIntensity;
        }

        graphics.pose().pushPose();

        // Позиционирование
        graphics.pose().translate(drawX, drawY, 0);

        // Вращение (вокруг центра)
        if (rotation != 0) {
            float centerX = (sourceRectWidth * scale) / 2f;
            float centerY = (sourceRectHeight * scale) / 2f;
            graphics.pose().translate(centerX, centerY, 0);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotation(rotation));
            graphics.pose().translate(-centerX, -centerY, 0);
        }

        // Масштаб
        graphics.pose().scale(scale, scale, 1);

        // Зеркалирование
        if (flipped) {
            graphics.pose().translate(sourceRectWidth, 0, 0);
            graphics.pose().scale(-1, 1, 1);
        }

        // Цвет и альфа
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, g, b, alpha * colorAlpha);

        // Рассчитываем позицию кадра в атласе
        int frameX = sourceRectX + currentFrame * sourceRectWidth;
        int frameY = sourceRectY;

        // Рисуем
        graphics.blit(texture, 0, 0, frameX, frameY, sourceRectWidth, sourceRectHeight);

        // Сброс цвета
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        graphics.pose().popPose();
    }

    /**
     * Рисует спрайт (упрощённая версия).
     */
    public void draw(GuiGraphics graphics) {
        draw(graphics, local);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished() {
        this.finished = true;
    }

    // === Builder-style методы для удобства ===

    public TemporaryAnimatedSprite withMotion(float mx, float my) {
        this.motion = new Vector2f(mx, my);
        return this;
    }

    public TemporaryAnimatedSprite withAcceleration(float ax, float ay) {
        this.acceleration = new Vector2f(ax, ay);
        return this;
    }

    public TemporaryAnimatedSprite withScale(float scale) {
        this.scale = scale;
        return this;
    }

    public TemporaryAnimatedSprite withScaleChange(float change) {
        this.scaleChange = change;
        return this;
    }

    public TemporaryAnimatedSprite withRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public TemporaryAnimatedSprite withRotationChange(float change) {
        this.rotationChange = change;
        return this;
    }

    public TemporaryAnimatedSprite withAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public TemporaryAnimatedSprite withAlphaFade(float fade) {
        this.alphaFade = fade;
        return this;
    }

    public TemporaryAnimatedSprite withColor(int color) {
        this.color = color;
        return this;
    }

    public TemporaryAnimatedSprite withColorAlpha(float alpha) {
        this.colorAlpha = alpha;
        return this;
    }

    public TemporaryAnimatedSprite withDelay(int delay) {
        this.delayBeforeAnimationStart = delay;
        return this;
    }

    public TemporaryAnimatedSprite withYPeriodic(float loopTime, float range) {
        this.yPeriodic = true;
        this.yPeriodicLoopTime = loopTime;
        this.yPeriodicRange = range;
        return this;
    }

    public TemporaryAnimatedSprite withShake(float intensity) {
        this.shakeIntensity = intensity;
        return this;
    }

    public TemporaryAnimatedSprite flipped() {
        this.flipped = true;
        return this;
    }

    public TemporaryAnimatedSprite local() {
        this.local = true;
        return this;
    }
}


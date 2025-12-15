package dev.flomik.stardew.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.ClientStardewData;
import dev.flomik.stardew.common.module.time.DayOfWeek; // Не забудь импорт
import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.StardewTimeUtils;
import dev.flomik.stardew.common.module.time.Weather;
import dev.flomik.stardew.core.config.StardewConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class StardewClockOverlay {

    private static final ResourceLocation BG_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/clock/clock_bg.png");
    private static final ResourceLocation HAND_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/clock/hand.png");
    private static final ResourceLocation WEATHER_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/clock/weather.png");
    private static final ResourceLocation SEASONS_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/clock/seasons.png");
    private static final ResourceLocation DIGITS_TEXTURE = new ResourceLocation(StardewMod.MODID, "textures/gui/clock/digits.png"); // Твои цифры 5x8

    private static final int BG_WIDTH = 71;
    private static final int BG_HEIGHT = 57;
    private static final int ICON_WIDTH = 12;
    private static final int ICON_HEIGHT = 8;
    private static final int HAND_WIDTH = 7;
    private static final int HAND_HEIGHT = 19;

    private static final int DIGIT_WIDTH = 5;
    private static final int DIGIT_HEIGHT = 8;

    private static final int HAND_ANCHOR_X = 22;
    private static final int HAND_ANCHOR_Y = 20;
    private static final int HAND_PIVOT_X = 3;
    private static final int HAND_PIVOT_Y = 17;
    private static final int WEATHER_X = 29;
    private static final int WEATHER_Y = 16;
    private static final int SEASON_X = 53;
    private static final int SEASON_Y = 16;

    private static final int DATE_TEXT_X = 34; // Где начинается дата
    private static final int DATE_TEXT_Y = 7; // Y координата для "Mon. 1"

    private static final int TIME_TEXT_X = 31; // Где начинается время
    private static final int TIME_TEXT_Y = 29; // Y для "12:00 pm"

    private static final int MONEY_X_RIGHT = 64;
    private static final int MONEY_Y = 46;

    private static final int ST_DAY_LENGTH = 20400;

    public static final IGuiOverlay HUD_CLOCK = (ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null) return;

        // Получаем масштаб из конфига
        float scale = StardewConfig.CLIENT.clockScale.get().floatValue();

        float scaledWidth = BG_WIDTH * scale;
        float x = screenWidth - scaledWidth - 10;
        float y = 10;

        long gameTime = mc.level.getDayTime();
        Season season = ClientStardewData.getSeason();
        Weather weather = ClientStardewData.getWeather();
        int day = ClientStardewData.getDay();

        DayOfWeek dayOfWeek = DayOfWeek.fromIndex((season.ordinal() * 28 + (day - 1)));

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);

        // 1. Фон
        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        graphics.blit(BG_TEXTURE, 0, 0, 0, 0, BG_WIDTH, BG_HEIGHT, BG_WIDTH, BG_HEIGHT);

        // 2. Иконки
        renderWeather(graphics, weather, season);
        renderSeason(graphics, season);

        // 3. Стрелка
        renderHand(graphics, gameTime);

        // 4. Текст (Дата и Время) - Обычный шрифт
        renderText(graphics, mc, gameTime, day, dayOfWeek);

        // 5. Деньги (Текстурный шрифт)
        long currentMoney = ClientStardewData.getMoney(); // Заглушка, потом брать из Capability
        renderMoney(graphics, currentMoney);

        graphics.pose().popPose();
    };

    private static void renderText(GuiGraphics graphics, Minecraft mc, long gameTime, int day, DayOfWeek dayOfWeek) {
        int color = 0x521212;

        String dayName = dayOfWeek.name().substring(0, 1).toUpperCase() + dayOfWeek.name().substring(1, 3).toLowerCase();
        String dateStr = dayName + ". " + day;

        graphics.pose().pushPose();
        graphics.pose().translate(DATE_TEXT_X, DATE_TEXT_Y, 0);
        float dateScale = 0.7f;
        graphics.pose().scale(dateScale, dateScale, 1f);
        graphics.drawString(mc.font, dateStr, 0, 0, color, false);
        graphics.pose().popPose();

        int hour = StardewTimeUtils.getHour(gameTime);
        int minute = StardewTimeUtils.getMinute(gameTime);

        minute = (minute / 10) * 10;

        String ampm = (hour >= 12 && hour < 24) ? "pm" : "am";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        if (hour >= 24) {
            displayHour = hour - 24;
            if (displayHour == 0) displayHour = 12;
            ampm = "am";
        }

        String timeStr = String.format("%d:%02d %s", displayHour, minute, ampm);

        graphics.pose().pushPose();
        graphics.pose().translate(TIME_TEXT_X, TIME_TEXT_Y, 0);
        float timeScale = 0.8f;
        graphics.pose().scale(timeScale, timeScale, 1f);
        // Можно центровать, если известно ширина поля (42px)
        int timeW = mc.font.width(timeStr);
        graphics.drawString(mc.font, timeStr, (40 - timeW) / 2, 0, color, false);
        graphics.pose().popPose();
    }

    private static void renderMoney(GuiGraphics graphics, long money) {
        String moneyStr = String.valueOf(money);
        int len = moneyStr.length();
        int maxLen = 8;

        for (int i = 0; i < len; i++) {
            if (i >= maxLen) break;

            char c = moneyStr.charAt(len - 1 - i);
            int digit = c - '0';

            int x = MONEY_X_RIGHT - DIGIT_WIDTH - (i * (DIGIT_WIDTH + 1));
            int y = MONEY_Y;

            int vIndex = 9 - digit;
            int v = vIndex * DIGIT_HEIGHT;
            int u = 0;

            graphics.blit(DIGITS_TEXTURE, x, y, u, v, DIGIT_WIDTH, DIGIT_HEIGHT, 5, 80);
        }
    }

    private static void renderWeather(GuiGraphics graphics, Weather weather, Season season) {
        int index = 0;
        switch (weather) {
            case SUNNY -> index = 0;
            case RAIN -> index = 1;
            case STORM -> index = 2;
            case WIND -> index = (season == Season.FALL) ? 4 : 3;
            case SNOW -> index = 5;
            case FEST_OVERRIDE -> index = 6;
            case WEDDING_OVERRIDE -> index = 7;
        }
        int u = index * ICON_WIDTH;
        graphics.blit(WEATHER_TEXTURE, WEATHER_X, WEATHER_Y, u, 0, ICON_WIDTH, ICON_HEIGHT, 96, 8);
    }

    private static void renderSeason(GuiGraphics graphics, Season season) {
        int index = season.ordinal();
        int u = index * ICON_WIDTH;
        graphics.blit(SEASONS_TEXTURE, SEASON_X, SEASON_Y, u, 0, ICON_WIDTH, ICON_HEIGHT, 48, 8);
    }

    private static void renderHand(GuiGraphics graphics, long gameTime) {
        long dayTime = gameTime % 24000;
        float progress = (float) dayTime / (float) ST_DAY_LENGTH;
        if (progress > 1.0f) progress = 1.0f;
        float angle = 180.0f + (progress * 180.0f);

        graphics.pose().pushPose();
        graphics.pose().translate(HAND_ANCHOR_X, HAND_ANCHOR_Y, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        graphics.pose().translate(-HAND_PIVOT_X, -HAND_PIVOT_Y, 0);
        graphics.blit(HAND_TEXTURE, 0, 0, 0, 0, HAND_WIDTH, HAND_HEIGHT, HAND_WIDTH, HAND_HEIGHT);
        graphics.pose().popPose();
    }
}

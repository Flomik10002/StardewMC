package dev.flomik.stardew.client;

import dev.flomik.stardew.core.time.Season;
import net.minecraft.client.Minecraft;

public class ClientSeasonManager {
    private static Season currentSeason = Season.SPRING;

    public static void setSeason(Season season) {
        if (currentSeason != season) {
            currentSeason = season;
            Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    public static Season getSeason() {
        return currentSeason;
    }
}
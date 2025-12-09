package dev.flomik.stardew.core.player;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.network.PacketHandler;
import dev.flomik.stardew.core.network.S2CSeasonSync;
import dev.flomik.stardew.core.time.Season;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StardewDateData dateData = StardewDateData.get(player.serverLevel());
            Season currentSeason = dateData.getSeason();

            PacketHandler.sendToPlayer(new S2CSeasonSync(currentSeason), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            StardewDateData dateData = StardewDateData.get(player.serverLevel());
            PacketHandler.sendToPlayer(new S2CSeasonSync(dateData.getSeason()), player);
        }
    }
}

package dev.flomik.stardew;

import dev.flomik.stardew.core.crop.logic.GrowthSystem;
import dev.flomik.stardew.core.crop.logic.MorningPass;
import dev.flomik.stardew.core.network.PacketHandler;
import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.command.SeasonArgument;
import dev.flomik.stardew.core.config.StardewConfig;
import dev.flomik.stardew.core.registry.blockentity.ModBlockEntities;
import dev.flomik.stardew.core.registry.item.ModCreativeModeTabs;
import dev.flomik.stardew.core.registry.item.ModItems;
import dev.flomik.stardew.core.registry.menu.ModMenuTypes;
import dev.flomik.stardew.core.registry.sound.ModSounds;
import dev.flomik.stardew.core.time.ScheduleManager;
import dev.flomik.stardew.core.time.StardewClock;
import dev.flomik.stardew.core.time.StardewDateData;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(StardewMod.MODID)
public class StardewMod {

    public static final String MODID = "stardew";
    public static final Logger LOGGER = LogManager.getLogger();

    public StardewMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModCreativeModeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        ArgumentTypeInfos.registerByClass(
                SeasonArgument.class,
                SingletonArgumentInfo.contextFree(SeasonArgument::season)
        );

        modEventBus.addListener(this::commonSetup);

        StardewConfig.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.init();
        });

        event.enqueueWork(() -> {
            dev.flomik.stardew.core.crop.CropRegistry.bootstrapVanillaLike(StardewMod.MODID);

            ScheduleManager.register(6, 0, () -> {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                for (ServerLevel sl : server.getAllLevels()) {
                    if (!sl.dimension().location().toString().equals("minecraft:overworld")) {
                        continue;
                    }
                    
                    StardewDateData dateData = StardewDateData.get(sl);
                    
                    dev.flomik.stardew.core.time.WeatherSystem.applyWeatherToWorld(sl, dateData.getTodayWeather());

                    MorningPass.run(sl);
                    GrowthSystem.run(sl);
                }
            });
        });
    }
}
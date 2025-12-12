package dev.flomik.stardew;

import dev.flomik.stardew.common.module.farming.crop.CropRegistry;
import dev.flomik.stardew.common.module.time.WeatherSystem;
import dev.flomik.stardew.common.registry.*;
import dev.flomik.stardew.common.module.farming.crop.logic.GrowthSystem;
import dev.flomik.stardew.common.module.farming.crop.logic.MorningPass;
import dev.flomik.stardew.core.network.PacketHandler;
import dev.flomik.stardew.common.admin.SeasonArgument;
import dev.flomik.stardew.common.module.time.ScheduleManager;
import dev.flomik.stardew.common.module.time.StardewDateData;
import dev.flomik.stardew.datagen.StardewItemModels;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

        StardewRegistry.init(modEventBus);

        ModTabs.load();
        ModBlocks.load();
        ModItems.load();
        ModSounds.load();
        ModMenuTypes.load();

        ArgumentTypeInfos.registerByClass(
                SeasonArgument.class,
                SingletonArgumentInfo.contextFree(SeasonArgument::season)
        );

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(StardewMod::onGatherData);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new StardewItemModels(output, existingFileHelper));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.init();
        });

        event.enqueueWork(() -> {
            CropRegistry.bootstrapVanillaLike(StardewMod.MODID);

            ScheduleManager.register(6, 0, () -> {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                for (ServerLevel sl : server.getAllLevels()) {
                    if (!sl.dimension().location().toString().equals("minecraft:overworld")) {
                        continue;
                    }
                    
                    StardewDateData dateData = StardewDateData.get(sl);
                    
                    WeatherSystem.applyWeatherToWorld(sl, dateData.getTodayWeather());

                    MorningPass.run(sl);
                    GrowthSystem.run(sl);
                }
            });
        });
    }
}
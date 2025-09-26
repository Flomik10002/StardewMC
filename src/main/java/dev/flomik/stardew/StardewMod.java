package dev.flomik.stardew;

import dev.flomik.stardew.core.registry.block.ModBlocks;
import dev.flomik.stardew.core.command.SeasonArgument;
import dev.flomik.stardew.core.config.StardewConfig;
import dev.flomik.stardew.core.registry.blockentity.ModBlockEntities;
import dev.flomik.stardew.core.registry.item.ModCreativeModeTabs;
import dev.flomik.stardew.core.registry.item.ModItems;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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


        ArgumentTypeInfos.registerByClass(
                SeasonArgument.class,
                SingletonArgumentInfo.contextFree(SeasonArgument::season)
        );

        StardewConfig.register();
    }
}
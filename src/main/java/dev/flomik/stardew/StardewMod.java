package dev.flomik.stardew;


import dev.flomik.stardew.core.config.StardewConfig;
import dev.flomik.stardew.core.registry.ModRegistry;
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
        ModRegistry.register(modEventBus);
        StardewConfig.register();
    }
}
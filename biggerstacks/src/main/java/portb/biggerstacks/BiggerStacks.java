/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portb.biggerstacks.config.ClientConfig;
import portb.biggerstacks.config.ServerConfig;
import portb.biggerstacks.event.ClientEvents;
import portb.biggerstacks.event.CommonForgeEvents;
import portb.biggerstacks.event.CommonModEvents;
import portb.biggerstacks.event.ServerEvents;
import portb.configlib.ConfigLib;
import portb.configlib.IMCAPI;
import portb.slw.MyLoggerFactory;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Constants.MOD_ID)
public class BiggerStacks
{
    public final static Logger LOGGER = LogUtils.getLogger();
    
    public BiggerStacks()
    {
        if (FMLEnvironment.dist.isClient())
            MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        
        MinecraftForge.EVENT_BUS.register(ServerEvents.class);
        MinecraftForge.EVENT_BUS.register(CommonModEvents.class);
        MinecraftForge.EVENT_BUS.register(CommonForgeEvents.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        
        ConfigLib.LOGGER = MyLoggerFactory.createMyLogger(LoggerFactory.getLogger(ConfigLib.class));
        
        registerConfigs();
    }
    
    private static void registerConfigs()
    {
        ModLoadingContext context = ModLoadingContext.get();
        
        if (FMLEnvironment.dist.isClient())
        {
            context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, Constants.MOD_ID + "-client.toml");
            context.registerConfig(ModConfig.Type.CLIENT,
                                   ServerConfig.LOCAL_INSTANCE.SPEC,
                                   Constants.MOD_ID + "-local.toml"
            );
        }
        
        context.registerConfig(ModConfig.Type.SERVER,
                               ServerConfig.SERVER_INSTANCE.SPEC,
                               Constants.MOD_ID + "-server.toml"
        );
    }
    
    void processIMC(final InterModProcessEvent event)
    {
        event.getIMCStream().forEach(imcMessage -> IMCAPI.addIMCRuleSupplier(imcMessage.senderModId(),
                                                                             imcMessage.messageSupplier()
        ));
    }
}

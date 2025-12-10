/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import portb.biggerstacks.Constants;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents
{
    private static ServerLifecycleHandler handler;
    
    /**
     * Creates a ServerLifecycleHandler that manages config updates
     */
    @SubscribeEvent
    public static void serverStarting(ServerAboutToStartEvent event)
    {
        handler = new ServerLifecycleHandler();
        
        MinecraftForge.EVENT_BUS.register(handler);
    }
    
    /**
     * Unregisters the handler
     */
    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event)
    {
        MinecraftForge.EVENT_BUS.unregister(handler);
        
        handler.ensureStopped();
    }
    
}

/*
 * Copyright (c) PORTB 2025
 *
 * Licensed under GNU LGPL v3
 * https://www.gnu.org/licenses/lgpl-3.0.txt
 */

package portb.biggerstacks.event;

import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import portb.biggerstacks.config.StackSizeRules;
import portb.biggerstacks.net.ClientboundRulesUpdatePacket;
import portb.biggerstacks.net.PacketHandler;
import portb.configlib.ConfigFileWatcher;
import portb.configlib.ConfigLib;
import portb.configlib.xml.RuleSet;

import static portb.biggerstacks.Constants.RULESET_FILE;

public class ServerLifecycleHandler
{
    private final ConfigFileWatcher watcher = new ConfigFileWatcher(RULESET_FILE);
    private       boolean           stopped = false;
    
    /**
     * Parses the config file and starts the file watcher
     * Called when server starts (ServerAboutToStart)
     */
    public ServerLifecycleHandler()
    {
        //read the ruleset file
        StackSizeRules.setRuleSet(ConfigLib.readRuleset(RULESET_FILE));
        
        //configure and start the watcher
        watcher.setOnUpdateAction(this::notifyClientsOfConfigChange);
        watcher.start();
    }
    
    private void notifyClientsOfConfigChange(RuleSet ruleSet)
    {
        //update our ruleset
        StackSizeRules.setRuleSet(ruleSet);
        
        //send new ruleset to clients
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundRulesUpdatePacket(ruleSet));
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void serverStopping(ServerStoppingEvent event)
    {
        if (!stopped)
        {
            //stop the watcher
            watcher.stop();
            stopped = true;
        }
    }
    
    public void ensureStopped()
    {
        if (!stopped)
        {
            watcher.stop();
            stopped = true;
        }
    }
    
}

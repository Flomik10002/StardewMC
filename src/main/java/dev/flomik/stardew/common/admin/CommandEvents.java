package dev.flomik.stardew.common.admin;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        DateCommand.register(event.getDispatcher());
        DateTestCommand.register(event.getDispatcher());
        RainTestCommand.register(event.getDispatcher());
        SoilTestCommand.register(event.getDispatcher());
        RandomizeDirtCommand.register(event.getDispatcher());
        EnergyCommand.register(event.getDispatcher());
        WalletCommand.register(event.getDispatcher());
    }
}

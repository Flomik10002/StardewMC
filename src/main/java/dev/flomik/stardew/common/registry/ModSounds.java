package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.StardewMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final RegistryObject<SoundEvent> HOE_TILL = register("hoe_till");
    public static final RegistryObject<SoundEvent> WATERING_CAN_USE = register("watering_can_use");

    private static RegistryObject<SoundEvent> register(String name) {
        return StardewRegistry.SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StardewMod.MODID, name)));
    }

    public static void load() {}
}


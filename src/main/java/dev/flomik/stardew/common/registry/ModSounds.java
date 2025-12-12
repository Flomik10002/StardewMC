package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.StardewMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final RegistryObject<SoundEvent> HOE_TILL = register("hoe_till");
    public static final RegistryObject<SoundEvent> WATERING_CAN_USE = register("watering_can_use");
    public static final RegistryObject<SoundEvent> CHEST_OPEN = register("open_chest");
    public static final RegistryObject<SoundEvent> CHEST_CLOSE = register("close_chest");
    public static final RegistryObject<SoundEvent> TOOL_CHARGE = register("tool_charge");
    public static final RegistryObject<SoundEvent> MACHINE_INSERT_1 = register("machine_insert_1");
    public static final RegistryObject<SoundEvent> MACHINE_INSERT_2 = register("machine_insert_2");
    public static final RegistryObject<SoundEvent> MACHINE_COLLECT = register("machine_collect");

    private static RegistryObject<SoundEvent> register(String name) {
        return StardewRegistry.SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(StardewMod.MODID, name)));
    }

    public static void load() {}
}


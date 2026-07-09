package dev.flomik.stardew.common.module.character.event;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.common.module.character.capability.CharacterProfileProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StardewMod.MODID)
public class CharacterProfileCapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(CharacterProfileProvider.CHARACTER_PROFILE_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(StardewMod.MODID, "character_profile"),
                        new CharacterProfileProvider(new CharacterProfile()));
            }
        }
    }

    // Персонаж со всей внешностью и именем сохраняется между смертями — это
    // не предмет/энергия, которые логично терять/восстанавливать.
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(CharacterProfileProvider.CHARACTER_PROFILE_CAPABILITY).ifPresent(oldProfile -> {
            event.getEntity().getCapability(CharacterProfileProvider.CHARACTER_PROFILE_CAPABILITY).ifPresent(newProfile -> {
                var tag = new net.minecraft.nbt.CompoundTag();
                oldProfile.saveNBT(tag);
                newProfile.loadFrom(tag);
            });
        });
    }
}

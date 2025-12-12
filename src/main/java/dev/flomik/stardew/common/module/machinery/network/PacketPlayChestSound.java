package dev.flomik.stardew.common.module.machinery.network;

import dev.flomik.stardew.common.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketPlayChestSound {
    private static final Map<BlockPos, SimpleSoundInstance> PLAYING_SOUNDS = new HashMap<>();

    private final BlockPos pos;
    private final boolean open;

    public PacketPlayChestSound(BlockPos pos, boolean open) {
        this.pos = pos;
        this.open = open;
    }

    public static void encode(PacketPlayChestSound msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.open);
    }

    public static PacketPlayChestSound decode(FriendlyByteBuf buf) {
        return new PacketPlayChestSound(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(PacketPlayChestSound msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                if (PLAYING_SOUNDS.containsKey(msg.pos)) {
                    SimpleSoundInstance existing = PLAYING_SOUNDS.get(msg.pos);
                    mc.getSoundManager().stop(existing);
                    PLAYING_SOUNDS.remove(msg.pos);
                }

                SoundEvent soundEvent = msg.open ? ModSounds.CHEST_OPEN.get() : ModSounds.CHEST_CLOSE.get();
                float pitch = mc.level.random.nextFloat() * 0.1F + 0.9F;
                
                // Using standard constructor for positioned sound
                SimpleSoundInstance instance = new SimpleSoundInstance(
                        soundEvent,
                        SoundSource.BLOCKS,
                        0.5F,
                        pitch,
                        net.minecraft.util.RandomSource.create(),
                        msg.pos
                );
                
                mc.getSoundManager().play(instance);
                PLAYING_SOUNDS.put(msg.pos, instance);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

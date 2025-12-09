package dev.flomik.stardew.core.network;

import dev.flomik.stardew.core.registry.block.craftables.BlockChest;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeChestVariant {
    private final BlockPos pos;
    private final int variant;

    public PacketChangeChestVariant(BlockPos pos, int variant) {
        this.pos = pos;
        this.variant = variant;
    }

    public static void encode(PacketChangeChestVariant msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.variant);
    }

    public static PacketChangeChestVariant decode(FriendlyByteBuf buf) {
        return new PacketChangeChestVariant(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(PacketChangeChestVariant msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel level = ctx.get().getSender().serverLevel();
            // Проверяем, прогружен ли чанк
            if (level.hasChunkAt(msg.pos)) {
                BlockState state = level.getBlockState(msg.pos);
                if (state.getBlock() instanceof BlockChest) {
                    // Меняем BlockState
                    level.setBlock(msg.pos, state.setValue(BlockChest.VARIANT, msg.variant), 3);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
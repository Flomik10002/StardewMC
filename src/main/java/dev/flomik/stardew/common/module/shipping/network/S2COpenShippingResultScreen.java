package dev.flomik.stardew.common.module.shipping.network;

import dev.flomik.stardew.client.screen.shipping.ShippingResultScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Пакет для открытия экрана результатов продаж на клиенте.
 */
public class S2COpenShippingResultScreen {

    private final List<ItemStack> shippedItems;
    private final String yesterdayDate;
    private final String todayDate;
    private final long totalEarnings;

    public S2COpenShippingResultScreen(List<ItemStack> shippedItems, String yesterdayDate, String todayDate, long totalEarnings) {
        this.shippedItems = shippedItems;
        this.yesterdayDate = yesterdayDate;
        this.todayDate = todayDate;
        this.totalEarnings = totalEarnings;
    }

    public S2COpenShippingResultScreen(FriendlyByteBuf buf) {
        this.yesterdayDate = buf.readUtf();
        this.todayDate = buf.readUtf();
        this.totalEarnings = buf.readLong();
        
        int count = buf.readVarInt();
        this.shippedItems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.shippedItems.add(buf.readItem());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(yesterdayDate);
        buf.writeUtf(todayDate);
        buf.writeLong(totalEarnings);
        
        buf.writeVarInt(shippedItems.size());
        for (ItemStack stack : shippedItems) {
            buf.writeItem(stack);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient();
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        
        // Открываем экран результатов
        ShippingResultScreen screen = new ShippingResultScreen(
                shippedItems,
                yesterdayDate,
                todayDate,
                () -> {
                    // Callback при закрытии экрана - ничего особенного не делаем,
                    // сервер уже обработал переход дня
                }
        );
        
        mc.setScreen(screen);
    }
}


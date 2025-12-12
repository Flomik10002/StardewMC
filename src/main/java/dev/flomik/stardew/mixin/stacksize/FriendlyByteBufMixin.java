package dev.flomik.stardew.mixin.stacksize;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Модифицирует способ хранения количества предметов в байтовом буфере.
 * Ванилла хранит количество как byte (до 127), эти миксины меняют на int (до 2 млрд).
 * В коде количество обрабатывается как int, поэтому это работает корректно.
 */
@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin {
    
    /**
     * Записывает количество предметов как int вместо byte.
     */
    @Redirect(method = "writeItemStack",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/network/FriendlyByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
    private ByteBuf writeBiggerStackCount(FriendlyByteBuf instance, int count) {
        return instance.writeInt(count);
    }
    
    /**
     * Мы не можем изменить возвращаемый тип метода, поэтому возвращаем заглушку
     * и модифицируем переменную позже через {@link #readStackItemCount}.
     */
    @Redirect(method = "readItem",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readByte()B"))
    private byte doNothing(FriendlyByteBuf instance) {
        return 0; // заглушка, реальное чтение в readStackItemCount
    }
    
    /**
     * Читает количество предметов как int вместо byte.
     */
    @ModifyVariable(method = "readItem", at = @At("STORE"), ordinal = 0)
    private int readStackItemCount(int value) {
        return ((FriendlyByteBuf) (Object) this).readInt();
    }
}


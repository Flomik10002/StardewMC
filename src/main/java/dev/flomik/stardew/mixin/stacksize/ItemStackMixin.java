package dev.flomik.stardew.mixin.stacksize;

import dev.flomik.stardew.common.stacksize.StackSizeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Модифицирует ItemStack для поддержки стаков >64
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {
    
    /**
     * Увеличивает максимальный размер стака
     */
    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void increaseStackLimit(CallbackInfoReturnable<Integer> cir) {
        int original = cir.getReturnValue();
        if (original > 1) {
            cir.setReturnValue(StackSizeHelper.getNewStackSize());
        }
    }
    
    /**
     * Сохраняет размер стака как int вместо byte.
     * Поддерживает совместимость с ванильным форматом.
     */
    @Redirect(method = "save",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putByte(Ljava/lang/String;B)V"))
    private void saveBigStack(CompoundTag tag, String key, byte ignored) {
        int count = ((ItemStack) (Object) this).getCount();
        
        // Сохраняем в Count как byte для ванильной совместимости (макс 127)
        tag.putByte("Count", (byte) Math.min(count, Byte.MAX_VALUE));
        
        // Если больше 127, сохраняем реальное значение в BigCount
        if (count > Byte.MAX_VALUE)
            tag.putInt("BigCount", count);
    }
    
    /**
     * Читает размер стака как int вместо byte.
     * Поддерживает совместимость с ванильным форматом.
     */
    @SuppressWarnings("DataFlowIssue")
    @Redirect(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/world/item/ItemStack;count:I",
                       opcode = Opcodes.PUTFIELD))
    private void readBigStack(ItemStack instance, int value, CompoundTag tag) {
        ItemStackAccessor accessor = ((ItemStackAccessor) (Object) instance);
        
        // Приоритет: BigCount > Count как int > Count как byte
        if (tag.contains("BigCount"))
            accessor.stardew$setCount(tag.getInt("BigCount"));
        else if (tag.getTagType("Count") == Tag.TAG_INT)
            accessor.stardew$setCount(tag.getInt("Count"));
        else
            accessor.stardew$setCount(tag.getByte("Count"));
    }
}

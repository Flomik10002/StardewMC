package dev.flomik.stardew.mixin.common;

import dev.flomik.stardew.common.module.time.TimeFreezeManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин для заморозки времени в ServerLevel.
 * Предотвращает увеличение dayTime когда время заморожено.
 */
@Mixin(ServerLevel.class)
public class ServerLevelTimeMixin {

    /**
     * Перехватываем метод tick и предотвращаем изменение времени если оно заморожено.
     * Метод tickTime() вызывается внутри tick() и увеличивает dayTime.
     */
    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void onTickTime(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        
        if (!TimeFreezeManager.shouldTickTime(level)) {
            ci.cancel();
        }
    }
}


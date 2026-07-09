package dev.flomik.stardew.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.flomik.stardew.client.character.skin.RuntimeSkinManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Перманентно "запирает" скин/модель руки локального игрока на нашу
 * сгенерированную (см. {@link RuntimeSkinManager}), не полагаясь на то, кто
 * последний записал приватные поля {@link PlayerInfo} (см. javadoc
 * {@code RuntimeSkinManager} - раньше это была гонка с асинхронным
 * {@code registerTextures()}, тянущим лицензионный скин с Mojang).
 *
 * Перехватываются САМИ методы чтения, а не поля - {@code getSkinLocation()}/
 * {@code getModelName()} это единственный путь, которым
 * {@code AbstractClientPlayer.getSkinTextureLocation()}/{@code getModelName()}
 * (а через них — весь рендер персонажа) узнают скин/модель, см.
 * {@code AbstractClientPlayer.java}. Vanilla {@code registerTextures()}
 * по-прежнему вызывается и пишет в свои поля как обычно (кейп/элитра
 * локального игрока не трогаем) - мы просто никогда не читаем то конкретное
 * поле (`textureLocations.get(SKIN)`/`skinModel`) для локального игрока.
 */
@Mixin(PlayerInfo.class)
public abstract class PlayerInfoSkinLockMixin {

    // @At("RETURN"), не "HEAD" - метод сам вызывает registerTextures() в
    // начале своего тела (кейп/элитра тоже читаются через него), пусть
    // отрабатывает как обычно; мы просто подменяем итоговое возвращаемое
    // значение уже ПОСЛЕ, а не мешаем оригинальной логике запуститься.
    @Inject(method = "getSkinLocation", at = @At("RETURN"), cancellable = true)
    private void stardew_lockSkinLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        if (isLocalPlayerWithComposedSkin()) {
            cir.setReturnValue(RuntimeSkinManager.getTextureLocation());
        }
    }

    @Inject(method = "getModelName", at = @At("RETURN"), cancellable = true)
    private void stardew_lockModelName(CallbackInfoReturnable<String> cir) {
        if (isLocalPlayerWithComposedSkin()) {
            cir.setReturnValue(RuntimeSkinManager.getModelName());
        }
    }

    private boolean isLocalPlayerWithComposedSkin() {
        if (!RuntimeSkinManager.hasComposedSkin()) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return false;
        GameProfile profile = ((PlayerInfo) (Object) this).getProfile();
        return profile != null && profile.getId().equals(minecraft.player.getGameProfile().getId());
    }
}

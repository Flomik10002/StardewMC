package dev.flomik.stardew.common.module.character.network;

import dev.flomik.stardew.common.module.character.AnimalPreference;
import dev.flomik.stardew.common.module.character.CharacterModelType;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.common.module.character.FarmType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Сервер подтверждает финальный (провалидированный, ТЗ §33) профиль — именно
 * его, а не то, что клиент отправил, применяет {@link dev.flomik.stardew.client.character.skin.SkinComposer}/
 * {@link dev.flomik.stardew.client.character.skin.RuntimeSkinManager} и
 * закрывает экран (ТЗ §43 "Финализация").
 */
public class S2CCharacterCreationAccepted {

    private final String name;
    private final String farmName;
    private final String favoriteThing;
    private final AnimalPreference animalPreference;
    private final CharacterModelType modelType;
    private final String skinId;
    private final int eyeColor;
    private final String hairId;
    private final int hairColor;
    private final String shirtId;
    private final int shirtColor;
    private final String pantsId;
    private final int pantsColor;
    private final String accessoryId;
    private final FarmType farmType;

    public S2CCharacterCreationAccepted(CharacterProfile profile) {
        this.name = profile.getName();
        this.farmName = profile.getFarmName();
        this.favoriteThing = profile.getFavoriteThing();
        this.animalPreference = profile.getAnimalPreference();
        this.modelType = profile.getModelType();
        this.skinId = profile.getSkinId();
        this.eyeColor = profile.getEyeColor();
        this.hairId = profile.getHairId();
        this.hairColor = profile.getHairColor();
        this.shirtId = profile.getShirtId();
        this.shirtColor = profile.getShirtColor();
        this.pantsId = profile.getPantsId();
        this.pantsColor = profile.getPantsColor();
        this.accessoryId = profile.getAccessoryId();
        this.farmType = profile.getFarmType();
    }

    private S2CCharacterCreationAccepted(String name, String farmName, String favoriteThing,
                                          AnimalPreference animalPreference, CharacterModelType modelType,
                                          String skinId, int eyeColor,
                                          String hairId, int hairColor, String shirtId, int shirtColor,
                                          String pantsId, int pantsColor, String accessoryId, FarmType farmType) {
        this.name = name;
        this.farmName = farmName;
        this.favoriteThing = favoriteThing;
        this.animalPreference = animalPreference;
        this.modelType = modelType;
        this.skinId = skinId;
        this.eyeColor = eyeColor;
        this.hairId = hairId;
        this.hairColor = hairColor;
        this.shirtId = shirtId;
        this.shirtColor = shirtColor;
        this.pantsId = pantsId;
        this.pantsColor = pantsColor;
        this.accessoryId = accessoryId;
        this.farmType = farmType;
    }

    public static void encode(S2CCharacterCreationAccepted msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name);
        buf.writeUtf(msg.farmName);
        buf.writeUtf(msg.favoriteThing);
        buf.writeEnum(msg.animalPreference);
        buf.writeEnum(msg.modelType);
        buf.writeUtf(msg.skinId);
        buf.writeInt(msg.eyeColor);
        buf.writeUtf(msg.hairId);
        buf.writeInt(msg.hairColor);
        buf.writeUtf(msg.shirtId);
        buf.writeInt(msg.shirtColor);
        buf.writeUtf(msg.pantsId);
        buf.writeInt(msg.pantsColor);
        buf.writeUtf(msg.accessoryId);
        buf.writeEnum(msg.farmType);
    }

    public static S2CCharacterCreationAccepted decode(FriendlyByteBuf buf) {
        return new S2CCharacterCreationAccepted(
                buf.readUtf(), buf.readUtf(), buf.readUtf(),
                buf.readEnum(AnimalPreference.class), buf.readEnum(CharacterModelType.class),
                buf.readUtf(), buf.readInt(),
                buf.readUtf(), buf.readInt(),
                buf.readUtf(), buf.readInt(),
                buf.readUtf(), buf.readInt(),
                buf.readUtf(),
                buf.readEnum(FarmType.class)
        );
    }

    public CharacterProfile toProfile() {
        CharacterProfile profile = new CharacterProfile();
        profile.setInitializationState(CharacterProfile.InitializationState.COMPLETED);
        profile.setName(name);
        profile.setFarmName(farmName);
        profile.setFavoriteThing(favoriteThing);
        profile.setAnimalPreference(animalPreference);
        profile.setModelType(modelType);
        profile.setSkinId(skinId);
        profile.setEyeColor(eyeColor);
        profile.setHairId(hairId);
        profile.setHairColor(hairColor);
        profile.setShirtId(shirtId);
        profile.setShirtColor(shirtColor);
        profile.setPantsId(pantsId);
        profile.setPantsColor(pantsColor);
        profile.setAccessoryId(accessoryId);
        profile.setFarmType(farmType);
        return profile;
    }

    public static void handle(S2CCharacterCreationAccepted msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> dev.flomik.stardew.client.character.ClientCharacterCreationOpener.onAccepted(msg.toProfile())));
        ctx.get().setPacketHandled(true);
    }
}

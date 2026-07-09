package dev.flomik.stardew.common.module.character.network;

import dev.flomik.stardew.common.module.character.AnimalPreference;
import dev.flomik.stardew.common.module.character.CharacterModelType;
import dev.flomik.stardew.common.module.character.FarmType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Клиент отправляет заполненный профиль после нажатия OK (ТЗ §43). Сервер —
 * источник истины: не доверяет ни одному полю без проверки
 * ({@link CharacterCreationServerHandler}, ТЗ §33).
 */
public class C2SFinishCharacterCreation {

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

    public C2SFinishCharacterCreation(String name, String farmName, String favoriteThing,
                                       AnimalPreference animalPreference, CharacterModelType modelType,
                                       String skinId, int eyeColor,
                                       String hairId, int hairColor,
                                       String shirtId, int shirtColor,
                                       String pantsId, int pantsColor,
                                       String accessoryId, FarmType farmType) {
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

    public static void encode(C2SFinishCharacterCreation msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 24);
        buf.writeUtf(msg.farmName, 32);
        buf.writeUtf(msg.favoriteThing, 48);
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

    public static C2SFinishCharacterCreation decode(FriendlyByteBuf buf) {
        return new C2SFinishCharacterCreation(
                buf.readUtf(24),
                buf.readUtf(32),
                buf.readUtf(48),
                buf.readEnum(AnimalPreference.class),
                buf.readEnum(CharacterModelType.class),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readEnum(FarmType.class)
        );
    }

    public static void handle(C2SFinishCharacterCreation msg, Supplier<NetworkEvent.Context> ctx) {
        var player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (player != null) {
                CharacterCreationServerHandler.handle(player, msg);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public String getName() { return name; }
    public String getFarmName() { return farmName; }
    public String getFavoriteThing() { return favoriteThing; }
    public AnimalPreference getAnimalPreference() { return animalPreference; }
    public CharacterModelType getModelType() { return modelType; }
    public String getSkinId() { return skinId; }
    public int getEyeColor() { return eyeColor; }
    public String getHairId() { return hairId; }
    public int getHairColor() { return hairColor; }
    public String getShirtId() { return shirtId; }
    public int getShirtColor() { return shirtColor; }
    public String getPantsId() { return pantsId; }
    public int getPantsColor() { return pantsColor; }
    public String getAccessoryId() { return accessoryId; }
    public FarmType getFarmType() { return farmType; }
}

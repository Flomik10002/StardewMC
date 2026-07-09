package dev.flomik.stardew.common.module.character;

import net.minecraft.nbt.CompoundTag;

/**
 * Постоянные данные персонажа игрока — источник истины для скина, а не сам
 * PNG (см. docs следующего порядка: ТЗ §66). {@link dev.flomik.stardew.client.character.skin.SkinComposer}
 * всегда может пересобрать текстуру из этого профиля; обратное не гарантировано.
 *
 * Схема версионируется ({@link #SCHEMA_VERSION}) на случай будущих миграций
 * (ТЗ §35).
 */
public class CharacterProfile {

    public static final int SCHEMA_VERSION = 1;

    public enum InitializationState {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    private InitializationState initializationState = InitializationState.NOT_STARTED;

    // --- identity (ТЗ §34 "identity") ---
    private String name = "";
    private String farmName = "";
    private String favoriteThing = "";
    private AnimalPreference animalPreference = AnimalPreference.CAT_GINGER;

    // --- appearance (ТЗ §34 "appearance") ---
    private CharacterModelType modelType = CharacterModelType.CLASSIC;
    private String skinId = "skin_001";
    private int eyeColor = 0x4A7C91;
    private String hairId = "hair_001";
    private int hairColor = 0x7A3E24;
    private String shirtId = "shirt_001";
    private int shirtColor = 0x527A61;
    private String pantsId = "pants_001";
    private int pantsColor = 0x29334A;
    private String accessoryId = "none";

    // --- world (ТЗ §34 "world") ---
    private FarmType farmType = FarmType.STANDARD;

    public boolean isCharacterCreated() {
        return initializationState == InitializationState.COMPLETED;
    }

    public InitializationState getInitializationState() {
        return initializationState;
    }

    public void setInitializationState(InitializationState state) {
        this.initializationState = state;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getFavoriteThing() { return favoriteThing; }
    public void setFavoriteThing(String favoriteThing) { this.favoriteThing = favoriteThing; }

    public AnimalPreference getAnimalPreference() { return animalPreference; }
    public void setAnimalPreference(AnimalPreference animalPreference) { this.animalPreference = animalPreference; }

    public CharacterModelType getModelType() { return modelType; }
    public void setModelType(CharacterModelType modelType) { this.modelType = modelType; }

    public String getSkinId() { return skinId; }
    public void setSkinId(String skinId) { this.skinId = skinId; }

    public int getEyeColor() { return eyeColor; }
    public void setEyeColor(int eyeColor) { this.eyeColor = eyeColor; }

    public String getHairId() { return hairId; }
    public void setHairId(String hairId) { this.hairId = hairId; }

    public int getHairColor() { return hairColor; }
    public void setHairColor(int hairColor) { this.hairColor = hairColor; }

    public String getShirtId() { return shirtId; }
    public void setShirtId(String shirtId) { this.shirtId = shirtId; }

    public int getShirtColor() { return shirtColor; }
    public void setShirtColor(int shirtColor) { this.shirtColor = shirtColor; }

    public String getPantsId() { return pantsId; }
    public void setPantsId(String pantsId) { this.pantsId = pantsId; }

    public int getPantsColor() { return pantsColor; }
    public void setPantsColor(int pantsColor) { this.pantsColor = pantsColor; }

    public String getAccessoryId() { return accessoryId; }
    public void setAccessoryId(String accessoryId) { this.accessoryId = accessoryId; }

    public FarmType getFarmType() { return farmType; }
    public void setFarmType(FarmType farmType) { this.farmType = farmType; }

    public CharacterProfile copy() {
        CharacterProfile copy = new CharacterProfile();
        copy.initializationState = this.initializationState;
        copy.name = this.name;
        copy.farmName = this.farmName;
        copy.favoriteThing = this.favoriteThing;
        copy.animalPreference = this.animalPreference;
        copy.modelType = this.modelType;
        copy.skinId = this.skinId;
        copy.eyeColor = this.eyeColor;
        copy.hairId = this.hairId;
        copy.hairColor = this.hairColor;
        copy.shirtId = this.shirtId;
        copy.shirtColor = this.shirtColor;
        copy.pantsId = this.pantsId;
        copy.pantsColor = this.pantsColor;
        copy.accessoryId = this.accessoryId;
        copy.farmType = this.farmType;
        return copy;
    }

    public void saveNBT(CompoundTag tag) {
        tag.putInt("SchemaVersion", SCHEMA_VERSION);
        tag.putString("InitializationState", initializationState.name());

        CompoundTag identity = new CompoundTag();
        identity.putString("Name", name);
        identity.putString("FarmName", farmName);
        identity.putString("FavoriteThing", favoriteThing);
        identity.putString("AnimalPreference", animalPreference.name());
        tag.put("Identity", identity);

        CompoundTag appearance = new CompoundTag();
        appearance.putString("ModelType", modelType.name());
        appearance.putString("SkinId", skinId);
        appearance.putInt("EyeColor", eyeColor);
        appearance.putString("HairId", hairId);
        appearance.putInt("HairColor", hairColor);
        appearance.putString("ShirtId", shirtId);
        appearance.putInt("ShirtColor", shirtColor);
        appearance.putString("PantsId", pantsId);
        appearance.putInt("PantsColor", pantsColor);
        appearance.putString("AccessoryId", accessoryId);
        tag.put("Appearance", appearance);

        CompoundTag world = new CompoundTag();
        world.putString("FarmType", farmType.name());
        tag.put("World", world);
    }

    /** Мутирует этот же instance (нужно для capability — identity должен быть стабильным). */
    public void loadFrom(CompoundTag tag) {
        CharacterProfile loaded = loadNBT(tag);
        this.initializationState = loaded.initializationState;
        this.name = loaded.name;
        this.farmName = loaded.farmName;
        this.favoriteThing = loaded.favoriteThing;
        this.animalPreference = loaded.animalPreference;
        this.modelType = loaded.modelType;
        this.skinId = loaded.skinId;
        this.eyeColor = loaded.eyeColor;
        this.hairId = loaded.hairId;
        this.hairColor = loaded.hairColor;
        this.shirtId = loaded.shirtId;
        this.shirtColor = loaded.shirtColor;
        this.pantsId = loaded.pantsId;
        this.pantsColor = loaded.pantsColor;
        this.accessoryId = loaded.accessoryId;
        this.farmType = loaded.farmType;
    }

    public static CharacterProfile loadNBT(CompoundTag tag) {
        CharacterProfile profile = new CharacterProfile();
        if (tag.isEmpty()) return profile;

        int version = tag.contains("SchemaVersion") ? tag.getInt("SchemaVersion") : 1;
        CharacterProfileMigrations.migrate(tag, version);

        if (tag.contains("InitializationState")) {
            try {
                profile.initializationState = InitializationState.valueOf(tag.getString("InitializationState"));
            } catch (IllegalArgumentException ignored) {
                profile.initializationState = InitializationState.NOT_STARTED;
            }
        }

        if (tag.contains("Identity")) {
            CompoundTag identity = tag.getCompound("Identity");
            profile.name = identity.getString("Name");
            profile.farmName = identity.getString("FarmName");
            profile.favoriteThing = identity.getString("FavoriteThing");
            if (identity.contains("AnimalPreference")) {
                profile.animalPreference = AnimalPreference.fromId(identity.getString("AnimalPreference"));
            }
        }

        if (tag.contains("Appearance")) {
            CompoundTag appearance = tag.getCompound("Appearance");
            if (appearance.contains("ModelType")) {
                profile.modelType = CharacterModelType.fromId(appearance.getString("ModelType"));
            }
            if (appearance.contains("SkinId")) profile.skinId = appearance.getString("SkinId");
            if (appearance.contains("EyeColor")) profile.eyeColor = appearance.getInt("EyeColor");
            if (appearance.contains("HairId")) profile.hairId = appearance.getString("HairId");
            if (appearance.contains("HairColor")) profile.hairColor = appearance.getInt("HairColor");
            if (appearance.contains("ShirtId")) profile.shirtId = appearance.getString("ShirtId");
            if (appearance.contains("ShirtColor")) profile.shirtColor = appearance.getInt("ShirtColor");
            if (appearance.contains("PantsId")) profile.pantsId = appearance.getString("PantsId");
            if (appearance.contains("PantsColor")) profile.pantsColor = appearance.getInt("PantsColor");
            if (appearance.contains("AccessoryId")) profile.accessoryId = appearance.getString("AccessoryId");
        }

        if (tag.contains("World")) {
            CompoundTag world = tag.getCompound("World");
            if (world.contains("FarmType")) profile.farmType = FarmType.fromId(world.getString("FarmType"));
        }

        return profile;
    }
}

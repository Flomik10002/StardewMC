package dev.flomik.stardew.common.module.character;

public enum CharacterModelType {
    CLASSIC,
    SLIM;

    public static CharacterModelType fromId(String id) {
        for (CharacterModelType value : values()) {
            if (value.name().equalsIgnoreCase(id)) return value;
        }
        return CLASSIC;
    }
}

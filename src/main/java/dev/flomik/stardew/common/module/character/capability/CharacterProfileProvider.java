package dev.flomik.stardew.common.module.character.capability;

import dev.flomik.stardew.common.module.character.CharacterProfile;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Отдельная capability от {@code PlayerProvider}/{@code PlayerStardewState}
 * (та — про энергию/кошелёк, это — про личность/внешность персонажа; разные
 * ответственности не должны жить в одном классе).
 */
public class CharacterProfileProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<CharacterProfile> CHARACTER_PROFILE_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final CharacterProfile backend;
    private final LazyOptional<CharacterProfile> optional;

    public CharacterProfileProvider(CharacterProfile backend) {
        this.backend = backend;
        this.optional = LazyOptional.of(() -> backend);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CHARACTER_PROFILE_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        backend.saveNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        backend.loadFrom(nbt);
    }
}

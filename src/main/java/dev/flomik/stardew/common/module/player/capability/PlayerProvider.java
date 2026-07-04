package dev.flomik.stardew.common.module.player.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerProvider implements ICapabilitySerializable<CompoundTag> {

    // Регистрируем токен для доступа к капе
    public static final Capability<PlayerStardewState> STARDEW_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final PlayerStardewState backend;
    private final LazyOptional<PlayerStardewState> optional;

    public PlayerProvider(PlayerStardewState backend) {
        this.backend = backend;
        this.optional = LazyOptional.of(() -> backend);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == STARDEW_CAPABILITY) {
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
        backend.loadNBT(nbt);
    }
}
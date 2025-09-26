package dev.flomik.stardew.core.registry.blockentity;

import dev.flomik.stardew.core.registry.block.BlockFarmland;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FarmlandBlockEntity extends BlockEntity {

    private int hydratedUntilDay = 0;

    public FarmlandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FARMLAND.get(), pos, state);
    }

    public void onPlace() {
        // инициализация при установке
    }

    public void tickServer() {
        // пока ничего — позже сюда можно вставить логику засыхания
    }

    public void hydrate(int currentDay) {
        this.hydratedUntilDay = currentDay + 1;
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (!state.getValue(BlockFarmland.HYDRATED)) {
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockFarmland.HYDRATED, true));
            }
        }
        setChanged();
    }

    public boolean isHydrated(int currentDay) {
        return currentDay <= hydratedUntilDay;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("hydratedUntilDay", hydratedUntilDay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.hydratedUntilDay = tag.getInt("hydratedUntilDay");
    }
}

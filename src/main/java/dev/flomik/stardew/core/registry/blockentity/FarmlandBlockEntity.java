package dev.flomik.stardew.core.registry.blockentity;

import dev.flomik.stardew.core.crop.FertilizerType;
import dev.flomik.stardew.core.registry.block.BlockFarmland;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FarmlandBlockEntity extends BlockEntity {

    private int hydratedUntilDay = 0;
    private FertilizerType fertilizer = FertilizerType.NONE;

    public FarmlandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FARMLAND.get(), pos, state);
    }

    public void onPlace() {
    }

    @Override public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) dev.flomik.stardew.core.crop.runtime.FarmlandTracker.onLoad(this);
    }
    @Override public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide) dev.flomik.stardew.core.crop.runtime.FarmlandTracker.onRemove(this);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, FarmlandBlockEntity be) {
        dev.flomik.stardew.core.crop.runtime.FarmlandTracker.onLoad(be);
    }

    public boolean applyFertilizer(FertilizerType type) {
        if (this.fertilizer != FertilizerType.NONE) return false;
        this.fertilizer = type;

        if (!level.isClientSide) {
            BlockState current = getBlockState();
            level.setBlockAndUpdate(worldPosition, current.setValue(BlockFarmland.FERTILIZER, type));
        }

        setChanged();
        return true;
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

    public FertilizerType getFertilizer() {
        return fertilizer;
    }

    public boolean hasFertilizer() {
        return fertilizer != FertilizerType.NONE;
    }

    public boolean isHydrated(int currentDay) {
        return currentDay <= hydratedUntilDay;
    }
    
    public void dehydrate(int currentDay) {
        this.hydratedUntilDay = 0;
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.getValue(BlockFarmland.HYDRATED)) {
                level.setBlockAndUpdate(worldPosition, state.setValue(BlockFarmland.HYDRATED, false));
            }
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("hydratedUntilDay", hydratedUntilDay);
        tag.putString("fertilizer", fertilizer.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fertilizer = FertilizerType.valueOf(tag.getString("fertilizer"));
        this.hydratedUntilDay = tag.getInt("hydratedUntilDay");
    }
}

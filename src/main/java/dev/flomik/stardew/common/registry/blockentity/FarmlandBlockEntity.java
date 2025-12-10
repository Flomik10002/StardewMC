package dev.flomik.stardew.common.registry.blockentity;

import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.core.crop.FertilizerType;
import dev.flomik.stardew.common.registry.block.surface.BlockFarmland;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FarmlandBlockEntity extends BlockEntity {

    private boolean hydrated = false;
    private FertilizerType fertilizer = FertilizerType.NONE;

    public FarmlandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FARMLAND.getTypeValue(), pos, state);
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

    public void hydrate() {
        this.hydrated = true;
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

    public boolean isHydrated() { return hydrated; }
    
    public void dehydrate() {
        this.hydrated = false;
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
        tag.putBoolean("hydrated", hydrated);
        tag.putString("fertilizer", fertilizer.name());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fertilizer = FertilizerType.valueOf(tag.getString("fertilizer"));
        this.hydrated = tag.contains("hydrated") ? tag.getBoolean("hydrated") : false;
    }
}

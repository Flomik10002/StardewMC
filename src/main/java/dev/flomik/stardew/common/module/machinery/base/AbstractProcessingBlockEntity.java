package dev.flomik.stardew.common.module.machinery.base;

import dev.flomik.stardew.common.api.block.BlockEntityHasItemVisual;
import dev.flomik.stardew.common.api.quality.Quality;
import dev.flomik.stardew.common.module.machinery.recipe.MachineRecipe;
import dev.flomik.stardew.common.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public abstract class AbstractProcessingBlockEntity extends BlockEntityHasItemVisual {

    private int progress = 0;
    private int maxProgress = 0;
    // TODO: [MECHANIC] Replace tick-based progress with absolute time
    // private long finishTime = -1;
    // On load: if (level.getGameTime() >= finishTime) -> finishProcessing()
    // This allows processing to continue even when chunk is unloaded.
    
    private ItemStack inputStack = ItemStack.EMPTY;
    private ItemStack outputStack = ItemStack.EMPTY;

    public AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract List<MachineRecipe> getRecipes();

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (!outputStack.isEmpty()) {
            return;
        }

        if (!inputStack.isEmpty() && maxProgress > 0) {
            progress++;
            if (progress >= maxProgress) {
                finishProcessing();
            }
        }
    }

    private void finishProcessing() {
        for (MachineRecipe recipe : getRecipes()) {
            if (recipe.matches(inputStack)) {
                MachineRecipe.ProcessingResult result = recipe.getResult(inputStack, level.random);

                this.outputStack = result.output();
                Quality.set(this.outputStack, result.quality());

                this.inputStack = ItemStack.EMPTY;
                this.progress = 0;
                this.maxProgress = 0;

                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                return;
            }
        }
    }

    public boolean interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!outputStack.isEmpty()) {
            if (!player.getInventory().add(outputStack)) {
                player.drop(outputStack, false);
            }
            outputStack = ItemStack.EMPTY;

            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

                level.playSound(null, worldPosition, ModSounds.MACHINE_COLLECT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return true;
        }

        if (inputStack.isEmpty() && !held.isEmpty()) {
            for (MachineRecipe recipe : getRecipes()) {
                if (recipe.matches(held)) {
                    this.inputStack = held.split(1);
                    this.maxProgress = recipe.getProcessingTime();
                    this.progress = 0;
                    setChanged();

                    if (level != null) {
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                        level.playSound(null, worldPosition,
                                level.random.nextBoolean() ? ModSounds.MACHINE_INSERT_1.get() : ModSounds.MACHINE_INSERT_2.get(),
                                SoundSource.BLOCKS, 1.0f, 1.0f);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderItem() {
        return !outputStack.isEmpty();
    }

    @Override
    public ItemStack getVisualItem() {
        return outputStack;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        if (!inputStack.isEmpty()) tag.put("Input", inputStack.save(new CompoundTag()));
        if (!outputStack.isEmpty()) tag.put("Output", outputStack.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");

        if (tag.contains("Input")) {
            inputStack = ItemStack.of(tag.getCompound("Input"));
        } else {
            inputStack = ItemStack.EMPTY;
        }

        if (tag.contains("Output")) {
            outputStack = ItemStack.of(tag.getCompound("Output"));
        } else {
            outputStack = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }
}
package dev.flomik.stardew.common.module.shipping;

import dev.flomik.stardew.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * BlockEntity для Shipping Bin.
 * Имеет один слот, предметы из которого автоматически отправляются в ShippingManager.
 * Поддерживает анимацию крышки как у сундука.
 */
public class ShippingBinBlockEntity extends BlockEntity implements Container, LidBlockEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();
    private static final int ABSORB_INTERVAL_TICKS = 5;

    // Анимация крышки
    private float openProgress;
    private float oldOpenProgress;
    private int openCount;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), 1, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ShippingBinMenu menu) {
                return menu.getBlockEntity() == ShippingBinBlockEntity.this;
            }
            return false;
        }
    };

    public ShippingBinBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHIPPING_BIN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ShippingBinBlockEntity blockEntity) {
        blockEntity.oldOpenProgress = blockEntity.openProgress;

        if (blockEntity.openCount > 0 && blockEntity.openProgress < 1.0F) {
            blockEntity.openProgress += 0.1F;
        } else if (blockEntity.openCount == 0 && blockEntity.openProgress > 0.0F) {
            blockEntity.openProgress -= 0.1F;
        }

        blockEntity.openProgress = Math.max(0.0F, Math.min(1.0F, blockEntity.openProgress));

        if (!level.isClientSide() && level.getGameTime() % ABSORB_INTERVAL_TICKS == 0 && level instanceof ServerLevel serverLevel) {
            blockEntity.absorbDroppedItems(serverLevel, pos);
        }
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return oldOpenProgress + (openProgress - oldOpenProgress) * partialTicks;
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == 1) {
            this.openCount = data;
            return true;
        }
        return super.triggerEvent(id, data);
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator() && level != null) {
            openersCounter.incrementOpeners(player, level, worldPosition, getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator() && level != null) {
            openersCounter.decrementOpeners(player, level, worldPosition, getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove && level != null) {
            openersCounter.recheckOpeners(level, worldPosition, getBlockState());
        }
    }

    /**
     * Отправляет содержимое слота в ShippingManager и очищает слот.
     * Вызывается при закрытии GUI или при добавлении предмета.
     */
    public void shipContents(Player player) {
        if (level == null || level.isClientSide()) return;

        ItemStack stack = items.get(0);
        if (!stack.isEmpty()) {
            ShippingManager manager = ShippingManager.get(level);
            manager.shipItem(player.getUUID(), stack);
            items.set(0, ItemStack.EMPTY);
            setChanged();
        }
    }

    public void shipItem(Player player, ItemStack stack) {
        if (level == null || level.isClientSide() || stack.isEmpty()) return;

        ShippingManager manager = ShippingManager.get(level);
        manager.shipItem(player.getUUID(), stack);
        setChanged();
    }

    public ItemStack getLastShippedItem(Player player) {
        if (level == null || level.isClientSide()) return ItemStack.EMPTY;

        ItemStack last = ShippingManager.get(level).peekLastItem(player.getUUID());
        return last != null ? last : ItemStack.EMPTY;
    }

    public ItemStack takeLastShippedItem(Player player) {
        if (level == null || level.isClientSide()) return ItemStack.EMPTY;

        return ShippingManager.get(level).retrieveLastItem(player.getUUID());
    }

    /**
     * Пытается извлечь последний отправленный предмет обратно игроку.
     */
    public boolean tryRetrieveLastItem(Player player) {
        if (level == null || level.isClientSide()) return false;

        ItemStack retrieved = takeLastShippedItem(player);

        if (!retrieved.isEmpty()) {
            // Пытаемся дать предмет игроку
            if (!player.getInventory().add(retrieved)) {
                // Если инвентарь полный, выбрасываем предмет
                player.drop(retrieved, false);
            }
            return true;
        }
        return false;
    }

    private void absorbDroppedItems(ServerLevel serverLevel, BlockPos pos) {
        AABB absorbArea = new AABB(pos).inflate(0.35D).expandTowards(0.0D, 1.0D, 0.0D);
        List<ItemEntity> droppedItems = serverLevel.getEntitiesOfClass(
                ItemEntity.class,
                absorbArea,
                itemEntity -> itemEntity.isAlive() && !itemEntity.getItem().isEmpty()
        );

        for (ItemEntity itemEntity : droppedItems) {
            Player owner = resolveShippingOwner(serverLevel, itemEntity);
            if (owner == null) continue;

            shipItem(owner, itemEntity.getItem());
            itemEntity.discard();
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.BLOCKS,
                    0.25F,
                    1.0F
            );
        }
    }

    @Nullable
    private Player resolveShippingOwner(ServerLevel serverLevel, ItemEntity itemEntity) {
        Entity owner = itemEntity.getOwner();
        if (owner instanceof Player player) {
            return player;
        }

        List<ServerPlayer> nearbyPlayers = serverLevel.getPlayers(player ->
                player.distanceToSqr(itemEntity) <= 16.0D
        );
        if (nearbyPlayers.isEmpty()) {
            return null;
        }

        Player nearest = nearbyPlayers.get(0);
        double nearestDistance = nearest.distanceToSqr(itemEntity);
        for (int i = 1; i < nearbyPlayers.size(); i++) {
            Player candidate = nearbyPlayers.get(i);
            double distance = candidate.distanceToSqr(itemEntity);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    // --- Container Implementation ---

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    // --- Capabilities ---

    @Override
    public void onLoad() {
        super.onLoad();
        itemHandler = LazyOptional.of(() -> new InvWrapper(this));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public Component getDisplayName() {
        return Component.translatable("container.stardew.shipping_bin");
    }

    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ShippingBinMenu(containerId, playerInventory, this);
    }
}

package dev.flomik.stardew.common.registry.framework;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockEntry<T extends Block, E extends BlockEntity> implements Supplier<T> {
    private final RegistryObject<T> block;
    private final RegistryObject<Item> item;
    private final RegistryObject<BlockEntityType<E>> blockEntity;

    public BlockEntry(RegistryObject<T> block, RegistryObject<Item> item, RegistryObject<BlockEntityType<E>> blockEntity) {
        this.block = block;
        this.item = item;
        this.blockEntity = blockEntity;
    }

    // --- Основной доступ ---
    @Override
    public T get() {
        return block.get();
    }

    public RegistryObject<T> getBlock() {
        return block;
    }

    public RegistryObject<Item> getItem() {
        return item;
    }

    public RegistryObject<BlockEntityType<E>> getType() {
        if (blockEntity == null) {
            throw new IllegalStateException("Block " + block.getId() + " has no BlockEntity!");
        }
        return blockEntity;
    }

    public BlockEntityType<E> getTypeValue() {
        return getType().get();
    }
}
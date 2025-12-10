package dev.flomik.stardew.common.registry.framework;

import dev.flomik.stardew.common.registry.StardewRegistry;
import dev.flomik.stardew.common.registry.framework.datagen.DataGenManager;
import dev.flomik.stardew.common.registry.framework.datagen.ItemModelGen;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class BlockBuilder<T extends Block> {
    private final String name;
    private final Function<BlockBehaviour.Properties, T> factory;
    private BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();
    private final List<TagKey<Block>> tags = new ArrayList<>();
    private boolean isSeasonal = false;

    public static <B extends Block> BlockBuilder<B> create(String name, Function<BlockBehaviour.Properties, B> factory) {
        return new BlockBuilder<>(name, factory);
    }

    private BlockBuilder(String name, Function<BlockBehaviour.Properties, T> factory) {
        this.name = name;
        this.factory = factory;
    }

    public BlockBuilder<T> initialProperties(Supplier<BlockBehaviour.Properties> props) {
        this.properties = props.get();
        return this;
    }

    public BlockBuilder<T> properties(UnaryOperator<BlockBehaviour.Properties> configure) {
        this.properties = configure.apply(this.properties);
        return this;
    }

    public BlockBuilder<T> transform(UnaryOperator<BlockBuilder<T>> func) {
        return func.apply(this);
    }

    public BlockBuilder<T> seasonal(boolean seasonal) {
        this.isSeasonal = seasonal;
        return this;
    }

    public <E extends BlockEntity> BlockEntityBuilder<E> blockEntity(BiFunction<BlockPos, BlockState, E> beFactory) {
        return new BlockEntityBuilder<>(beFactory);
    }

    public BlockItemBuilder item() {
        return new BlockItemBuilder(this);
    }

    public RegistryObject<T> noItem() {
        return registerInternal(false, null, null, null).getBlock();
    }

    public RegistryObject<T> register() {
        return registerInternal(true, new Item.Properties(), null, null).getBlock();
    }

    protected BlockEntry<T, ?> registerInternal(boolean hasItem, Item.Properties itemProps, RegistryObject<CreativeModeTab> tab, ItemModelGen modelGen) {
        RegistryObject<T> blockReg = StardewRegistry.BLOCKS.register(name, () -> factory.apply(properties));
        RegistryObject<Item> itemReg = null;

        if (hasItem) {
            itemReg = StardewRegistry.ITEMS.register(name, () -> new BlockItem(blockReg.get(), itemProps));

            if (tab != null) {
                TabManager.assign(tab, itemReg);
            }
            if (modelGen != null) {
                DataGenManager.assign(itemReg, modelGen);
            }
        }

        return new BlockEntry<>(blockReg, itemReg, null);
    }

    public class BlockItemBuilder {
        private final BlockBuilder<T> parent;
        private final Item.Properties itemProperties = new Item.Properties();
        private RegistryObject<CreativeModeTab> tab;
        private ItemModelGen modelGen; // Храним генератор

        public BlockItemBuilder(BlockBuilder<T> parent) { this.parent = parent; }

        public BlockItemBuilder tab(RegistryObject<CreativeModeTab> tab) {
            this.tab = tab;
            return this;
        }

        public BlockItemBuilder stacksTo(int count) {
            this.itemProperties.stacksTo(count);
            return this;
        }

        public BlockItemBuilder model(ItemModelGen gen) {
            this.modelGen = gen;
            return this;
        }

        public RegistryObject<T> register() {
            return parent.registerInternal(true, itemProperties, tab, modelGen).getBlock();
        }
    }

    public class BlockEntityBuilder<E extends BlockEntity> {
        private final BiFunction<BlockPos, BlockState, E> beFactory;
        private Item.Properties itemProperties = new Item.Properties();
        private RegistryObject<CreativeModeTab> tab;
        private boolean hasItem = true;
        private ItemModelGen modelGen; // Храним генератор

        public BlockEntityBuilder(BiFunction<BlockPos, BlockState, E> beFactory) {
            this.beFactory = beFactory;
        }

        public BlockEntityBuilder<E> item() { return this; }

        public BlockEntityBuilder<E> noItem() {
            this.hasItem = false;
            return this;
        }

        public BlockEntityBuilder<E> tab(RegistryObject<CreativeModeTab> tab) {
            this.tab = tab;
            return this;
        }

        public BlockEntityBuilder<E> model(ItemModelGen gen) {
            this.modelGen = gen;
            return this;
        }

        public BlockEntry<T, E> register() {
            BlockEntry<T, ?> base = BlockBuilder.this.registerInternal(hasItem, itemProperties, tab, modelGen);

            RegistryObject<BlockEntityType<E>> typeReg = StardewRegistry.BLOCK_ENTITIES.register(name, () ->
                    BlockEntityType.Builder.of(beFactory::apply, base.get()).build(null)
            );

            return new BlockEntry<>(base.getBlock(), base.getItem(), typeReg);
        }
    }
}
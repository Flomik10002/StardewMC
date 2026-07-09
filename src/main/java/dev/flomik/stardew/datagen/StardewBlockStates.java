package dev.flomik.stardew.datagen;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.common.registry.framework.multiblock.MultiblockBlock;
import dev.flomik.stardew.common.registry.framework.multiblock.MultiblockProperties;
import dev.flomik.stardew.common.registry.framework.multiblock.MultiblockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Автоматически генерирует blockstate для всех зарегистрированных
 * мультиблоков ({@link MultiblockBlock}) — писать его руками для каждого
 * нового мультиблока не нужно вообще.
 *
 * Клетка (0,0) ("исток") получает настоящую модель {@code block/<name>}
 * (её всё ещё нужно нарисовать/собрать руками — это единственное, что
 * авторы мультиблока должны сделать сами). Все остальные клетки — общую
 * переиспользуемую невидимую модель {@code block/empty}, которая никак
 * не зависит от конкретного мультиблока.
 *
 * Свойства part_x/part_y общие для всех мультиблоков и объявлены с
 * фиксированным диапазоном 0..4 (см. {@link MultiblockBlock#MAX_SIZE}),
 * поэтому variant-билдер проходит по всем 25 комбинациям вне зависимости
 * от реальных width/height блока — состояния за пределами фактической
 * фигуры просто никогда не будут стоять в мире, но модель им всё равно
 * нужна (иначе Minecraft будет ругаться на отсутствующие варианты).
 */
public class StardewBlockStates extends BlockStateProvider {

    public StardewBlockStates(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StardewMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        MultiblockRegistry.all().forEach((name, supplier) -> {
            Block block = supplier.get();
            if (block instanceof MultiblockBlock multiblock) {
                generateMultiblock(name, multiblock);
            }
        });
    }

    private void generateMultiblock(String name, MultiblockBlock block) {
        ModelFile anchorModel = models().getExistingFile(modLoc("block/" + name));
        ModelFile emptyModel = models().getExistingFile(modLoc("block/empty"));

        getVariantBuilder(block).forAllStates(state -> {
            boolean isOrigin = state.getValue(MultiblockProperties.PART_X) == 0
                    && state.getValue(MultiblockProperties.PART_Y) == 0;

            return ConfiguredModel.builder()
                    .modelFile(isOrigin ? anchorModel : emptyModel)
                    .build();
        });
    }
}

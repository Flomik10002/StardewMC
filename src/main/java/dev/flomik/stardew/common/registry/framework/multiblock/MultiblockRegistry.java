package dev.flomik.stardew.common.registry.framework.multiblock;

import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Реестр "имя -> поставщик блока" для всех блоков, зарегистрированных через
 * {@code BlockBuilder} — заполняется автоматически, руками трогать не нужно.
 * Датаген {@link dev.flomik.stardew.datagen.StardewBlockStates} проходит по
 * нему и генерирует blockstate для тех записей, что оказались мультиблоком
 * ({@link MultiblockBlock}), полностью автоматически (анкер получает
 * настоящую модель {@code block/<name>}, остальные клетки — общую пустую
 * {@code block/empty}). Благодаря этому у нового мультиблока вообще не нужно
 * руками писать blockstate-файл — только модель для клетки-истока.
 */
public final class MultiblockRegistry {

    private static final Map<String, Supplier<? extends Block>> ENTRIES = new LinkedHashMap<>();

    public static void register(String name, Supplier<? extends Block> block) {
        ENTRIES.put(name, block);
    }

    public static Map<String, Supplier<? extends Block>> all() {
        return ENTRIES;
    }

    private MultiblockRegistry() {
    }
}

package dev.flomik.stardew.common.registry.framework;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TabManager {
    private static final Map<RegistryObject<CreativeModeTab>, List<Supplier<? extends ItemLike>>> TAB_CONTENTS = new HashMap<>();

    /**
     * Вызывается из билдера предмета/блока.
     * "Запомни, что этот предмет должен быть в этой вкладке"
     */
    public static void assign(RegistryObject<CreativeModeTab> tab, Supplier<? extends ItemLike> item) {
        TAB_CONTENTS.computeIfAbsent(tab, k -> new ArrayList<>()).add(item);
    }

    /**
     * Это событие Forge вызывает автоматически. Мы перехватываем его и заполняем вкладки.
     */
    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        TAB_CONTENTS.forEach((tabReg, items) -> {
            if (event.getTabKey() == tabReg.getKey()) {
                items.forEach(item -> event.accept(item.get()));
            }
        });
    }
}
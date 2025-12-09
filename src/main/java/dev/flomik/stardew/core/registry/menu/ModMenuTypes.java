package dev.flomik.stardew.core.registry.menu;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.core.menu.ModChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, StardewMod.MODID);

    public static final RegistryObject<MenuType<ModChestMenu>> CHEST_MENU =
            MENUS.register("chest",
                    () -> IForgeMenuType.create(ModChestMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

}

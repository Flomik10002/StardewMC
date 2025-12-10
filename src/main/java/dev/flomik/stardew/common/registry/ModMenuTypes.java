package dev.flomik.stardew.common.registry;

import dev.flomik.stardew.core.menu.ModChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final RegistryObject<MenuType<ModChestMenu>> CHEST_MENU =
            StardewRegistry.MENUS.register("chest", () -> IForgeMenuType.create(ModChestMenu::new));


    public static void load() {}
}

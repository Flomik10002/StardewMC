package dev.flomik.stardew.common.module.character.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Обычный {@link Item} (НЕ {@link net.minecraft.world.item.ArmorItem}, см.
 * чат: "оставь подход с обычными айтемами... без армор айтема") —
 * единственное, что делает возможным реально надеть/снять его в CHEST/LEGS -
 * это интерфейс {@link Equipable}: его default-метод
 * {@link #swapWithEquipmentSlot} (переиспользуем как есть, без своей копии)
 * сам находит слот через {@link Equipable#getEquipmentSlot()} и меняет
 * местами руку/слот - ТОЧНО так же, как ваниль делает для настоящей брони,
 * только тут {@code HumanoidArmorLayer} ничего не рисует (та проверяет
 * {@code instanceof ArmorItem}, а не {@code Equipable}) - предмет остаётся
 * невидимым, но при этом полноценно надевается/снимается через ПКМ.
 *
 * Сам предмет — только "указатель": какая именно косметика надета, решает
 * NBT (см. {@link dev.flomik.stardew.common.module.character.ClothingStacks}),
 * а не Java-класс/инстанс - поэтому ОДИН этот класс на слот (shirt/pants)
 * обслуживает произвольно большое число разных "нарядов" (ТЗ - "свапать
 * ОГРОМНОЕ количество разной одежды"), не по одному Item на вариант.
 */
public class ClothingItem extends Item implements Equipable {

    private final EquipmentSlot slot;

    public ClothingItem(EquipmentSlot slot, Properties properties) {
        super(properties);
        this.slot = slot;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return slot;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }
}

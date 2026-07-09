package dev.flomik.stardew.common.module.character;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * NBT-контракт для {@code ModItems.CLOTHING_SHIRT}/{@code CLOTHING_PANTS} —
 * невидимых предметов в CHEST/LEGS-слоте (см. их javadoc), которыми
 * {@link dev.flomik.stardew.common.module.character.network.CharacterCreationServerHandler}
 * "одевает" игрока на сервере, а клиент потом читает эти же теги, чтобы
 * решить между реальной косметикой и "голым" fallback (см.
 * {@code SkinComposer}) при live-изменении экипировки.
 *
 * {@code "none"}/отсутствие тега - тот же смысл, что и
 * {@code CosmeticRegistry}'s {@code "none"}: снятый предмет ИЛИ предмет без
 * тега (что по факту не должно происходить, раз стек создаётся только тут) -
 * трактуются одинаково, "надеть нечего".
 */
public final class ClothingStacks {

    private static final String CLOTHING_ID = "ClothingId";
    private static final String CLOTHING_COLOR = "ClothingColor";

    private ClothingStacks() {
    }

    public static ItemStack create(Item item, String clothingId, int color) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(CLOTHING_ID, clothingId);
        tag.putInt(CLOTHING_COLOR, color);
        return stack;
    }

    /** {@code "none"} и для пустого слота, и для предмета без нашего тега (см. javadoc класса). */
    public static String clothingId(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(CLOTHING_ID)) {
            return "none";
        }
        return stack.getTag().getString(CLOTHING_ID);
    }

    public static int clothingColor(ItemStack stack, int fallback) {
        if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(CLOTHING_COLOR)) {
            return fallback;
        }
        return stack.getTag().getInt(CLOTHING_COLOR);
    }

    /** {@code null}, если тега нет вообще (в отличие от {@link #clothingColor} — тут нужно РАЗЛИЧИТЬ "нет данных" от "нет изменений"). */
    public static Integer clothingColorOrNull(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(CLOTHING_COLOR)) {
            return null;
        }
        return stack.getTag().getInt(CLOTHING_COLOR);
    }
}

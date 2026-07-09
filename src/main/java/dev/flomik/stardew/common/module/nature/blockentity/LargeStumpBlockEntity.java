package dev.flomik.stardew.common.module.nature.blockentity;

import dev.flomik.stardew.common.module.nature.runtime.LargeStumpTracker;
import dev.flomik.stardew.common.module.tools.ToolEnchantment;
import dev.flomik.stardew.common.module.tools.item.ToolAxe;
import dev.flomik.stardew.common.registry.ModBlocks;
import dev.flomik.stardew.common.registry.framework.multiblock.MultiblockPartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Хранит текущее ХП большого пня — но только у клетки-истока (0,0), см.
 * {@link MultiblockPartBlockEntity#getOrigin(Class)}. Урон за удар зависит
 * от тира топора: медь=3, сталь=4, золото=5, иридий=7 (энчант Powerful даёт
 * x1.5). При ХП=20 это даёт ровно 7/5/4/3/2 ударов соответственно. Базовый
 * топор (тир 0) урона не наносит вообще — как в оригинальной Stardew Valley.
 */
public class LargeStumpBlockEntity extends MultiblockPartBlockEntity {

    public static final int MAX_HP = 20;

    // Индекс — tier топора (см. ToolAxe): 0=базовый (не наносит урон), 1=медь, 2=сталь, 3=золото, 4=иридий
    private static final int[] POWER_BY_TIER = {0, 3, 4, 5, 7};
    private static final float POWERFUL_MULTIPLIER = 1.5f;

    private int hp = MAX_HP;

    public LargeStumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public LargeStumpBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlocks.LARGE_STUMP.getTypeValue(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide && isOrigin()) LargeStumpTracker.onLoad(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide && isOrigin()) LargeStumpTracker.onRemove(this);
    }

    public int getHp() {
        return brain().hp;
    }

    public void resetHp() {
        LargeStumpBlockEntity brain = brain();
        brain.hp = MAX_HP;
        brain.setChanged();
    }

    /**
     * Наносит удар выбранным инструментом (можно бить по любой из клеток —
     * урон всегда применяется к общему ХП структуры).
     *
     * @return true, если этим ударом пень полностью сломан
     */
    public boolean hit(ItemStack tool) {
        int power = powerOf(tool);
        if (power <= 0) return false;

        LargeStumpBlockEntity brain = brain();
        brain.hp -= power;
        brain.setChanged();
        return brain.hp <= 0;
    }

    private LargeStumpBlockEntity brain() {
        LargeStumpBlockEntity origin = getOrigin(LargeStumpBlockEntity.class);
        return origin != null ? origin : this;
    }

    public static boolean isAxe(ItemStack tool) {
        return tool.getItem() instanceof ToolAxe;
    }

    public static boolean canDamage(ItemStack tool) {
        return powerOf(tool) > 0;
    }

    private static int powerOf(ItemStack tool) {
        if (!(tool.getItem() instanceof ToolAxe axe)) return 0;

        int tier = axe.getTier();
        if (tier <= 0 || tier >= POWER_BY_TIER.length) return 0;

        float power = POWER_BY_TIER[tier];
        if (tool.hasTag() && tool.getTag().getBoolean(ToolEnchantment.POWERFUL.getNbtKey())) {
            power *= POWERFUL_MULTIPLIER;
        }

        return Math.round(power);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("hp", hp);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        hp = tag.contains("hp") ? tag.getInt("hp") : MAX_HP;
    }
}

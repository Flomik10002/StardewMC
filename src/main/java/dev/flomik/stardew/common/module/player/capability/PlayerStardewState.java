package dev.flomik.stardew.common.module.player.capability;

import dev.flomik.stardew.core.network.PacketHandler;
import dev.flomik.stardew.common.module.player.network.S2CSyncPlayerState;
import dev.flomik.stardew.common.module.time.StardewTimeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class PlayerStardewState {
    public static final float BASE_MAX = 270.0f;

    private float currentEnergy = 270.0f;
    private float maxEnergy = 270.0f;
    private boolean isExhausted = false;

    // Wallet System
    private long money = 0;
    private long totalEarnings = 0;

    // TODO: [MECHANIC] Implement Skills & Experience
    // Map<SkillType, Integer> experience;
    // Map<SkillType, Integer> level;
    // List<Profession> professions;
    // void addExperience(SkillType skill, int amount);

    private final Player player;

    public PlayerStardewState(Player player) {
        this.player = player;
    }

    // --- Energy Getters ---
    public float getCurrentEnergy() { return currentEnergy; }
    public float getMaxEnergy() { return maxEnergy; }
    public boolean isExhausted() { return isExhausted; }

    // --- Wallet Getters ---
    public long getMoney() { return money; }
    public long getTotalEarnings() { return totalEarnings; }

    // --- Energy Actions ---

    public void consumeEnergy(float amount) {
        if (player.isCreative()) return;

        float previousEnergy = this.currentEnergy;
        this.currentEnergy -= amount;

        // Предупреждение о низкой энергии
        // По вики: "When energy first falls below 15, a notice appears"
        if (previousEnergy > 15 && currentEnergy <= 15) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cYou're starting to feel exhausted."));
        }

        // Устанавливаем флаг усталости при достижении 0 энергии (Exhaustion)
        // По вики: "The player becomes exhausted upon reaching 0 energy"
        // Флаг остается до конца дня (снимается только сном или специальными предметами)
        if (currentEnergy <= 0 && !isExhausted) {
            isExhausted = true;
            // TODO: Отправить сообщение в чат "You feel sluggish from over-exertion."
        }

        // Обморок при -15
        if (currentEnergy <= -15.0f) {
            // Логика обморока должна вызываться из EventTick,
            // здесь мы просто фиксируем значение
            currentEnergy = -15.0f;
        }

        sync();
    }

    public void restoreEnergy(float amount) {
        this.currentEnergy = Math.min(this.currentEnergy + amount, this.maxEnergy);
        sync();
    }

    /**
     * Прямая установка энергии (для админских команд).
     * Обходит все ограничения и проверки.
     */
    public void setEnergy(float amount) {
        this.currentEnergy = amount;
        sync();
    }

    /**
     * Установка максимальной энергии (для админских команд).
     * Если текущая энергия больше нового максимума, она будет ограничена.
     */
    public void setMaxEnergy(float amount) {
        this.maxEnergy = Math.max(0, amount);
        // Если текущая энергия больше нового максимума, обрезаем её
        if (this.currentEnergy > this.maxEnergy) {
            this.currentEnergy = this.maxEnergy;
        }
        sync();
    }

    /**
     * Рассчитывает энергию на утро на основе времени, когда игрок лег спать.
     * Использует StardewTimeUtils для расчета времени.
     * @param sleepTick Время мира в момент засыпания
     */
    public void calculateSleepRestoration(long sleepTick) {
        float restorationRate = 1.0f; // Изначально 100%

        // 1. Штраф за истощение (Exhaustion) -50%
        if (isExhausted) {
            restorationRate -= 0.5f;
            isExhausted = false; // Снимаем эффект на утро (штраф применен)
        }

        // 2. Штраф за позднее время (после полуночи)
        // В Stardew Valley: после 24:00 (0:00) штраф 2.5% за каждые 10 минут
        int hour = StardewTimeUtils.getHour(sleepTick);
        int minute = StardewTimeUtils.getMinute(sleepTick);

        // В Stardew системе: hour 0 = 24:00 (полночь), hour 1 = 25:00 (1:00)
        // Если час < 6, это значит после полуночи (0, 1, 2 - это 24:00, 25:00, 26:00)
        if (hour < 6) {
            // Преобразуем в минуты после полуночи (hour 0 = 0 минут, hour 1 = 60 минут, и т.д.)
            int minutesPastMidnight = hour * 60 + minute;

            // Штраф: 2.5% (0.025) за каждые 10 минут после полуночи
            float timePenalty = (minutesPastMidnight / 10.0f) * 0.025f;

            // Ограничиваем штраф максимумом -50% (достигается к 2:00 AM)
            timePenalty = Math.min(timePenalty, 0.5f);

            restorationRate -= timePenalty;
        }

        // 3. Применяем восстановление
        // Энергия на утро = Максимум * Коэффициент восстановления
        this.currentEnergy = maxEnergy * restorationRate;

        sync();
    }

    public void passOutFromExhaustion() {
        // Штраф при обмороке: просыпаемся с 50% энергии (или меньше, если были истощены)
        // Обычно в SV это половина макс энергии.
        this.currentEnergy = Math.max(0, maxEnergy * 0.5f);
        this.isExhausted = false;
        sync();
    }

    // --- Wallet Actions ---

    public void setMoney(long amount) {
        long diff = amount - this.money;
        if (diff > 0) {
            this.totalEarnings += diff;
        }
        this.money = Math.max(0, amount);
        sync();
    }

    public void addMoney(long amount) {
        if (amount <= 0) return;
        this.money += amount;
        this.totalEarnings += amount;
        sync();
    }

    public boolean trySpendMoney(long amount) {
        if (amount < 0) return false;
        if (this.money >= amount) {
            this.money -= amount;
            sync();
            return true;
        }
        return false;
    }

    // --- Tech ---

    public void copyFrom(PlayerStardewState source) {
        this.currentEnergy = source.currentEnergy;
        this.maxEnergy = source.maxEnergy;
        this.isExhausted = source.isExhausted;
        this.money = source.money;
        this.totalEarnings = source.totalEarnings;
    }

    public void saveNBT(CompoundTag tag) {
        tag.putFloat("CurrentEnergy", currentEnergy);
        tag.putFloat("MaxEnergy", maxEnergy);
        tag.putBoolean("Exhausted", isExhausted);
        tag.putLong("Money", money);
        tag.putLong("TotalEarnings", totalEarnings);
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("CurrentEnergy")) {
            this.currentEnergy = tag.getFloat("CurrentEnergy");
            this.maxEnergy = tag.getFloat("MaxEnergy");
            this.isExhausted = tag.getBoolean("Exhausted");
        }
        if (tag.contains("Money")) {
            this.money = tag.getLong("Money");
            this.totalEarnings = tag.getLong("TotalEarnings");
        }
    }

    public void sync() {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToPlayer(new S2CSyncPlayerState(currentEnergy, maxEnergy, isExhausted, money, totalEarnings), serverPlayer);
        }
    }

    public void setClientData(float current, float max, boolean exhausted) {
        this.currentEnergy = current;
        this.maxEnergy = max;
        this.isExhausted = exhausted;
    }
}

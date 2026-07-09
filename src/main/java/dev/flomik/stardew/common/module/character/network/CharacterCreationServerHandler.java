package dev.flomik.stardew.common.module.character.network;

import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.common.module.character.ClothingStacks;
import dev.flomik.stardew.common.module.character.StardewWorldMarker;
import dev.flomik.stardew.common.module.character.capability.CharacterProfileProvider;
import dev.flomik.stardew.common.module.character.cosmetic.CosmeticRegistry;
import dev.flomik.stardew.common.module.player.capability.PlayerProvider;
import dev.flomik.stardew.common.module.player.capability.PlayerStardewState;
import dev.flomik.stardew.common.module.time.Season;
import dev.flomik.stardew.common.module.time.StardewDateData;
import dev.flomik.stardew.common.registry.ModItems;
import dev.flomik.stardew.core.network.PacketHandler;
import dev.flomik.stardew.mixin.common.PrimaryLevelDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Серверная валидация профиля персонажа (ТЗ §33) — не доверяет клиенту
 * вслепую. Косметические ID мягко подменяются на fallback через
 * {@link CosmeticRegistry} (ТЗ §57 "удалённый ID -> fallback, а не падение"),
 * а не отклоняются; жёстко отклоняется только некорректное имя (ТЗ §42/§10.1).
 */
final class CharacterCreationServerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterCreationServerHandler.class);

    private static final int MAX_NAME_LENGTH = 24;
    private static final int MAX_FARM_NAME_LENGTH = 32;
    private static final int MAX_FAVORITE_THING_LENGTH = 48;

    private CharacterCreationServerHandler() {
    }

    static void handle(ServerPlayer player, C2SFinishCharacterCreation msg) {
        String rejection = validateName(msg.getName());
        if (rejection != null) {
            PacketHandler.sendToPlayer(new S2CCharacterCreationRejected(rejection), player);
            return;
        }

        player.getCapability(CharacterProfileProvider.CHARACTER_PROFILE_CAPABILITY).ifPresent(profile -> {
            profile.setInitializationState(CharacterProfile.InitializationState.IN_PROGRESS);

            profile.setName(sanitize(msg.getName(), MAX_NAME_LENGTH));
            profile.setFarmName(sanitize(msg.getFarmName(), MAX_FARM_NAME_LENGTH));
            profile.setFavoriteThing(sanitize(msg.getFavoriteThing(), MAX_FAVORITE_THING_LENGTH));
            profile.setAnimalPreference(msg.getAnimalPreference());
            profile.setModelType(msg.getModelType());

            profile.setSkinId(CosmeticRegistry.isValidSkin(msg.getSkinId()) ? msg.getSkinId() : CosmeticRegistry.skinIds().get(0));
            profile.setEyeColor(msg.getEyeColor());
            profile.setHairId(CosmeticRegistry.isValidHair(msg.getHairId()) ? msg.getHairId() : CosmeticRegistry.hairIds().get(0));
            profile.setHairColor(msg.getHairColor());
            profile.setShirtId(CosmeticRegistry.isValidShirt(msg.getShirtId()) ? msg.getShirtId() : CosmeticRegistry.shirtIds().get(0));
            profile.setShirtColor(msg.getShirtColor());
            profile.setPantsId(CosmeticRegistry.isValidPants(msg.getPantsId()) ? msg.getPantsId() : CosmeticRegistry.pantsIds().get(0));
            profile.setPantsColor(msg.getPantsColor());
            profile.setAccessoryId(CosmeticRegistry.isValidAccessory(msg.getAccessoryId()) ? msg.getAccessoryId() : "none");
            profile.setFarmType(msg.getFarmType());

            profile.setInitializationState(CharacterProfile.InitializationState.COMPLETED);

            resetTemplateStateOnce(player);
            renameWorldToFarmName(player, profile.getFarmName());
            equipClothing(player, profile);

            PacketHandler.sendToPlayer(new S2CCharacterCreationAccepted(profile), player);
        });
    }

    /**
     * "Одевает" игрока в CHEST/LEGS (см. чат: "нам нужно выдавать игроку в
     * слоты одежды рубашку и штаны") - предметы сами по себе НИКОГДА не
     * рендерятся (см. {@code ModItems.CLOTHING_SHIRT} javadoc), это просто
     * носитель NBT ({@code ClothingId}/{@code ClothingColor}, см.
     * {@code ClothingStacks}), который клиент читает при снятии/надевании
     * (см. {@code ClothingEquipmentListener}), чтобы решить между реальной
     * одеждой и "голым" fallback - именно поэтому "голым" можно оказаться
     * ТОЛЬКО в игре (сняв предмет), а не в самом редакторе создания
     * персонажа (тот вообще не трогает эквип, только профиль).
     */
    private static void equipClothing(ServerPlayer player, CharacterProfile profile) {
        ItemStack shirt = ClothingStacks.create(ModItems.CLOTHING_SHIRT.get(), profile.getShirtId(), profile.getPantsColor());
        ItemStack pants = ClothingStacks.create(ModItems.CLOTHING_PANTS.get(), profile.getPantsId(), profile.getPantsColor());
        player.setItemSlot(EquipmentSlot.CHEST, shirt);
        player.setItemSlot(EquipmentSlot.LEGS, pants);
    }

    /**
     * Название фермы = заголовок мира (то, что показывает список миров) —
     * мир на момент этого вызова УЖЕ создан и сервер УЖЕ запущен (см.
     * {@code WorldTemplateManager}/{@code StardewPlayButtonHandler} — имя на
     * старте всегда заглушка "Stardew Valley", реальное имя фермы появляется
     * только тут). Просто переписать {@code level.dat} на диске бесполезно —
     * {@link LevelSettings} держится в памяти ({@link PrimaryLevelData}) и
     * следующий автосейв затрёт файл обратно СТАРЫМ именем; поэтому меняем
     * именно in-memory {@code settings} через {@link PrimaryLevelDataAccessor}
     * (у {@link LevelSettings} нет {@code withLevelName(...)}, только полная
     * пересборка с тем же остальным), и сразу форсируем полное сохранение —
     * страховка на случай, если игрок выйдет/Alt+F4 раньше следующего
     * обычного автосейва.
     */
    private static void renameWorldToFarmName(ServerPlayer player, String farmName) {
        if (farmName == null || farmName.isBlank()) return;

        MinecraftServer server = player.getServer();
        WorldData worldData = server.getWorldData();
        if (!(worldData instanceof PrimaryLevelData levelData)) return;

        PrimaryLevelDataAccessor accessor = (PrimaryLevelDataAccessor) levelData;
        LevelSettings current = accessor.getSettings();
        if (farmName.equals(current.levelName())) return;

        accessor.setSettings(new LevelSettings(farmName, current.gameType(), current.hardcore(), current.difficulty(),
                current.allowCommands(), current.gameRules(), current.getDataConfiguration(), current.getLifecycle()));

        try {
            server.saveEverything(true, true, true);
        } catch (Exception e) {
            LOGGER.warn("[Stardew] Failed to force-save world after renaming to farm name", e);
        }
    }

    /**
     * Шаблон (map/SV) - это реальный мир, в котором его создатель играл и
     * оставил свой прогресс (дату, деньги) - копия наследует это как есть
     * (ТЗ по world-template: "не резать руками сырой NBT, сбросить кодом при
     * первом входе"). Делается один раз за жизнь мира ({@link StardewWorldMarker#isTemplateStateReset()}),
     * иначе повторное прохождение creator'а (например {@code /stardew debug
     * resetcharacter}) стирало бы уже накопленный игроком прогресс.
     */
    private static void resetTemplateStateOnce(ServerPlayer player) {
        StardewWorldMarker marker = StardewWorldMarker.get(player.serverLevel());
        if (marker.isTemplateStateReset()) return;
        marker.markTemplateStateReset();

        StardewDateData.get(player.serverLevel()).setDate(Season.SPRING, 1);

        player.getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(PlayerStardewState::resetToNewGame);
    }

    /** @return translation key причины отказа, либо {@code null} если имя валидно (ТЗ §10.1). */
    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            return "gui.stardew.character.error.name_empty";
        }
        for (int i = 0; i < name.length(); i++) {
            if (Character.isISOControl(name.charAt(i))) {
                return "gui.stardew.character.error.name_invalid";
            }
        }
        return null;
    }

    private static String sanitize(String value, int maxLength) {
        if (value == null) return "";
        String trimmed = value.strip();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}

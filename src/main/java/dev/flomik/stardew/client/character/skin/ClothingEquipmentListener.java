package dev.flomik.stardew.client.character.skin;

import dev.flomik.stardew.StardewMod;
import dev.flomik.stardew.client.screen.character.CharacterCreationScreen;
import dev.flomik.stardew.common.module.character.ClothingStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

/**
 * Живое обновление скина по факту надетой/снятой одежды (см. чат: "в
 * редакторе голым быть нельзя. только в игре") — реальная механика "снять
 * рубашку/штаны" тут, у {@code SkinComposer}/{@code RuntimeSkinManager} нет.
 *
 * {@link net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent}
 * тут НЕ используется, хоть и выглядит подходящим — он документированно
 * "fired on server-side only", клиенту недоступен. Поэтому вместо события —
 * дешёвое сравнение по клиентскому тику: {@link ClothingStacks#clothingId}
 * для CHEST/LEGS у ЛОКАЛЬНОГО игрока, пересборка ТОЛЬКО когда что-то реально
 * изменилось (не каждый тик).
 */
@Mod.EventBusSubscriber(modid = StardewMod.MODID, value = Dist.CLIENT)
public final class ClothingEquipmentListener {

    private static String lastShirtId = null;
    private static String lastPantsId = null;
    /**
     * Отдельно от id - см. чат: "надену шорты одного цвета, потом другие
     * совсем другого цвета - будет работать?". Раньше цвет вообще не
     * отслеживался: смена штанов с ТЕМ ЖЕ id (тот же паттерн, другой
     * ClothingColor) вообще не считалась изменением и не пересобирала скин.
     */
    private static Integer lastPantsColor = null;

    private ClothingEquipmentListener() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        // Пока открыт редактор - реальные слоты CHEST/LEGS игрока ещё ПУСТЫЕ
        // (сервер надевает их только в CharacterCreationServerHandler.equipClothing,
        // ПОСЛЕ "OK" - см. её javadoc), а превью уже показывает выбранную в
        // драфте одежду через RuntimeSkinManager.refresh(draft). Без этой
        // проверки тик читает пустые слоты как "none"/"none" и тут же
        // затирает корректное превью редактора на голое тело (см. чат: "в
        // редакторе голым быть нельзя. только в игре").
        if (Minecraft.getInstance().screen instanceof CharacterCreationScreen) {
            return;
        }

        String shirtId = ClothingStacks.clothingId(player.getItemBySlot(EquipmentSlot.CHEST));
        String pantsId = ClothingStacks.clothingId(player.getItemBySlot(EquipmentSlot.LEGS));
        Integer pantsColor = ClothingStacks.clothingColorOrNull(player.getItemBySlot(EquipmentSlot.LEGS));

        if (Objects.equals(shirtId, lastShirtId) && Objects.equals(pantsId, lastPantsId) && Objects.equals(pantsColor, lastPantsColor)) {
            return;
        }
        lastShirtId = shirtId;
        lastPantsId = pantsId;
        lastPantsColor = pantsColor;
        RuntimeSkinManager.refreshEquipmentOverride(shirtId, pantsId, pantsColor);
    }
}

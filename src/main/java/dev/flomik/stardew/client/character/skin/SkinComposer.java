package dev.flomik.stardew.client.character.skin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.flomik.stardew.common.module.character.CharacterModelType;
import dev.flomik.stardew.common.module.character.CharacterProfile;
import dev.flomik.stardew.common.module.character.cosmetic.CosmeticDefinition;
import dev.flomik.stardew.common.module.character.cosmetic.CosmeticRegistry;
import dev.flomik.stardew.common.module.character.cosmetic.CosmeticVariant;
import dev.flomik.stardew.common.module.character.cosmetic.SkinToneDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Собирает 64×64 Minecraft-скин из {@link CharacterProfile} (ТЗ §24-§29).
 * Источник истины — профиль, а не PNG (ТЗ §66): этот класс можно вызвать
 * заново в любой момент и получить идентичный результат.
 *
 * Для одежды {@link CosmeticVariant#mask()} — grayscale+alpha слой
 * перекраски, а {@link CosmeticVariant#texture()} — неизменяемый overlay;
 * {@link CosmeticDefinition#variantFor} выбирает male/female-вариант под
 * {@link CharacterProfile#getModelType()} (руки classic/slim разной ширины -
 * см. javadoc {@link CosmeticDefinition}). Каждая категория рисуется в СВОЁМ
 * ограниченном наборе областей скина (см.
 * {@code applyCosmetic}'s {@code allowedRegions}) - иначе лишний непрозрачный
 * пиксель в ассете утёк бы на скин без ограничений (ваниль рендерит layer2
 * ног/тела/рук всегда, вне зависимости от того, знает ли наш код об этой
 * зоне). Простой placeholder по областям тела остаётся только для рубашки,
 * пока для неё нет реальных текстур - для штанов убран, реальные ассеты уже
 * подключены (см. {@code character/clothes/down}).
 *
 * Не худеет/не толстеет — {@link CharacterProfile#getModelType()} влияет
 * ТОЛЬКО на выбор male/female-ассета (в т.ч. {@code SKIN_MASK_MALE}/
 * {@code SKIN_MASK_FEMALE} - см. compose()), а не на геометрию тела (та не
 * завязана на ширину рук).
 */
public final class SkinComposer {

    private static final int SIZE = 64;
    /** Ботинки - универсальный слой ног, не привязан к конкретному типу низа одежды (см. compose()). */
    private static final String BOOTS_TEXTURE = "character/clothes/down/boots.png";
    /**
     * Глаза - единственный фиксированный ассет (не выбираемый по id, в
     * отличие от волос/рубашек), поэтому не в {@code CosmeticRegistry}, а
     * прямо тут, как и {@link #BOOTS_TEXTURE}. {@code EYES_TEXTURE_MASK} -
     * перекрашиваемая радужка (см. {@code profile.getEyeColor()}),
     * {@code EYES_TEXTURE_BASE} - неизменяемый контур поверх неё.
     */
    private static final String EYES_TEXTURE_MASK = "character/eyes/eyes_mask.png";
    private static final String EYES_TEXTURE_BASE = "character/eyes/eyes.png";
    /**
     * Кожа красится ТОЧНО так же, как волосы - ОДНА перекрашиваемая маска,
     * никакой отдельной "фиксированной" текстуры (см. чат и
     * asset-tools/skin_tint_map_generator). Male/female тут - про НЕ ширину
     * рук (та не влияет на заливку тела вообще, см. класс javadoc), а про
     * то, какая из двух масок используется - обе сгенерены из РАЗНЫХ по
     * яркости исходников (skin_male.png тёмный, skin_female.png светлый),
     * но центрированы на СВОЕЙ медиане (см. генератор), так что в паре с
     * {@link dev.flomik.stardew.common.module.character.cosmetic.SkinToneDefinition#base()}
     * дают одинаковый результат независимо от пола.
     */
    private static final String SKIN_MASK_MALE = "character/skin/skin_mask_male.png";
    private static final String SKIN_MASK_FEMALE = "character/skin/skin_mask_female.png";

    /**
     * Фикс-цвет overlay (НЕ tint-маска - красить тут нечего, см. чат) на
     * случай "нет рубашки"/"нет штанов" ({@code shirtId/pantsId == "none"}) -
     * без них applyCosmetic естественно возвращает false (см. compose()), и
     * раньше в этом случае рубашка красилась плоской заливкой цветом штанов
     * (грубая заглушка без реальных ассетов), а штаны - никак вообще. Теперь
     * вместо заглушки/пустоты рисуется настоящий "голый" низ/верх.
     */
    private static final String NO_CLOTHES_PANTIES = "character/clothes/no_clothes/panties.png";
    private static final String NO_CLOTHES_SHIRT_MALE = "character/clothes/no_clothes/shirt_male.png";
    private static final String NO_CLOTHES_SHIRT_FEMALE = "character/clothes/no_clothes/shirt_female.png";

    private SkinComposer() {
    }

    public static NativeImage compose(CharacterProfile profile) {
        // ВАЖНО: последний boolean у NativeImage - это useCalloc. false тут
        // означает nmemAlloc (НЕобнулённая память - мусор от чужих
        // предыдущих native-аллокаций, будь то старый шрифтовый атлас, чья-то
        // текстура и т.п.), а НЕ "и так уже нули", как раньше ошибочно
        // считалось в этом комментарии. Именно так на скин попадал случайный
        // мусор (текст/паттерны из совсем других текстур) в областях, которые
        // мы сами явно не закрашиваем (layer2 тела/рук) - причём РАЗНЫЙ при
        // каждом пересборе, поэтому выглядело как "не чистится"/"стакается".
        // true = nmemCalloc - гарантированно обнулённый (прозрачный) буфер.
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, SIZE, SIZE, true);

        CharacterModelType modelType = profile.getModelType();

        SkinToneDefinition skin = CosmeticRegistry.skin(profile.getSkinId());
        String skinMaskPath = modelType == CharacterModelType.SLIM ? SKIN_MASK_FEMALE : SKIN_MASK_MALE;
        applyTintMask(image, skinMaskPath, skin.base(), buildAllowedMask(SKIN_REGIONS));

        // Волосы - ВЫСШИЙ приоритет из всех категорий (рисуются последними,
        // см. конец метода), поэтому их occupancy считается ПЕРВЫМ, ещё до
        // одежды - точно так же, как рубашка исключает штаны ниже, только
        // теперь волосы исключают ВСЁ (одежду и глаза). У волос нет своей
        // зоны (unrestrictedMask) - длинная причёска может лечь на тело/руки
        // совершенно как угодно, поэтому мирроринг occupancy между layer1 и
        // layer2 нужен НЕ ТОЛЬКО для ног (как у рубашки), а для головы,
        // тела и обеих рук тоже - см. mirrorLayerOccupancy.
        CosmeticDefinition hairDefinition = CosmeticRegistry.hair(profile.getHairId());
        boolean[] hairMask = unrestrictedMask();
        boolean[] hairOccupancy = occupancyMask(hairDefinition, modelType, hairMask);
        mirrorLayerOccupancy(hairOccupancy);

        // Рубашке разрешено заходить на зону штанов (длинные туники/платья
        // "верхом", см. javadoc SHIRT_REGIONS) - и там, где у рубашки есть
        // ХОТЬ КАКАЯ-ТО непрозрачность (даже частичная, на антиалиасинге),
        // штанам и ботинкам ТУДА ПОПАДАТЬ ЗАПРЕЩЕНО СОВСЕМ - не "рисуются
        // раньше и потом перекрываются" (это давало бы блендинг цветов на
        // полупрозрачных краевых пикселях рубашки), а физически вычитаются
        // из их allowed-маски ДО отрисовки. Поэтому occupancy рубашки
        // считается ПЕРВЫМ (до штанов/ботинок), хотя сама рубашка рисуется
        // на скин последней.
        CosmeticDefinition shirtDefinition = CosmeticRegistry.shirt(profile.getShirtId());
        boolean[] shirtMask = buildAllowedMask(SHIRT_REGIONS);
        subtract(shirtMask, hairOccupancy);
        boolean[] shirtOccupancy = occupancyMask(shirtDefinition, modelType, shirtMask);
        // Layer1 и layer2 одной и той же ноги - это внутренняя/внешняя
        // поверхность ОДНОГО и того же куска ноги, не две независимые зоны;
        // если художник нарисовал напуск рубашки только в одном из них
        // (частая ситуация - забыли/не подумали про второй), второй слой
        // остался бы неисключённым для штанов/юбки/платья на той же самой
        // видимой высоте ноги. Зеркалим occupancy между парой.
        mirrorLayerOccupancy(shirtOccupancy);

        boolean[] legMask = buildAllowedMask(PANTS_REGIONS);
        subtract(legMask, shirtOccupancy);
        subtract(legMask, hairOccupancy);

        // Ботинки - ВСЕГДА первый слой на ногах сразу после кожи, независимо
        // от того, штаны/шорты/юбка/платье выбраны - не привязаны к
        // конкретному CosmeticDefinition, поэтому рисуются тут напрямую, а не
        // через applyCosmetic. Маска цвета низа одежды (ниже) рисуется ПОСЛЕ
        // и перекрывает ботинки везде, где сама непрозрачна - по краям
        // (голень/лодыжка) ботинки остаются видны.
        applyOverlay(image, BOOTS_TEXTURE, legMask);

        // "Нет штанов" (profile.getPantsId() == "none", см. CosmeticRegistry
        // allowNoneOnly) - applyCosmetic возвращает false (variant без
        // texture/mask), рисуем NO_CLOTHES_PANTIES вместо пустоты. hasPants
        // (реально ли что-то надето, а не голая заглушка) нужен ниже для
        // рубашки - см. applyShirtCosmetic.
        boolean hasPants = applyCosmetic(image, CosmeticRegistry.pants(profile.getPantsId()), profile.getPantsColor(), modelType, legMask);
        if (!hasPants) {
            applyOverlay(image, NO_CLOTHES_PANTIES, legMask);
        }

        // Рубашка красится в ЦВЕТ ШТАНОВ (profile.getPantsColor()), а НЕ в
        // свой отдельный цвет - отдельного слайдера "Shirt Color" в GUI нет
        // и не будет (явное решение), profile.getShirtColor() тут намеренно
        // не используется для тонирования маски.
        //
        // "Нет рубашки" (см. выше про штаны, тот же принцип) - раньше тут
        // была плоская заливка цветом штанов (грубая заглушка без реальных
        // ассетов), теперь настоящий "голый торс", свой на male/female (см.
        // NO_CLOTHES_SHIRT_MALE/FEMALE).
        if (!applyShirtCosmetic(image, shirtDefinition, profile.getPantsColor(), modelType, shirtMask, hasPants)) {
            String noShirtTexture = modelType == CharacterModelType.SLIM ? NO_CLOTHES_SHIRT_FEMALE : NO_CLOTHES_SHIRT_MALE;
            applyOverlay(image, noShirtTexture, shirtMask);
        }

        // Порядок ПОСЛЕ одежды: сначала глаза, потом волосы поверх всего -
        // так чёлка/длинные пряди могут лечь поверх лица/плеч рубашки. Глаза
        // тоже уступают волосам (чёлка, закрывающая глаза, - обычное дело).
        boolean[] eyesMask = buildAllowedMask(EYES_REGIONS);
        subtract(eyesMask, hairOccupancy);
        boolean eyesApplied = applyTintMask(image, EYES_TEXTURE_MASK, profile.getEyeColor(), eyesMask);
        eyesApplied |= applyOverlay(image, EYES_TEXTURE_BASE, eyesMask);
        if (!eyesApplied) {
            drawEyes(image, withAlpha(profile.getEyeColor()));
        }

        // ВАЖНО: у волос, в отличие от одежды, НЕТ ограничения по области
        // скина - причёска рисуется ПОСЛЕДНЕЙ и может занимать любые
        // пиксели (длинные волосы на плечах/спине, чёлка на лице и т.п.),
        // поэтому маска тут не строится через buildAllowedMask(регионы), а
        // берётся полностью разрешающей (см. unrestrictedMask()) - та же
        // hairMask, которой считалась occupancy выше.
        if (!applyCosmetic(image, hairDefinition, profile.getHairColor(), modelType, hairMask)) {
            int hairColor = withAlpha(profile.getHairColor());
            fillRegion(image, MinecraftSkinLayout.HEAD_LAYER2, hairColor);
        }

        // Аксессуары (ТЗ §16) в v1 визуально не отрисовываются - без реальных
        // текстур позиционировать их осмысленно негде; профиль хранит
        // accessoryId уже сейчас, отрисовка добавится вместе с ассетами.

        return image;
    }

    private static void drawEyes(NativeImage image, int eyeColor) {
        MinecraftSkinLayout.Rect front = MinecraftSkinLayout.HEAD_LAYER1[MinecraftSkinLayout.FACE_FRONT];
        // Простая пара точек на уровне глаз внутри лицевой грани головы 8x8.
        setPixel(image, front.x() + 2, front.y() + 3, eyeColor);
        setPixel(image, front.x() + 5, front.y() + 3, eyeColor);
    }

    private static void fillRegion(NativeImage image, MinecraftSkinLayout.Rect[] faces, int color) {
        for (MinecraftSkinLayout.Rect face : faces) {
            fillRect(image, face, color);
        }
    }

    private static void fillRect(NativeImage image, MinecraftSkinLayout.Rect rect, int color) {
        for (int dy = 0; dy < rect.h(); dy++) {
            for (int dx = 0; dx < rect.w(); dx++) {
                setPixel(image, rect.x() + dx, rect.y() + dy, color);
            }
        }
    }

    private static void setPixel(NativeImage image, int x, int y, int nativeColor) {
        image.setPixelRGBA(x, y, nativeColor);
    }

    // Каждой категории одежды разрешено красить ТОЛЬКО её собственную зону
    // скина - без этого applyTintMask/applyOverlay это сырой блит всего
    // файла 1:1 на координаты скина, и любой лишний непрозрачный пиксель в
    // ассете (например, случайно оставленный в layer2 ноги при экспорте из
    // другого шаблона) утёк бы на скин НЕОГРАНИЧЕННО - это и есть источник
    // "багающихся слоёв": ваниль всегда рендерит layer2-кубы ног/тела/рук по
    // этим UV, независимо от того, знает ли наш код об этой зоне.
    // Ноги ВКЛЮЧЕНЫ намеренно - некоторым рубашкам (длинные туники, платья
    // "верхом") разрешено заходить на зону штанов, обрезать это не нужно.
    // Штаны рисуются ПОСЛЕ рубашки в compose() и перекрывают её там, где
    // сами непрозрачны - там, где штаны/шорты не доходят (напр. шорты
    // короче туники), низ рубашки остаётся виден, как и должно быть.
    private static final MinecraftSkinLayout.Rect[][] SHIRT_REGIONS = {
            MinecraftSkinLayout.BODY_LAYER1,
            MinecraftSkinLayout.RIGHT_ARM_LAYER1,
            MinecraftSkinLayout.LEFT_ARM_LAYER1,
            MinecraftSkinLayout.BODY_LAYER2,
            MinecraftSkinLayout.RIGHT_ARM_LAYER2,
            MinecraftSkinLayout.LEFT_ARM_LAYER2,
            MinecraftSkinLayout.RIGHT_LEG_LAYER1,
            MinecraftSkinLayout.LEFT_LEG_LAYER1,
            MinecraftSkinLayout.RIGHT_LEG_LAYER2,
            MinecraftSkinLayout.LEFT_LEG_LAYER2
    };
    private static final MinecraftSkinLayout.Rect[][] PANTS_REGIONS = {
            MinecraftSkinLayout.RIGHT_LEG_LAYER1,
            MinecraftSkinLayout.LEFT_LEG_LAYER1,
            MinecraftSkinLayout.RIGHT_LEG_LAYER2,
            MinecraftSkinLayout.LEFT_LEG_LAYER2
    };
    /** Глаза рисуются на лицевой стороне головы - тот же принцип "своя зона", что у одежды выше. */
    private static final MinecraftSkinLayout.Rect[][] EYES_REGIONS = {
            MinecraftSkinLayout.HEAD_LAYER1
    };
    /** Кожа - только layer1 всех частей тела (то же, что раньше заливала fillBodyLayer1WithSkin); layer2 - зона одежды, кожи там не видно. */
    private static final MinecraftSkinLayout.Rect[][] SKIN_REGIONS = {
            MinecraftSkinLayout.HEAD_LAYER1,
            MinecraftSkinLayout.BODY_LAYER1,
            MinecraftSkinLayout.RIGHT_ARM_LAYER1,
            MinecraftSkinLayout.LEFT_ARM_LAYER1,
            MinecraftSkinLayout.RIGHT_LEG_LAYER1,
            MinecraftSkinLayout.LEFT_LEG_LAYER1
    };

    /**
     * В отличие от одежды/глаз, у волос НЕТ своей зоны — причёска рисуется
     * последней и вправе занимать любые пиксели скина (длинные волосы на
     * плечах/спине, чёлка на лице и т.п.), поэтому вместо
     * {@code buildAllowedMask(регионы)} используется полностью разрешающая
     * маска.
     */
    private static boolean[] unrestrictedMask() {
        boolean[] allowed = new boolean[SIZE * SIZE];
        Arrays.fill(allowed, true);
        return allowed;
    }

    private static boolean[] buildAllowedMask(MinecraftSkinLayout.Rect[][] regions) {
        boolean[] allowed = new boolean[SIZE * SIZE];
        for (MinecraftSkinLayout.Rect[] group : regions) {
            for (MinecraftSkinLayout.Rect rect : group) {
                for (int dy = 0; dy < rect.h(); dy++) {
                    for (int dx = 0; dx < rect.w(); dx++) {
                        allowed[(rect.y() + dy) * SIZE + (rect.x() + dx)] = true;
                    }
                }
            }
        }
        return allowed;
    }

    /**
     * Layer1/layer2 одной и той же части тела - геометрически ОДИН физический
     * кусок (внутренний куб / чуть больший внешний "напуск"), просто ДВЕ
     * РАЗНЫЕ UV-зоны в 64×64 текстуре, каждая пара - это layer1, сдвинутый на
     * фиксированное (dx,dy) до layer2 (см. те же смещения в определениях
     * {@code MinecraftSkinLayout}, напр. {@code RIGHT_LEG_LAYER2}/
     * {@code LEFT_LEG_LAYER2}). Если один из двух занят, помечаем занятым и
     * парный пиксель - иначе пропуск в одной из проекций (художник
     * нарисовал только один слой) оставлял бы лазейку на той же самой
     * видимой части тела.
     */
    private record LayerBoundsPair(int x, int y, int w, int h, int dx, int dy) {
    }

    private static final LayerBoundsPair[] LAYER_PAIRS = {
            new LayerBoundsPair(0, 0, 32, 16, 32, 0),    // голова: layer2 = layer1 +32 по X
            new LayerBoundsPair(16, 16, 24, 16, 0, 16),  // тело: layer2 = layer1 +16 по Y
            new LayerBoundsPair(40, 16, 16, 16, 0, 16),  // правая рука: layer2 = layer1 +16 по Y
            new LayerBoundsPair(32, 48, 16, 16, 16, 0),  // левая рука: layer2 = layer1 +16 по X
            new LayerBoundsPair(0, 16, 16, 16, 0, 16),   // правая нога: layer2 = layer1 +16 по Y
            new LayerBoundsPair(16, 48, 16, 16, -16, 0), // левая нога: layer2 = layer1 -16 по X
    };

    private static void mirrorLayerOccupancy(boolean[] occupied) {
        for (LayerBoundsPair pair : LAYER_PAIRS) {
            for (int y = pair.y(); y < pair.y() + pair.h(); y++) {
                for (int x = pair.x(); x < pair.x() + pair.w(); x++) {
                    int i1 = y * SIZE + x;
                    int i2 = (y + pair.dy()) * SIZE + (x + pair.dx());
                    if (occupied[i1] || occupied[i2]) {
                        occupied[i1] = true;
                        occupied[i2] = true;
                    }
                }
            }
        }
    }

    private static void subtract(boolean[] mask, boolean[] occupancy) {
        for (int i = 0; i < mask.length; i++) {
            if (occupancy[i]) {
                mask[i] = false;
            }
        }
    }

    /**
     * Порядок - СНАЧАЛА texture, ПОТОМ маска поверх неё (см. чат: "надо
     * сначала текстуру, а маску поверх нее... я там уже изначально под такой
     * формат все делал") - раньше было наоборот (маска, потом texture поверх
     * неё), что не совпадало с тем, как реально нарисованы ассеты. Безопасно
     * для всех категорий: у штанов texture - всегда прозрачная заглушка
     * ({@code character/skin/blank.png}), у волос texture вообще нет -
     * порядок для них визуально не важен, различие проявляется только там,
     * где ОБА поля реально заполнены (сейчас - часть рубашек).
     */
    private static boolean applyCosmetic(NativeImage image, CosmeticDefinition definition, int rgbColor, CharacterModelType modelType, boolean[] allowedMask) {
        if (definition == null) {
            return false;
        }
        CosmeticVariant variant = definition.variantFor(modelType);
        if (variant == null) {
            return false;
        }

        boolean applied = false;
        if (variant.texture() != null) {
            applied |= applyOverlay(image, variant.texture(), allowedMask);
        }
        if (definition.supportsColor() && variant.mask() != null) {
            applied |= applyTintMask(image, variant.mask(), rgbColor, allowedMask);
        }
        return applied;
    }

    /**
     * Только для рубашки - у неё {@code mask} значит не "перекрашиваемая
     * ткань самой рубашки" (как у штанов/волос), а ОВЕРЛЕЙ КОМБИНЕЗОНА
     * (лямки/нагрудник поверх {@code texture} - обычной рубашки), крашеный в
     * цвет штанов. Без штанов комбинезону нечего "продолжать" вверх, поэтому
     * {@code hasPants} гасит и маску, и {@code pantsOverlay} - опциональный
     * неизменяемый слой (см. чат: "добавь опциональную текстуру, которая
     * будет рисоваться у рубашки ТОЛЬКО когда одеты штаны"), который рисуется
     * МЕЖДУ {@code texture} и {@code mask} (в отличие от маски - НЕ красится).
     * {@code texture} (сама рубашка) рисуется ВСЕГДА, когда рубашка вообще
     * выбрана (см. чат: "если штаны есть, но самой рубашки нет - рендерим
     * none рубашку и всё" - это уже покрыто возвратом false ниже, когда
     * variant == null).
     */
    private static boolean applyShirtCosmetic(NativeImage image, CosmeticDefinition definition, int pantsColor, CharacterModelType modelType, boolean[] allowedMask, boolean hasPants) {
        if (definition == null) {
            return false;
        }
        CosmeticVariant variant = definition.variantFor(modelType);
        if (variant == null) {
            return false;
        }

        boolean applied = false;
        if (variant.texture() != null) {
            applied |= applyOverlay(image, variant.texture(), allowedMask);
        }
        if (hasPants && variant.pantsOverlay() != null) {
            applied |= applyOverlay(image, variant.pantsOverlay(), allowedMask);
        }
        if (hasPants && definition.supportsColor() && variant.mask() != null) {
            applied |= applyTintMask(image, variant.mask(), pantsColor, allowedMask);
        }
        return applied;
    }

    /**
     * Какие пиксели (в пределах {@code allowedMask}) реально заняты этой
     * косметикой (mask ИЛИ texture, alpha>0) - НЕ рисует ничего, только
     * сканирует. Нужно, чтобы другая категория (штаны/ботинки) могла
     * заранее исключить эти пиксели из СВОЕЙ маски - жёсткий вырез вместо
     * рисования по очереди с блендингом на полупрозрачных краях.
     */
    private static boolean[] occupancyMask(CosmeticDefinition definition, CharacterModelType modelType, boolean[] allowedMask) {
        boolean[] occupied = new boolean[SIZE * SIZE];
        if (definition == null) {
            return occupied;
        }
        CosmeticVariant variant = definition.variantFor(modelType);
        if (variant == null) {
            return occupied;
        }
        if (variant.mask() != null) {
            markOccupancy(variant.mask(), allowedMask, occupied);
        }
        if (variant.texture() != null) {
            markOccupancy(variant.texture(), allowedMask, occupied);
        }
        return occupied;
    }

    private static void markOccupancy(String path, boolean[] allowedMask, boolean[] occupied) {
        NativeImage source = readCharacterTexture(path);
        if (source == null) {
            return;
        }
        try (source) {
            int width = Math.min(SIZE, source.getWidth());
            int height = Math.min(SIZE, source.getHeight());
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!allowedMask[y * SIZE + x]) {
                        continue;
                    }
                    if (nativeAlpha(source.getPixelRGBA(x, y)) > 0) {
                        occupied[y * SIZE + x] = true;
                    }
                }
            }
        }
    }

    private static boolean applyTintMask(NativeImage image, String maskPath, int rgbColor, boolean[] allowedMask) {
        NativeImage mask = readCharacterTexture(maskPath);
        if (mask == null) {
            return false;
        }
        try (mask) {
            int width = Math.min(SIZE, mask.getWidth());
            int height = Math.min(SIZE, mask.getHeight());
            int baseR = (rgbColor >> 16) & 0xFF;
            int baseG = (rgbColor >> 8) & 0xFF;
            int baseB = rgbColor & 0xFF;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!allowedMask[y * SIZE + x]) {
                        continue;
                    }
                    int pixel = mask.getPixelRGBA(x, y);
                    int alpha = nativeAlpha(pixel);
                    if (alpha == 0) {
                        continue;
                    }

                    int gray = luminance(nativeRed(pixel), nativeGreen(pixel), nativeBlue(pixel));
                    double factor = gray / 128.0D;
                    int tinted = nativeColor(
                            clampByte(baseR * factor),
                            clampByte(baseG * factor),
                            clampByte(baseB * factor),
                            alpha
                    );
                    blendPixel(image, x, y, tinted);
                }
            }
            return true;
        }
    }

    private static boolean applyOverlay(NativeImage image, String texturePath, boolean[] allowedMask) {
        NativeImage overlay = readCharacterTexture(texturePath);
        if (overlay == null) {
            return false;
        }
        try (overlay) {
            int width = Math.min(SIZE, overlay.getWidth());
            int height = Math.min(SIZE, overlay.getHeight());
            boolean applied = false;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!allowedMask[y * SIZE + x]) {
                        continue;
                    }
                    int pixel = overlay.getPixelRGBA(x, y);
                    if (nativeAlpha(pixel) == 0) {
                        continue;
                    }
                    blendPixel(image, x, y, pixel);
                    applied = true;
                }
            }
            return applied;
        }
    }

    private static NativeImage readCharacterTexture(String path) {
        String resourcePath = "/assets/stardew/textures/" + path;
        try (InputStream stream = SkinComposer.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            return NativeImage.read(stream);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void blendPixel(NativeImage image, int x, int y, int source) {
        int sourceAlpha = nativeAlpha(source);
        if (sourceAlpha == 0) {
            return;
        }
        if (sourceAlpha == 255) {
            setPixel(image, x, y, source);
            return;
        }

        int destination = image.getPixelRGBA(x, y);
        double sa = sourceAlpha / 255.0D;
        double da = nativeAlpha(destination) / 255.0D;
        double outA = sa + da * (1.0D - sa);
        if (outA <= 0.0D) {
            setPixel(image, x, y, 0);
            return;
        }

        int r = clampByte((nativeRed(source) * sa + nativeRed(destination) * da * (1.0D - sa)) / outA);
        int g = clampByte((nativeGreen(source) * sa + nativeGreen(destination) * da * (1.0D - sa)) / outA);
        int b = clampByte((nativeBlue(source) * sa + nativeBlue(destination) * da * (1.0D - sa)) / outA);
        int a = clampByte(outA * 255.0D);
        setPixel(image, x, y, nativeColor(r, g, b, a));
    }

    private static int luminance(int r, int g, int b) {
        return clampByte(0.2126D * r + 0.7152D * g + 0.0722D * b);
    }

    private static int clampByte(double value) {
        return Math.max(0, Math.min(255, (int) Math.round(value)));
    }

    private static int nativeRed(int nativeColor) {
        return nativeColor & 0xFF;
    }

    private static int nativeGreen(int nativeColor) {
        return (nativeColor >> 8) & 0xFF;
    }

    private static int nativeBlue(int nativeColor) {
        return (nativeColor >> 16) & 0xFF;
    }

    private static int nativeAlpha(int nativeColor) {
        return (nativeColor >> 24) & 0xFF;
    }

    private static int nativeColor(int r, int g, int b, int a) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    /**
     * {@code profile}-цвета хранятся как обычный 0xRRGGBB (см. ТЗ §17-19
     * примеры "#4A7C91"). {@link NativeImage#setPixelRGBA} же ждёт ABGR8888
     * (порядок байт R,G,B,A в памяти) — без этой перепаковки красный и синий
     * канал перепутаются местами.
     */
    private static int withAlpha(int rgbColor) {
        int r = (rgbColor >> 16) & 0xFF;
        int g = (rgbColor >> 8) & 0xFF;
        int b = rgbColor & 0xFF;
        int a = 0xFF;
        return nativeColor(r, g, b, a);
    }
}

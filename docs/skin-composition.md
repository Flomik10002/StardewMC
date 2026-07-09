# Генерация Runtime Skin: Кожа И Одежда

Документ фиксирует целевую схему генерации скина игрока в StardewMC на основе обсуждения Stardew reference и текущей реализации `SkinComposer`.

Главная идея: источник истины - не PNG-файл скина, а данные персонажа и экипировки. Клиент на их основе собирает runtime `64x64` Minecraft skin texture.

## Stardew Reference: Как Работает Кожа

В Stardew цвет кожи устроен не как свободный RGB-пикер. Игрок выбирает индекс тона кожи, а renderer перекрашивает базовый farmer spritesheet через palette swap.

Основные файлы reference:

- `StardewValley/Farmer.cs` - хранит `skin` как `NetInt`.
- `StardewValley/FarmerRenderer.cs` - применяет palette swap.
- `StardewValley.Menus/CharacterCustomization.cs` - показывает стрелки `Skin`, а не color picker.
- `reference-sources/PC _ Computer - Stardew Valley - Playable Characters - Farmer Base.png` - локальный reference/rip sheet для анализа.

В Stardew есть базовая texture игрока:

- `Characters\Farmer\farmer_base`
- `Characters\Farmer\farmer_girl_base`

В этой texture участки кожи размечены служебными цветами. Это не финальные цвета кожи, а маркеры:

- skin shadow marker: `#6B003A`
- skin mid marker: `#E06B65`
- skin light marker: `#F9AE89`

В C# renderer эти маркеры находятся через индексы пикселей `260`, `261`, `262`. Важно: `260/261/262` - это не RGB-значения, а позиции пикселей в исходной texture. Renderer берет цвет пикселя на этой позиции, сканирует всю texture и запоминает все пиксели с таким же цветом.

Дальше выбирается `skin index` в диапазоне `0..23`. Renderer читает texture-палитру `Characters\Farmer\skinColors`. Для каждой строки skin index в ней лежат три цвета:

- darkest
- medium
- lightest

Потом выполняется замена:

```text
all pixels with marker #6B003A -> skinColors[skinIndex].darkest
all pixels with marker #E06B65 -> skinColors[skinIndex].medium
all pixels with marker #F9AE89 -> skinColors[skinIndex].lightest
```

Итог: кожа не заливается одним цветом. Она собирается из трех тонов, чтобы сохранить объем: тени, основной тон и свет.

Reference PNG подтверждает эту схему. В правой части rip sheet есть палитровая область, где по строкам идут skin tones. Этот файл нужен только как технический reference. В runtime-ресурсы StardewMC не нужно переносить оригинальный Stardew PNG или точную Stardew-палитру.

## StardewMC: Целевая Модель Кожи

Для StardewMC правильная модель - хранить не `skinColor`, а `skinId` или `skinToneId`.

Текущая база уже близка к этому:

- `CharacterProfile` хранит `skinId`.
- `CosmeticRegistry` читает `assets/stardew/character/skins.json`.
- `SkinToneDefinition` хранит `highlight`, `base`, `shadow`.
- `SkinComposer` собирает `NativeImage 64x64` из `CharacterProfile`.

Соответствие Stardew-терминам:

- `shadow` = darkest
- `base` = medium
- `highlight` = lightest

Пример целевого `skins.json`:

```json
[
  {
    "id": "skin_001",
    "palette": {
      "highlight": "#FFD4BC",
      "base": "#F7C0A1",
      "shadow": "#B2846A"
    }
  }
]
```

Эти цвета должны быть оригинальной палитрой StardewMC, а не копией Stardew assets.

## Как Генерировать Кожу

Есть два возможных подхода.

### Вариант A: Marker Template

Делаем собственный `64x64` skin template, где кожа размечена тремя служебными цветами:

- `SKIN_SHADOW_MARKER`
- `SKIN_BASE_MARKER`
- `SKIN_HIGHLIGHT_MARKER`

При генерации:

1. Загружаем template.
2. Сканируем все пиксели.
3. Если пиксель равен shadow marker, заменяем на `skin.shadow`.
4. Если пиксель равен base marker, заменяем на `skin.base`.
5. Если пиксель равен highlight marker, заменяем на `skin.highlight`.
6. Остальные пиксели оставляем прозрачными или как есть.

Плюс: ближе к Stardew palette swap.

Минус: маркерные цвета хрупкие. Художник может случайно использовать такой же цвет в другом месте.

### Вариант B: Mask Layers

Простой рекомендуемый вариант для StardewMC: использовать маски, а не магические marker colors.

Нужные ассеты:

```text
assets/stardew/textures/character/skin/template_shadow.png
assets/stardew/textures/character/skin/template_base.png
assets/stardew/textures/character/skin/template_highlight.png
```

Каждая маска - прозрачный `64x64` PNG. Непрозрачные пиксели показывают, куда наносить соответствующий цвет.

При генерации:

1. Создать пустой прозрачный `NativeImage 64x64`.
2. Наложить `template_shadow.png` цветом `skin.shadow`.
3. Наложить `template_base.png` цветом `skin.base`.
4. Наложить `template_highlight.png` цветом `skin.highlight`.
5. Продолжить наложение волос, глаз, одежды и аксессуаров.

Плюсы:

- художнику проще править зоны;
- нет зависимости от конкретных marker colors;
- маски можно отдельно версионировать;
- легко добавлять новые зоны: румянец, нос, тени лица, детали рук.

Для текущего проекта это лучше, чем повторять Stardew-пиксельные индексы `260/261/262`.

### Вариант C: Tone Map + Palette Ramp

Если трех тонов кожи мало, лучше не переходить к обычному multiply-grayscale как у одежды. Для кожи это быстро дает грязные оттенки: тени становятся слишком серыми или слишком насыщенными, а разные skin tones начинают выглядеть неестественно.

Более сильная схема для кожи:

```text
assets/stardew/textures/character/skin/template_tone_map.png
```

`template_tone_map.png` - это grayscale/index map:

- alpha = где есть кожа;
- grayscale = номер уровня света/тени;
- уровней может быть 8, 10, 12 или 16;
- каждый уровень должен быть чистым фиксированным значением, без anti-aliasing.

Например для 10 уровней:

```text
0, 28, 57, 85, 113, 142, 170, 198, 227, 255
```

В `skins.json` тогда хранится не только `highlight/base/shadow`, а полноценный ramp:

```json
{
  "id": "skin_001",
  "ramp": [
    "#5A3328",
    "#704232",
    "#88543F",
    "#A4684F",
    "#BF7F61",
    "#D89575",
    "#E8AA8B",
    "#F4BFA4",
    "#FFD3BC",
    "#FFE2D0"
  ]
}
```

Генерация:

1. Читаем grayscale пиксель из `template_tone_map.png`.
2. Переводим его в индекс `0..ramp.length - 1`.
3. Берем цвет из `skin.ramp[index]`.
4. Пишем его в runtime skin.

Это все еще Stardew-подход: выбранный `skinId` задает палитру, а не произвольный RGB. Разница в том, что вместо трех цветов у нас может быть 10 уровней света. При 24 вариантах кожи это хорошо масштабируется: один общий tone-map и 24 palette ramps.

## Порядок Композиции Скина

Целевой порядок сборки:

1. Создать прозрачный `NativeImage 64x64`.
2. Нарисовать кожу layer1: голова, тело, руки, ноги.
3. Нарисовать глаза и базовые черты лица.
4. Нарисовать волосы.
5. Нарисовать базовую одежду: shirt/pants/skirt/boots, если они являются плоской одеждой.
6. Нарисовать layer2-детали одежды: куртка, рукава, манжеты, выступающие края.
7. Нарисовать плоские аксессуары, которые помещаются в Minecraft skin texture.
8. Зарегистрировать/обновить `DynamicTexture`.
9. Применить texture location к игроку.

Кожа должна рисоваться до одежды. Одежда должна перекрывать кожу только там, где ее `mask` или `texture` явно непрозрачны.

## Одежда: Не Armor, А Runtime Skin Composition

Для StardewMC одежду лучше не пихать в vanilla armor pipeline, если это обычная Stardew-like косметика.

Правильная модель:

- плоская одежда запекается в runtime skin;
- объемная косметика рисуется отдельным render layer;
- vanilla armor остается только для настоящей брони.

Что запекать в runtime skin:

- рубашки;
- штаны;
- простые ботинки;
- простые волосы;
- плоские аксессуары лица;
- базовые рукава/манжеты, если они помещаются в `64x64` skin.

Что не запекать в runtime skin:

- шляпы с объемом;
- рюкзаки;
- плащи;
- большие юбки/пальто;
- предметы на спине;
- любые 3D или выступающие детали.

Для таких вещей нужен custom render layer.

## Данные Одежды

Для одежды лучше использовать grayscale tint map, а не три отдельные маски `shadow/base/highlight`.

Минимальная схема для плоской одежды:

```json
{
  "id": "pants_001",
  "texture": "character/pants/001_texture.png",
  "mask": "character/pants/001_mask.png",
  "supportsColor": true
}
```

Смысл полей:

- `mask` - grayscale + alpha слой ткани, который красится выбранным цветом.
- `texture` - обычный RGBA слой неизменяемых деталей поверх ткани.
- `supportsColor` - можно ли применять пользовательский цвет.

В `mask`:

- alpha задает форму окрашиваемой части;
- grayscale задает свет/тень;
- `128` означает "выбранный цвет без изменения";
- ниже `128` темнит выбранный цвет;
- выше `128` осветляет выбранный цвет.

В `texture` остаются детали, которые не должны перекрашиваться:

- золотой воротник;
- логотип;
- пуговицы;
- швы с фиксированным цветом;
- грязь/узоры;
- любые декоративные пиксели, не зависящие от цвета ткани.

Формула при генерации:

```text
factor = maskGray / 128.0
out.rgb = chosenColor.rgb * factor
out.a = maskAlpha
```

После этого поверх рисуется `texture`.

Порядок для одной вещи:

```text
draw mask recolored by chosenColor
draw texture unchanged
```

Для подготовки таких слоев есть workshop-скрипт:

```text
python asset-tools/make_tint_map.py input.png \
  --tint-output shirt_001_mask.png \
  --fixed-output shirt_001_texture.png \
  --mask shirt_fabric_mask.png \
  --preview-output shirt_001_preview.png \
  --preview-color "#4A7C91"
```

Если `--mask` не передан, весь непрозрачный исходный PNG считается окрашиваемой тканью. Для реальных вещей лучше давать mask вручную, потому что скрипт не может надежно понять, что воротник золотой и должен остаться в `texture`.

## Профиль И Экипировка

Нужно разделять внешний вид персонажа и надетую одежду.

Внешность профиля:

```text
skinId
hairId
hairColor
shirtId
shirtColor
pantsId
pantsColor
accessoryId
```

Экипировка:

```text
equippedShirtItem
equippedPantsItem
equippedBootsItem
```

У предмета одежды должны быть свои данные:

```text
clothingId
clothingColor
dyeable
prismatic/effect flags
```

Правило разрешения внешнего вида:

1. Если есть экипированный shirt item, для рендера используется он.
2. Иначе используется `profile.shirtId/profile.shirtColor`.
3. Если есть экипированный pants item, для рендера используется он.
4. Иначе используется `profile.pantsId/profile.pantsColor`.

Это повторяет смысл Stardew:

- character customization меняет внешний вид профиля;
- dye/tailoring меняет конкретный предмет одежды;
- надетый предмет может визуально заменить профильную одежду;
- снятие предмета возвращает вид к профильной одежде.

## Когда Пересобирать Runtime Skin

Скин нужно пересобирать при любом изменении visual state:

- игрок завершил character customization;
- изменился `skinId`;
- изменился `hairId` или `hairColor`;
- изменился `shirtId` или `shirtColor`;
- изменился `pantsId` или `pantsColor`;
- игрок надел/снял clothing item;
- clothing item был покрашен;
- resource reload изменил texture/mask JSON.

Не нужно пересобирать скин каждый tick.

Правильная схема:

```text
visual state -> stable hash -> cache lookup -> compose only if hash changed
```

Hash должен учитывать:

- profile appearance fields;
- IDs и цвета экипированной одежды;
- модель рук: classic/slim;
- версию ассетов или resource reload counter.

## Multiplayer

Сервер должен быть источником истины для профиля и экипировки.

Целевой поток:

1. Клиент меняет draft в GUI.
2. Клиент отправляет `C2SFinishCharacterCreation`.
3. Сервер валидирует ID кожи/волос/одежды.
4. Сервер сохраняет профиль в capability.
5. Сервер рассылает принятый профиль клиентам.
6. Клиенты генерируют runtime skin для соответствующего UUID.

Сейчас `RuntimeSkinManager` работает как local-player manager. Для полного multiplayer нужно расширить идею до per-player cache:

```text
Map<UUID, DynamicTexture>
Map<UUID, VisualStateHash>
```

Каждый клиент должен уметь собрать внешний вид любого видимого игрока из синхронизированных данных. Не нужно передавать готовый PNG по сети, достаточно передавать компактное состояние.

## Связь С Текущим SkinComposer

Сейчас `SkinComposer` уже делает правильную архитектурную вещь: собирает skin из `CharacterProfile`, а не редактирует сохраненный PNG игрока.

Текущее упрощение:

- кожа заливается по Minecraft body faces через `highlight/base/shadow`;
- shirt и pants заливаются сплошным цветом по областям тела;
- реальные `texture/mask` слои из `CosmeticDefinition` частично подключены для одежды;
- runtime texture применяется только локальному игроку.

Целевой следующий шаг:

1. Для кожи выбрать стартовый формат: три маски `shadow/base/highlight` или `template_tone_map.png + ramp`.
2. Заменить процедурную заливку тела на наложение skin masks или skin tone-map.
3. Добавить чтение `texture/mask` слоев для shirts и pants.
4. Накладывать одежду поверх кожи через grayscale tint formula.
5. Добавить cache по visual state hash.
6. Позже расширить manager до per-player dynamic skins.

## Псевдокод

```text
compose(profile, equipment):
    state = resolveVisualState(profile, equipment)
    image = new 64x64 transparent image

    skin = SkinRegistry.get(state.skinId)
    if skin has ramp:
        applyToneMap(image, "skin/template_tone_map.png", skin.ramp)
    else:
        applyMask(image, "skin/template_shadow.png", skin.shadow)
        applyMask(image, "skin/template_base.png", skin.base)
        applyMask(image, "skin/template_highlight.png", skin.highlight)

    drawEyes(image, state.eyeColor)

    hair = HairRegistry.get(state.hairId)
    applyCosmetic(image, hair, state.hairColor)

    shirt = resolveShirt(profile, equipment)
    applyCosmetic(image, shirt.definition, shirt.color)

    pants = resolvePants(profile, equipment)
    applyCosmetic(image, pants.definition, pants.color)

    accessory = AccessoryRegistry.get(state.accessoryId)
    applyFlatAccessoryIfPossible(image, accessory)

    return image
```

`applyCosmetic` должен соблюдать `supportsColor`:

```text
applyCosmetic(image, definition, color):
    if definition.supportsColor:
        draw definition.mask recolored by color
    draw definition.texture unchanged
```

Формула для `definition.mask`:

```text
factor = maskGray / 128.0
out.rgb = color.rgb * factor
out.a = maskAlpha
```

## Итоговое Решение

Для StardewMC целевая схема такая:

- кожа - preset palette через `skinId`, не свободный RGB;
- кожа генерируется через `shadow/base/highlight` или через `tone_map + ramp`;
- технически лучше использовать masks, а не Stardew marker pixel indices;
- одежда красится через grayscale `mask` и неизменяемый `texture` overlay;
- плоская одежда запекается в runtime skin;
- объемная косметика идет в custom render layer;
- armor pipeline используется только для настоящей брони;
- сервер синхронизирует состояние, клиенты сами собирают texture;
- PNG не является source of truth, его всегда можно пересобрать из данных.

# Покраска штанов в Stardew C# Decompiled Main

Документ описывает, как в локальном reference source `reference-sources/.StardewValleyDecompiled-main` устроены цвет штанов, окрашивание и логика экрана смены персонажа. Код ниже не копируется из decompiled source; это поведенческое описание для переноса системы.

## Основные файлы

- `StardewValley.Menus/CharacterCustomization.cs` - экран создания/смены персонажа и отдельный UI для dye pots/clothes dye.
- `StardewValley/Farmer.cs` - состояние игрока, выбранный стиль штанов, цвет штанов, экипированный `Clothing`.
- `StardewValley/FarmerRenderer.cs` - применение цвета штанов при отрисовке.
- `StardewValley.Objects/Clothing.cs` - предмет одежды, флаги `dyeable`/`isPrismatic`, `clothesColor`, метод смешивания цвета.
- `StardewValley.GameData.Pants/PantsData.cs` - данные штанов: дефолтный цвет, можно ли красить, призматика, доступность в кастомизации.
- `StardewValley.Menus/DyeMenu.cs` и `TailoringMenu.cs` - внешние способы покраски одежды через dye pots/tailoring.

## Модель данных

В Stardew цвет штанов живет не только как "цвет персонажа". Есть две связанные модели:

- legacy/customization override на `Farmer`: поле `pants` хранит выбранный ID штанов, а `pantsColor` хранит цвет для этого override.
- современная экипировка на `Farmer`: `pantsItem` хранит `Clothing`, а сам цвет лежит в `pantsItem.clothesColor`.

Метод `Farmer.changePantsColor(Color)` синхронизирует обе модели: выставляет `pantsColor` и, если у игрока есть `pantsItem`, выставляет тот же цвет в `pantsItem.clothesColor`. Поэтому обычная смена цвета персонажа не является чисто UI-настройкой: она также перекрашивает экипированный предмет штанов, если он есть.

`PantsData` определяет базовые свойства конкретного ID штанов:

- `DefaultColor` - цвет при создании `Clothing` из данных.
- `CanBeDyed` - разрешает покраску.
- `IsPrismatic` - вместо сохраненного цвета используется динамический prismatic color.
- `CanChooseDuringCharacterCustomization` - разрешает выбрать этот ID на экране смены персонажа.

`Clothing.LoadData(applyColor: true)` для штанов читает `PantsData`, выставляет `dyeable`, `clothesColor`, `clothesType = PANTS` и `isPrismatic`. Если предмет `dyeable`, описание предмета получает дополнительную строку о возможности окрашивания.

## Как выбирается цвет для отрисовки

Рендер не перекрашивает текстуру заранее. `FarmerRenderer` каждый раз получает текстуру и sprite index через `Farmer.GetDisplayPants(...)`, а затем рисует штаны с tint-цветом из `Farmer.GetPantsColor()`.

Приоритет `GetPantsColor()`:

1. Если включен override через `Farmer.pants`, возвращается цвет override из `pantsColor`.
2. Иначе, если есть `pantsItem` и он prismatic, возвращается динамический prismatic color.
3. Иначе, если есть `pantsItem`, возвращается `pantsItem.clothesColor`.
4. Иначе возвращается нейтральный белый цвет.

Это важно: сохраненный RGB - не отдельная запеченная текстура, а tint, применяемый поверх выбранного spritesheet-кадра штанов. В Minecraft-переносе можно сделать иначе, но поведенчески источник истины должен оставаться `pantsId + pantsColor`.

## Смена персонажа: подробный поток

`CharacterCustomization` используется не только для создания персонажа. Один и тот же класс работает в нескольких режимах `Source`, и от режима зависит, куда именно записывается выбранный цвет.

Основные режимы для штанов:

- обычная смена/создание персонажа - меняет внешний вид игрока через `Farmer.changePantsColor`.
- `Source.DyePots` - меняет цвет уже надетого dyeable `pantsItem`.
- `Source.ClothesDye` - меняет цвет отдельного предмета одежды `_itemToDye`, который показывается на preview farmer.

### Инициализация UI

При создании `CharacterCustomization` экран сначала определяет, является ли он dye-меню. Для `DyePots` он считает, сколько окрашиваемых вещей надето: shirt и pants добавляются только если `Game1.player.CanDyeShirt()`/`CanDyePants()` возвращают `true`. От этого зависит высота окна и набор показанных color picker'ов.

Для обычной смены одежды, когда `allow_clothing_changes` включен:

- добавляются стрелки выбора shirt;
- добавляется строка `Pants Color`;
- создается `pantsColorPicker = new ColorPicker("Pants", ...)`;
- стартовый цвет берется из `Game1.player.GetPantsColor()`;
- для трех полос picker'а создаются компоненты навигации с ID `528`, `529`, `530`.

Эти три ID соответствуют каналам HSV-подобного picker'а:

- `528` - hue;
- `529` - saturation;
- `530` - value/brightness.

Для `DyePots` строка штанов создается только при `Game1.player.CanDyePants()`. Начальный цвет также берется из `GetPantsColor()`, но запись при изменении идет не через `changePantsColor`, а напрямую в `Game1.player.pantsItem.Value.clothesColor`.

Для `ClothesDye` picker создается как `Dye Color`, а стартовый цвет берется из `_itemToDye.clothesColor`. Это режим не изменения персонажа как профиля, а изменения конкретного предмета одежды.

### Действия color picker'а

В обычной смене персонажа клик по `pantsColorPicker` делает:

1. picker вычисляет новый `Color` по позиции клика;
2. экран вызывает `Game1.player.changePantsColor(color)`;
3. состояние игрока обновляет `pantsColor` и, если есть `pantsItem`, `pantsItem.clothesColor`;
4. preview/рендер получают новый tint через `GetPantsColor()`.

Удержание мыши работает тем же путем, только цвет берется через `clickHeld`. Это позволяет тянуть слайдер без отдельной кнопки подтверждения.

Для gamepad/keyboard-навигации используются те же компоненты `528`-`530`. При DPad вправо значение соответствующего канала увеличивается, при DPad влево уменьшается. После изменения:

- сохраняется предыдущий цвет в `LastColor`;
- вызывается `changeHue`, `changeSaturation` или `changeValue`;
- picker помечается как `Dirty`;
- `_sliderOpTarget` ставится на `pantsColorPicker`;
- `_sliderAction` ставится на `_recolorPantsAction`.

В обычном режиме `_recolorPantsAction` вызывает `Game1.player.changePantsColor(pantsColorPicker.getSelectedColor())`. В `DyePots` этот action заменяется и напрямую пишет в `pantsItem.clothesColor`, если штаны можно красить.

### Отличие обычной смены персонажа от dye modes

В обычной смене персонажа цвет считается частью внешности игрока. UI не проверяет `CanDyePants()`, когда `allow_clothing_changes` включен: он просто показывает настройку `Pants Color` и пишет через `changePantsColor`.

В `DyePots` цвет считается покраской надетого предмета. Поэтому:

- picker для штанов появляется только если `CanDyePants() == true`;
- запись идет в `Game1.player.pantsItem.Value.clothesColor`;
- после записи помечаются dirty оба рендера: реальный игрок и display farmer.

В `ClothesDye` цвет считается покраской отдельного `_itemToDye`. Изменение вызывает `DyeItem(color)`, а `DyeItem` вызывает `_itemToDye.Dye(color, 1f)`. Сила `1f` означает полное движение текущего цвета к выбранному цвету за один шаг.

### Выбор стиля штанов

Стрелки `Pants Style` не меняют цвет. Они вызывают `Game1.player.rotatePantStyle(change, GetValidPantsIds())`.

`GetValidPantsIds()` берет данные из `Game1.pantsData` и оставляет ID, у которых `CanChooseDuringCharacterCustomization == true`. Текущий выбранный ID также учитывается через общий helper выбора одежды, чтобы экран не ломался на уже выбранной вещи.

`rotatePantStyle` циклически переключает ID. Если текущий ID не найден в списке допустимых, ставится первый допустимый. После изменения вызывается `changePantStyle`, который обновляет `Farmer.pants` и сообщает renderer'у о смене pants texture/index.

Цвет при этом не сбрасывается. Выбранный `pantsColor` продолжает применяться к новому стилю, если только дальнейшая логика конвертации override в `Clothing` не создаст предмет с собственным цветом.

### Randomize

При рандомизации персонажа экран отдельно выбирает случайный стиль штанов и отдельно генерирует `pantsColor`. Цвет сначала выбирается случайными RGB-компонентами, затем может быть затемнен по каналам. Для некоторых shirt index есть специальные цветовые варианты штанов.

После выбора цвета вызывается `Game1.player.changePantsColor(pantsColor)`, а picker синхронизируется обратно через `pantsColorPicker.setColor(Game1.player.GetPantsColor())`.

## Внешнее окрашивание одежды

`Clothing.Dye(Color, strength)` работает только если `dyeable == true`. Метод не просто заменяет цвет, а двигает каждый канал текущего цвета к целевому цвету с указанной силой.

В `TailoringMenu` цвет красителя берется из предмета:

- prismatic shard переводит одежду в prismatic-режим;
- colored object берет собственный цвет;
- остальные предметы получают цвет через context tags.

Сила красителя зависит от тегов:

- weak/default - слабое изменение;
- `dye_medium` - среднее изменение;
- `dye_strong` - полное или почти полное изменение;
- override может принудительно задать силу.

`DyeMenu` с dye pots сначала требует заполнить все pots. Надетые shirt/pants показываются в списке окрашиваемых вещей только если соответствующий `CanDye...()` возвращает `true`. Для штанов этот check сводится к `Game1.player.pantsItem.Value?.dyeable.Value ?? false`.

## Вывод для StardewMC

Для текущей реализации StardewMC уже выбран близкий минимальный аналог:

- `CharacterProfile` хранит `pantsId` и `pantsColor` как постоянные данные внешности.
- `CharacterCreationScreen` имеет отдельную строку выбора pants ID и отдельную HSV-группу для pants color.
- `pants.json` содержит определения штанов с `supportsColor: true`.
- `SkinComposer` сейчас запекает `pantsColor` в Minecraft skin legs, а не применяет tint на каждом render call.

Это допустимая Minecraft-адаптация, но важно сохранить разделение будущих систем:

- смена персонажа должна менять внешний вид профиля: `pantsId + pantsColor`;
- будущая система одежды должна хранить цвет на конкретном item stack, аналогично `Clothing.clothesColor`;
- dye pots/tailoring не должны безусловно менять профиль игрока, если игрок красит отдельный предмет;
- если появятся prismatic pants, их цвет должен быть вычисляемым render-state, а не обычным сохраненным RGB;
- выбор `Pants Style` должен быть независим от `Pants Color`.

Минимальный целевой поток для StardewMC character customization:

1. Клиент открывает экран с draft-профилем.
2. Стрелки pants меняют только `draft.pantsId`.
3. HSV-слайдеры pants меняют только `draft.pantsColor`.
4. Preview пересобирается из draft.
5. На подтверждение клиент отправляет `pantsId` и `pantsColor` на сервер.
6. Сервер валидирует `pantsId`, сохраняет профиль и рассылает принятый профиль обратно.
7. Клиент применяет серверный профиль и пересобирает runtime skin.

Такой поток соответствует поведению Stardew на уровне данных, даже если технически Minecraft-версия запекает цвет в skin texture вместо runtime tint-отрисовки.

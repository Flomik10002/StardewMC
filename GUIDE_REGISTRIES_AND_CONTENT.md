# Гайд по системе регистров StardewMC

В этом проекте используется кастомная система регистрации (Framework), расположенная в `dev.flomik.stardew.common.registry.framework`. Она упрощает создание предметов, блоков, сущностей блоков (Block Entities) и привязку их к вкладкам креатива и генерации моделей.

Все основные регистры находятся в `dev.flomik.stardew.common.registry`.

## 1. Регистрация предметов (`ModItems`)

Для добавления нового предмета используйте `ItemBuilder` в классе `ModItems`.

### Пример простого предмета
```java
public static final RegistryObject<Item> MY_ITEM = ItemBuilder.create("my_item")
        .tab(ModTabs.ARTISAN_GOODS)       // Указать вкладку
        .model(ModelPresets.simple())     // Автоматическая генерация simple модели (генерируется в datagen)
        .register();
```

### Пример предмета с кастомным классом
Если у вас есть свой класс предмета (например, инструмент):
```java
public static final RegistryObject<ToolHoe> MY_HOE = ItemBuilder
        .create("my_hoe", p -> new ToolHoe(p, PatternType.SINGLE)) // Фабрика
        .stacksTo(1)                      // Размер стака
        .tab(ModTabs.TOOLS)
        .register();
```

### Основные методы `ItemBuilder`:
- `create(String name)`: Создает билдер для обычного `Item`.
- `create(String name, Function<Properties, I> factory)`: Создает билдер для кастомного класса.
- `tab(RegistryObject<CreativeModeTab>)`: Добавляет предмет во вкладку.
- `stacksTo(int)`: Устанавливает максимальный размер стака.
- `model(ItemModelGen)`: Указывает пресет для генерации модели (см. `ModelPresets`).

---

## 2. Регистрация блоков (`ModBlocks`)

Для блоков используется `BlockBuilder`. Он умеет сразу регистрировать и сам блок, и его `BlockItem`, и даже `BlockEntity`.

### Пример простого блока
```java
public static final RegistryObject<BlockDirt> MY_DIRT = BlockBuilder
        .create("my_dirt", BlockDirt::new) // Фабрика блока
        .transform(copy(Blocks.DIRT))      // Копирование свойств ванильного блока
        .item()                            // Создать BlockItem
            .tab(ModTabs.BLOCK)            // Вкладка для BlockItem
        .register();
```

### Пример блока с BlockEntity (сложный блок)
Возвращает объект `BlockEntry<Block, BlockEntity>`, содержащий ссылки на всё сразу.

```java
public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder
        .create("keg", BlockKeg::new)
        .transform(woodMachine())          // Пресет свойств (деревянная машина)
        .blockEntity(BlockEntityKeg::new)  // Фабрика BlockEntity
        .item().tab(ModTabs.CRAFTABLES)    // ItemBlock во вкладке Craftables
        .register();
```

### Пример технического блока (без предмета)
Например, растение (Crop), у которого нет ItemBlock (семена — это отдельный предмет).

```java
public static final BlockEntry<BlockCrop, CropBlockEntity> CROP = BlockBuilder
        .create("crop", BlockCrop::new)
        .transform(crop())                 // Пресет свойств для растений
        .blockEntity(CropBlockEntity::new)
        .noItem()                          // НЕ создавать ItemBlock
        .register();
```

### Основные методы `BlockBuilder`:
- `create(String name, Function<Properties, B> factory)`: Начало цепочки.
- `transform(...)`: Применение пресетов свойств (см. `BlockPresets`).
- `blockEntity(BiFunction<...> factory)`: Привязка BlockEntity. Возвращает `BlockEntityBuilder`.
- `item()`: Переход к настройке предмета блока. Возвращает `BlockItemBuilder`.
- `noItem()`: Завершает регистрацию без создания предмета.

---

## 3. Работа с Block Entities

Если вы регистрируете блок через `.blockEntity(...)`, система автоматически:
1. Создаст `BlockEntityType`.
2. Зарегистрирует его в Forge.
3. Вернет его в составе `BlockEntry`.

Доступ к типу энтити:
```java
// Получение RegistryObject
RegistryObject<BlockEntityType<BlockEntityKeg>> type = ModBlocks.KEG.getType();

// Получение самого типа (для рендереров и т.д.)
BlockEntityType<BlockEntityKeg> typeValue = ModBlocks.KEG.getTypeValue();
```

В самом классе `BlockEntity` конструктор должен принимать `BlockEntityType`:
```java
public class MyBlockEntity extends BlockEntity {
    public MyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MY_BLOCK_ENTRY.getTypeValue(), pos, state);
    }
}
```

---

## 4. Генерация моделей (`datagen`)

Система поддерживает декларативную генерацию моделей через `DataGenManager`.
В `ItemBuilder` метод `.model(...)` принимает генератор модели.

### Пресеты (`ModelPresets`)
Находятся в `dev.flomik.stardew.common.registry.framework.datagen.ModelPresets`.

- `simple()`: Генерирует модель `item/generated` с текстурой `item/name`.
- `handheld()`: Генерирует модель `item/handheld` (для инструментов).
- `seed()`: Генерирует модель для семян.

Если вы указали `.model()`, то при запуске Data Generator (runData) json-файлы моделей будут созданы автоматически.

### Сезонные модели (ClientModelRegistry)
Для блоков с сезонными текстурами (трава, грядки) используется отдельная система в `ClientModelRegistry.java`.
Она **не** использует стандартный json-loader для выбора текстур, а подменяет `BakedModel` на лету.

Если вы добавляете новый сезонный блок:
1. Зарегистрируйте его как обычный блок.
2. В `ClientModelRegistry` добавьте логику подмены в `onModelBake`.
3. Убедитесь, что текстуры лежат по путям, ожидаемым в `ClientModelRegistry` (например, `block/grass/summer/...`).

---

## 5. Добавление культур (Crops)

Культуры регистрируются отдельно в логическом реестре `CropRegistry`, так как они используют один блок `ModBlocks.CROP` с разными данными.

1. **Создайте определение культуры (`CropDef`)** в `CropRegistry.bootstrapVanillaLike`:
   ```java
   register(new ResourceLocation(modid, "my_crop"),
       new CropDef(
           new ResourceLocation(modid, "my_crop"),
           List.of("spring"),          // Сезоны
           List.of(1, 2, 2, 3),        // Дни в каждой фазе
           -1,                         // Дней на регров (-1 = одноразовое)
           false,                      // Треллис (блочит проход)
           false,                      // Пэдди (рис)
           true,                       // Требует полива
           new ResourceLocation(modid, "my_crop_item"), // Дроп
           CropDef.HarvestMethod.GRAB, // Метод сбора
           1, 1, 0.05f, 0              // Мин, Макс, Шанс доп., Индекс спрайта
       ),
       new ResourceLocation(modid, "my_crop_seeds") // ID предмета семян
   );
   ```

2. **Добавьте предметы**:
   - Сам урожай (`ModItems.MY_CROP_ITEM`).
   - Семена (`ModItems.MY_CROP_SEEDS`) — используйте класс `ItemStardewSeed` и передайте ID культуры.

---

## Структура папок ресурсов

```
src/main/resources/assets/stardew/
├── blockstates/      # JSON стейты блоков
├── lang/             # en_us.json, ru_ru.json
├── models/
│   ├── block/        # Модели блоков
│   └── item/         # Модели предметов (многие генерируются)
└── textures/
    ├── block/        # Текстуры блоков
    │   ├── craftables/
    │   ├── dirt/
    │   ├── farmland/
    │   └── grass/
    ├── gui/
    └── item/         # Текстуры предметов
```


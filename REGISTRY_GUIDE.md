# Гайд по системе регистрации (Registry Guide)

Этот документ описывает процесс добавления нового контента в проект с использованием кастомной системы регистров, расположенной в `src/main/java/dev/flomik/stardew/common/registry/`.

Система построена на паттерне **Builder**, который упрощает регистрацию предметов, блоков и связанных с ними сущностей, а также автоматически связывает их с вкладками креатива, генерацией моделей и рендерерами.

---

## Структура проекта

Проект использует **модульную архитектуру**:

```
common/module/
├── craftables/      # Крафтабельные предметы (сундуки, машины)
├── farming/         # Фермерство (кропы, семена, фермерская земля)
├── nature/          # Природные блоки (трава, земля)
├── tools/           # Инструменты (мотыги, лейки)
├── time/            # Система времени/сезонов/погоды
└── player/          # События игрока
```

Классы регистрации находятся в `common/registry/`:
- `ModBlocks.java` - регистрация всех блоков
- `ModItems.java` - регистрация всех предметов
- `ModTabs.java` - вкладки креатива
- `ModMenuTypes.java` - типы меню

---

## 1. Добавление предметов (Items)

Все предметы регистрируются в классе `ModItems.java`.

### Базовый синтаксис

```java
public static final RegistryObject<Item> MY_ITEM = ItemBuilder.create("my_item_name")
        .tab(ModTabs.TAB_NAME)          // Вкладка креатива
        .stacksTo(64)                   // Размер стака (по умолчанию 64)
        .visual(ModelPresets.simple())  // Генерация стандартной модели
        .register();                    // Финальная регистрация
```

### Реальный пример из проекта

```java
// Простой предмет (томат)
public static final RegistryObject<Item> TOMATO = ItemBuilder.create("tomato")
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.simple())
        .register();

// Предмет с кастомным размером стака (мёд)
public static final RegistryObject<Item> HONEY = ItemBuilder
        .create("honey")
        .stacksTo(999)  // Использует систему стаков мода (макс 999)
        .tab(ModTabs.ARTISAN_GOODS)
        .visual(ModelPresets.simple())
        .register();
```

### Предмет с кастомным классом

Если у вас есть свой класс предмета (например, инструмент или семена):

```java
// Семена с кастомным классом
public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create(
        "tomato_seeds",
        p -> new ItemStardewSeed(p, StardewRegistry.id("tomato"))
)
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.seed())
        .register();

// Инструмент (мотыга)
public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
        .create("basic_hoe", p -> new ToolHoe(p, PatternType.SINGLE))
        .stacksTo(1)
        .tab(ModTabs.TOOLS)
        .register();
```

### Доступные методы `ItemBuilder`

- `.create(String name)` - создаёт стандартный `Item`
- `.create(String name, Function<Item.Properties, T> factory)` - создаёт кастомный предмет
- `.stacksTo(int count)` - размер стака (по умолчанию 64, в моде максимум 999)
- `.tab(RegistryObject<CreativeModeTab>)` - вкладка креатива
- `.visual(ItemModelGen)` - генератор модели (см. раздел "Генерация моделей")

### Доступные вкладки (`ModTabs`)

- `ModTabs.BLOCK` - блоки
- `ModTabs.TOOLS` - инструменты
- `ModTabs.CRAFTABLES` - крафтабельные предметы
- `ModTabs.ARTISAN_GOODS` - ремесленные товары
- `ModTabs.CROPS` - кропы и семена

---

## 2. Добавление блоков (Blocks)

Блоки регистрируются в классе `ModBlocks.java`. Используется `BlockBuilder`.

### Простой блок без Block Entity

```java
public static final RegistryObject<BlockDirt> DIRT = BlockBuilder.create("dirt", BlockDirt::new)
        .transform(copy(Blocks.DIRT))  // Копирует свойства ванильного блока
        .seasonal(true)                // Блок меняется в зависимости от сезона
        .item()                        // Создать предмет для блока
        .tab(ModTabs.BLOCK)
        .register();
```

### Блок с кастомными свойствами

```java
public static final RegistryObject<Block> MY_BLOCK = BlockBuilder.create("my_block", Block::new)
        .initialProperties(() -> BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.STONE)
        )
        .item()
        .tab(ModTabs.BLOCK)
        .visual(ModelPresets.useBlockModel())  // Использует модель блока
        .register();
```

### Реальный пример: блок с сезонностью

```java
public static final RegistryObject<BlockGrassSurface> GRASS = BlockBuilder.create("grass", BlockGrassSurface::new)
        .transform(copy(Blocks.GRASS_BLOCK))
        .seasonal(true)  // Блок меняет текстуру в зависимости от сезона
        .item()
        .tab(ModTabs.BLOCK)
        .register();
```

### Доступные методы `BlockBuilder`

- `.create(String name, Function<BlockBehaviour.Properties, T> factory)` - создание блока
- `.initialProperties(Supplier<BlockBehaviour.Properties>)` - задать свойства напрямую
- `.properties(UnaryOperator<BlockBehaviour.Properties>)` - модифицировать свойства
- `.transform(UnaryOperator)` - применить пресет свойств (см. `BlockPresets`)
- `.seasonal(boolean)` - включить сезонные изменения текстуры
- `.item()` - переключиться в режим настройки предмета-блока
- `.noItem()` - блок без предмета (технический блок)

### Доступные пресеты (`BlockPresets`)

```java
// Деревянная машина (сундуки, бочки и т.д.)
.transform(woodMachine())  // MapColor.WOOD, strength 2.5F, звук WOOD, noOcclusion

// Кроп (растения)
.transform(crop())  // MapColor.PLANT, strength 2.5F, звук CROP, noOcclusion, instabreak

// Копировать свойства ванильного блока
.transform(copy(Blocks.STONE))  // Полностью копирует свойства указанного блока
```

### Примеры использования пресетов

```java
// Деревянная машина (бочка)
public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder.create("keg", BlockKeg::new)
        .transform(woodMachine())  // Применяет пресет для деревянных машин
        .blockEntity(BlockEntityKeg::new)
        .renderer(VisualItemAboveRenderer::new)
        .item()
        .tab(ModTabs.CRAFTABLES)
        .visual(ModelPresets.simple())
        .register();

// Кроп (технический блок, без предмета)
public static final BlockEntry<BlockCrop, CropBlockEntity> CROP = BlockBuilder.create("crop", BlockCrop::new)
        .transform(crop())  // Применяет пресет для кропов
        .blockEntity(CropBlockEntity::new)
        .noItem()  // Кроп не имеет предмета
        .register();
```

---

## 3. Блоки с Block Entity (Tile Entities)

Система позволяет зарегистрировать Блок, Предмет, Тип Блок Энтити (BlockEntityType) и Рендерер одним вызовом.

**Важно:** Для этого используется метод `.blockEntity(...)`.

### Базовый пример

```java
public static final BlockEntry<MyBlock, MyBlockEntity> MY_MACHINE = BlockBuilder.create("my_machine", MyBlock::new)
        .transform(woodMachine())
        .blockEntity(MyBlockEntity::new)      // Регистрация фабрики BE
        .renderer(VisualItemAboveRenderer::new) // Автоматическая регистрация рендерера
        .item()
        .tab(ModTabs.CRAFTABLES)
        .visual(ModelPresets.simple())
        .register();
```

### Реальный пример: Бочка (Keg)

```java
public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder.create("keg", BlockKeg::new)
        .transform(woodMachine())
        .blockEntity(BlockEntityKeg::new)
        .renderer(VisualItemAboveRenderer::new)  // Показывает предмет над блоком
        .item()
        .tab(ModTabs.CRAFTABLES)
        .visual(ModelPresets.simple())
        .register();
```

### Реальный пример: Сундук с кастомным рендерером

```java
public static final BlockEntry<BlockChest, BlockEntityChest> CHEST = BlockBuilder.create("chest", BlockChest::new)
        .transform(woodMachine())
        .blockEntity(BlockEntityChest::new)
        .renderer(ChestRenderer::new)  // Кастомный рендерер для сундука
        .item()
        .tab(ModTabs.CRAFTABLES)
        .visual(ModelPresets.simple())
        .register();
```

### Реальный пример: Фермерская земля с Block Entity

```java
public static final BlockEntry<BlockFarmland, FarmlandBlockEntity> FARMLAND = BlockBuilder.create("farmland", BlockFarmland::new)
        .transform(copy(Blocks.DIRT))
        .seasonal(true)  // Меняет текстуру по сезонам
        .blockEntity(FarmlandBlockEntity::new)
        .item()
        .tab(ModTabs.BLOCK)
        .visual(ModelPresets.simple())
        .register();
```

### Доступ к объектам через `BlockEntry`

После регистрации вы получаете `BlockEntry<T, E>`, который содержит:

```java
MY_MACHINE.get()              // Блок (Block)
MY_MACHINE.getItem()          // Предмет (RegistryObject<Item>)
MY_MACHINE.getType()          // Тип BE (RegistryObject<BlockEntityType<E>>)
MY_MACHINE.getTypeValue()     // Тип BE (BlockEntityType<E>) - для использования в коде
```

### Доступные методы `BlockEntityBuilder`

- `.blockEntity(BiFunction<BlockPos, BlockState, E>)` - регистрация фабрики BE
- `.renderer(BlockEntityRendererProvider<E>)` - автоматическая регистрация рендерера на клиенте
- `.item()` - переключиться в режим настройки предмета
- `.noItem()` - BE без предмета (технический блок)
- `.tab(RegistryObject<CreativeModeTab>)` - вкладка креатива
- `.visual(ItemModelGen)` - генератор модели предмета

### Требования к конструктору Block Entity

**Важно:** Конструктор `BlockEntity` должен принимать `(BlockPos, BlockState)`:

```java
// ✅ Правильно
public class MyBlockEntity extends BlockEntity {
    public MyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MY_MACHINE.getTypeValue(), pos, state);
    }
}

// ❌ Неправильно (не будет работать с BlockBuilder)
public class MyBlockEntity extends BlockEntity {
    public MyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
```

Если нужен конструктор с `BlockEntityType`, добавьте оба:

```java
public class FarmlandBlockEntity extends BlockEntity {
    // Основной конструктор
    public FarmlandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    // Конструктор для BlockBuilder
    public FarmlandBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlocks.FARMLAND.getTypeValue(), pos, state);
    }
}
```

---

## 4. Генерация моделей (DataGen)

В проекте используется система автоматической привязки генераторов моделей к предметам.

### Как работает

В методе `.visual(...)` билдера вы указываете стратегию генерации.
Класс `ModelPresets` содержит основные пресеты:

- `ModelPresets.simple()` - стандартная модель `item/generated` (плоский предмет)
- `ModelPresets.handheld()` - модель для инструментов `item/handheld`
- `ModelPresets.useBlockModel()` - использует модель блока для предмета
- `ModelPresets.seed()` - модель для семян (аналогична `simple()`)

### Примеры использования

```java
// Простой предмет (томат, мёд)
.visual(ModelPresets.simple())

// Инструмент (мотыга, лейка)
.visual(ModelPresets.handheld())

// Предмет блока (использует модель блока)
.visual(ModelPresets.useBlockModel())

// Семена
.visual(ModelPresets.seed())
```

### Реальные примеры из проекта

```java
// Простой предмет
public static final RegistryObject<Item> TOMATO = ItemBuilder.create("tomato")
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.simple())
        .register();

// Семена
public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create(
        "tomato_seeds",
        p -> new ItemStardewSeed(p, StardewRegistry.id("tomato"))
)
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.seed())
        .register();
```

**Примечание:** Для блоков (BlockState) автоматическая генерация в `BlockBuilder` на данный момент не настроена явно в коде билдера (только для Item-моделей блоков), поэтому BlockState и модели блоков обычно создаются вручную в `resources/assets/stardew/blockstates/` и `models/block/`, либо через отдельные Datagen классы.

---

## 5. Клиентская часть (Рендереры)

### Автоматическая регистрация (рекомендуется)

Рендереры автоматически регистрируются при использовании `.renderer()` в `BlockBuilder`:

```java
public static final BlockEntry<MyBlock, MyBlockEntity> MY_MACHINE = BlockBuilder.create("my_machine", MyBlock::new)
        .blockEntity(MyBlockEntity::new)
        .renderer(MyRenderer::new)  // Рендерер будет зарегистрирован автоматически
        .item()
        .register();
```

Все рендереры обрабатываются классом `RendererRegistry` и регистрируются в `ClientSetup.java`.

### Доступные рендереры

#### `VisualItemAboveRenderer`
Показывает предмет над блоком (для машин, которые обрабатывают предметы):

```java
.renderer(VisualItemAboveRenderer::new)
```

**Используется для:**
- Бочки (Keg)
- Улья (BeeHouse)
- Сыроварни (CheesePress)

#### `ChestRenderer`
Кастомный рендерер для сундуков с анимацией:

```java
.renderer(ChestRenderer::new)
```

**Используется для:**
- Сундуков (Chest)

### Ручная регистрация (для особых случаев)

Если нужна ручная регистрация (например, для слоёв моделей), используйте `ClientSetup.java`:

```java
@SubscribeEvent
public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(ChestRenderer.LAYER_LOCATION, ChestRenderer::createBodyLayer);
}
```

---

## 6. Создание кастомного блока

### Пример: Блок с Block Entity и инвентарём

```java
// 1. Создайте класс блока в common/module/craftables/block/
package dev.flomik.stardew.common.module.craftables.block;

public class BlockKeg extends Block implements EntityBlock {
    // ... реализация блока
}

// 2. Создайте Block Entity в common/module/craftables/blockentity/
package dev.flomik.stardew.common.module.craftables.blockentity;

public class BlockEntityKeg extends BlockEntity {
    public BlockEntityKeg(BlockPos pos, BlockState state) {
        super(ModBlocks.KEG.getTypeValue(), pos, state);
    }
    // ... реализация BE
}

// 3. Зарегистрируйте в ModBlocks.java
public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder.create("keg", BlockKeg::new)
        .transform(woodMachine())
        .blockEntity(BlockEntityKeg::new)
        .renderer(VisualItemAboveRenderer::new)
        .item()
        .tab(ModTabs.CRAFTABLES)
        .visual(ModelPresets.simple())
        .register();
```

### Пример: Сезонный блок

```java
// Блок меняет текстуру в зависимости от сезона
public static final RegistryObject<BlockGrassSurface> GRASS = BlockBuilder.create("grass", BlockGrassSurface::new)
        .transform(copy(Blocks.GRASS_BLOCK))
        .seasonal(true)  // Включает сезонные изменения
        .item()
        .tab(ModTabs.BLOCK)
        .register();
```

### Пример: Технический блок (без предмета)

```java
// Кроп не имеет предмета, это технический блок
public static final BlockEntry<BlockCrop, CropBlockEntity> CROP = BlockBuilder.create("crop", BlockCrop::new)
        .transform(crop())
        .blockEntity(CropBlockEntity::new)
        .noItem()  // Блок без предмета
        .register();
```

---

## 7. Создание кастомного предмета

### Пример: Простой предмет

```java
public static final RegistryObject<Item> TOMATO = ItemBuilder.create("tomato")
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.simple())
        .register();
```

### Пример: Предмет с кастомным классом

```java
// 1. Создайте класс предмета
package dev.flomik.stardew.common.module.farming.item;

public class ItemStardewSeed extends Item {
    private final ResourceLocation cropId;
    
    public ItemStardewSeed(Item.Properties properties, ResourceLocation cropId) {
        super(properties);
        this.cropId = cropId;
    }
}

// 2. Зарегистрируйте в ModItems.java
public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create(
        "tomato_seeds",
        p -> new ItemStardewSeed(p, StardewRegistry.id("tomato"))
)
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.seed())
        .register();
```

### Пример: Инструмент

```java
// Инструмент с кастомными параметрами
public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
        .create("basic_hoe", p -> new ToolHoe(p, PatternType.SINGLE))
        .stacksTo(1)
        .tab(ModTabs.TOOLS)
        .register();
```

---

## 8. Работа с размерами стаков

В моде реализована система стаков до 999 предметов (см. `StackConfig.MAX_STACK_SIZE`).

### Установка размера стака

```java
// Стандартный размер (64)
public static final RegistryObject<Item> TOMATO = ItemBuilder.create("tomato")
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.simple())
        .register();

// Кастомный размер (999 - максимум в моде)
public static final RegistryObject<Item> HONEY = ItemBuilder
        .create("honey")
        .stacksTo(999)
        .tab(ModTabs.ARTISAN_GOODS)
        .visual(ModelPresets.simple())
        .register();

// Нестакаемый предмет (1)
public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
        .create("basic_hoe", p -> new ToolHoe(p, PatternType.SINGLE))
        .stacksTo(1)
        .tab(ModTabs.TOOLS)
        .register();
```

**Примечание:** Система стаков автоматически применяется ко всем предметам с размером стака > 1. Максимальный размер стака задаётся в `StackConfig.MAX_STACK_SIZE` (по умолчанию 999).

---

## Краткий чек-лист по добавлению контента

1. **Определите модуль**: Решите, в какой модуль поместить класс (`craftables`, `farming`, `nature`, `tools` и т.д.)

2. **Создайте классы**:
   - Блок: `common/module/{module}/block/BlockName.java`
   - Block Entity: `common/module/{module}/blockentity/BlockEntityName.java`
   - Предмет: `common/module/{module}/item/ItemName.java`

3. **Зарегистрируйте в регистрах**:
   - Блоки → `ModBlocks.java`
   - Предметы → `ModItems.java`

4. **Настройте свойства**:
   - Используйте пресеты (`woodMachine()`, `crop()`, `copy()`)
   - Укажите вкладку креатива
   - Выберите генератор модели

5. **Добавьте рендерер** (если нужен):
   - Используйте `.renderer()` для автоматической регистрации

6. **Ресурсы**:
   - Текстуры: `resources/assets/stardew/textures/item/` или `textures/block/`
   - Lang файлы: `resources/assets/stardew/lang/en_us.json` и `ru_ru.json`

---

## Полные примеры из проекта

### Пример 1: Бочка (Keg) - полная регистрация

```java
// В ModBlocks.java
public static final BlockEntry<BlockKeg, BlockEntityKeg> KEG = BlockBuilder.create("keg", BlockKeg::new)
        .transform(woodMachine())                    // Пресет для деревянных машин
        .blockEntity(BlockEntityKeg::new)            // Регистрация Block Entity
        .renderer(VisualItemAboveRenderer::new)      // Рендерер для показа предмета
        .item()                                      // Создать предмет
        .tab(ModTabs.CRAFTABLES)                    // Вкладка креатива
        .visual(ModelPresets.simple())               // Генератор модели
        .register();                                 // Регистрация

// Это создаёт:
// - Блок stardew:keg
// - BlockItem для него
// - BlockEntityType для BlockEntityKeg
// - Автоматически регистрирует VisualItemAboveRenderer на клиенте
```

### Пример 2: Семена - полная регистрация

```java
// В ModItems.java
public static final RegistryObject<ItemStardewSeed> TOMATO_SEEDS = ItemBuilder.create(
        "tomato_seeds",
        p -> new ItemStardewSeed(p, StardewRegistry.id("tomato"))
)
        .tab(ModTabs.CROPS)
        .visual(ModelPresets.seed())
        .register();
```

### Пример 3: Инструмент - полная регистрация

```java
// В ModItems.java
public static final RegistryObject<ToolHoe> BASIC_HOE = ItemBuilder
        .create("basic_hoe", p -> new ToolHoe(p, PatternType.SINGLE))
        .stacksTo(1)
        .tab(ModTabs.TOOLS)
        .register();
```

---

## Полезные советы

1. **Используйте пресеты**: Вместо ручной настройки свойств используйте `woodMachine()`, `crop()`, `copy()`

2. **Модульная структура**: Размещайте классы в соответствующих модулях для лучшей организации

3. **Автоматическая регистрация**: Используйте `.renderer()` вместо ручной регистрации в `ClientSetup`

4. **Размеры стаков**: Помните, что в моде максимум 999, но можно задать меньше через `.stacksTo()`

5. **Сезонность**: Используйте `.seasonal(true)` для блоков, которые должны меняться по сезонам

6. **Технические блоки**: Используйте `.noItem()` для блоков, которые не должны иметь предмет (кропы, технические блоки)

---

## Часто задаваемые вопросы

**Q: Как создать блок с кастомными свойствами?**
A: Используйте `.initialProperties()` или `.properties()` для настройки свойств напрямую.

**Q: Как создать блок без предмета?**
A: Используйте `.noItem()` вместо `.item()`.

**Q: Как добавить рендерер?**
A: Используйте `.renderer(YourRenderer::new)` в `BlockEntityBuilder`.

**Q: Как работает сезонность?**
A: Используйте `.seasonal(true)` - система автоматически меняет текстуру блока в зависимости от сезона.

**Q: Где размещать классы?**
A: В соответствующих модулях: `common/module/{module}/block/`, `common/module/{module}/blockentity/`, и т.д.

**Q: Какой максимальный размер стака?**
A: В моде максимум 999 (настраивается в `StackConfig.MAX_STACK_SIZE`).

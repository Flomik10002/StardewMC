# Гайд по системе регистрации (Registry Guide)

Этот документ описывает процесс добавления нового контента в проект с использованием кастомной системы регистров, расположенной в `src/main/java/dev/flomik/stardew/common/registry/`.

Система построена на паттерне **Builder**, который упрощает регистрацию предметов, блоков и связанных с ними сущностей, а также автоматически связывает их с вкладками креатива и генерацией моделей.

---

## 1. Добавление предметов (Items)

Все предметы регистрируются в классе `ModItems.java`.

### Синтаксис
Используйте `ItemBuilder.create("id_предмета")` для начала цепочки регистрации.

```java
public static final RegistryObject<Item> MY_ITEM = ItemBuilder.create("my_item_name")
        .tab(ModTabs.TAB_NAME)          // Вкладка креатива
        .stacksTo(64)                   // Размер стака (по умолчанию 64)
        .model(ModelPresets.simple())   // Генерация стандартной модели (generated)
        .register();                    // Финальная регистрация
```

### Пример с кастомным классом предмета
Если у вас есть свой класс предмета (например, инструмент):

```java
public static final RegistryObject<ToolHoe> MY_HOE = ItemBuilder
        .create("my_hoe", p -> new ToolHoe(p, PatternType.SINGLE)) // Передача конструктора
        .stacksTo(1)
        .tab(ModTabs.TOOLS)
        .register();
```

---

## 2. Добавление блоков (Blocks)

Блоки регистрируются в классе `ModBlocks.java`. Используется `BlockBuilder`.

### Простой блок
Создает блок и соответствующий ему `BlockItem`.

```java
public static final RegistryObject<Block> MY_BLOCK = BlockBuilder.create("my_block", Block::new)
        .initialProperties(() -> BlockBehaviour.Properties.of().strength(2.0f)) // Свойства блока
        // ИЛИ
        .transform(BlockPresets.copy(Blocks.STONE)) // Копирование свойств ванильного блока
        .item()                         // Создать предмет для блока
            .tab(ModTabs.BLOCK)         // Вкладка для предмета
            .model(ModelPresets.simple()) // Простая модель предмета (block/parent)
            .register();                // Вернуться к регистрации блока
```

### Доступные методы `BlockBuilder`
- `.transform(UnaryOperator)`: Применяет пресет свойств (например `BlockPresets.woodMachine()`).
- `.item()`: Переключается в режим настройки предмета-блока (`BlockItem`).
- `.noItem()`: Блок регистрируется без предмета (например, технический блок или кроп).

---

## 3. Блоки с Block Entity (Tile Entities)

Система позволяет зарегистрировать Блок, Предмет и Тип Блок Энтити (BlockEntityType) одним вызовом.

**Важно:** Для этого используется метод `.blockEntity(...)`.

### Пример
```java
// Возвращает BlockEntry, содержащий ссылки на Блок, Предмет и Тип BE
public static final BlockEntry<MyBlock, MyBlockEntity> MY_MACHINE = BlockBuilder.create("my_machine", MyBlock::new)
        .transform(BlockPresets.woodMachine())
        .blockEntity(MyBlockEntity::new) // Регистрация фабрики BE
        .item()
            .tab(ModTabs.CRAFTABLES)
            .model(ModelPresets.simple())
        .register(); // Возвращает BlockEntry
```

Для доступа к объектам используйте:
- `MY_MACHINE.get()` -> Блок
- `MY_MACHINE.getItem()` -> Предмет
- `MY_MACHINE.getTypeValue()` -> Тип Block Entity (`BlockEntityType<MyBlockEntity>`)

---

## 4. Генерация моделей (DataGen)

В проекте используется система автоматической привязки генераторов моделей к предметам.

### Как работает
В методе `.model(...)` билдера вы указываете стратегию генерации.
Класс `ModelPresets` содержит основные пресеты:
- `ModelPresets.simple()`: Стандартная модель `item/generated` (плоский предмет).
- `ModelPresets.handheld()`: Модель для инструментов.
- `ModelPresets.seed()`: Специфичная модель для семян.

Эти привязки обрабатываются классом `DataGenManager`, который используется в фазе `gatherData` (обычно в классе `StardewItemModels` или аналогичном в `datagen` пакете).

**Примечание:** Для блоков (BlockState) автоматическая генерация в `BlockBuilder` на данный момент не настроена явно в коде билдера (только для Item-моделей блоков), поэтому BlockState и модели блоков обычно создаются вручную в `resources/assets/stardew/blockstates/` и `models/block/`, либо через отдельные Datagen классы.

---

## 5. Клиентская часть (Рендереры)

Регистрация `BlockEntityRenderer` происходит в классе `ClientSetup.java` (пакет `client`).

### Регистрация рендерера
Используйте событие `EntityRenderersEvent.RegisterRenderers`.

```java
@SubscribeEvent
public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    // Пример для обычного блока
    event.registerBlockEntityRenderer(
            ModBlockEntities.MY_ENTITY_TYPE.get(), 
            MyRenderer::new
    );

    // Пример для блока, зарегистрированного через BlockEntry (ModBlocks)
    event.registerBlockEntityRenderer(
            ModBlocks.MY_MACHINE.getTypeValue(), // Получение типа из Entry
            MyRenderer::new
    );
}
```

---

## Краткий чек-лист по добавлению контента

1. **Создать классы**: Создайте классы для `Block` и `BlockEntity` (если нужны) в `common/registry/block/...`.
2. **Зарегистрировать Блок/BE**: В `ModBlocks.java` добавьте поле с `BlockBuilder`.
3. **Зарегистрировать Предмет**: Если предмет отдельный (не блок), добавьте его в `ModItems.java` через `ItemBuilder`.
4. **Ресурсы**: Добавьте текстуры в `resources/assets/stardew/textures/...`.
5. **Клиент**: Если у BE есть особый рендер, зарегистрируйте его в `ClientSetup.java`.
6. **Lang**: Не забудьте добавить название в `en_us.json` / `ru_ru.json`.


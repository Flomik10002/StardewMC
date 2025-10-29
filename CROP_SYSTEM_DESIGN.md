# Система растений StardewMC - Полная документация

## Оглавление

1. [Обзор системы](#обзор-системы)
2. [Архитектура](#архитектура)
3. [Механика роста растений](#механика-роста-растений)
4. [Стадии роста](#стадии-роста)
5. [Удобрения и бонусы роста](#удобрения-и-бонусы-роста)
6. [Полив и грядки](#полив-и-грядки)
7. [Сбор урожая](#сбор-урожая)
8. [Регров (повторный урожай)](#регров-повторный-урожай)
9. [Сезонность](#сезонность)
10. [Типы растений](#типы-растений)
11. [Качество урожая](#качество-урожая)
12. [Особые механики](#особые-механики)
13. [Интеграция с Minecraft](#интеграция-с-minecraft)
14. [Структура данных](#структура-данных)

---

## Обзор системы

### Концепция
Переносим полную систему растений из Stardew Valley в Minecraft, адаптируя её под особенности игры:
- **Ванильный подход**: используем BlockState, BlockEntity, а не кастомные блоки
- **Грядки как основа**: культуры растут только на обработанных грядках (FarmlandBlock)
- **Стадийный рост**: растения проходят через несколько визуальных стадий
- **Полив**: требуется ежедневный полив для роста
- **Удобрения**: влияют на скорость роста и качество урожая
- **Сезонность**: растения растут только в определённые сезоны

### Что НЕ переносим
- **spriteIndex, tintColor**: используем Minecraft модели/текстуры
- **Визуальный шейк/анимации**: Minecraft ограничения
- **Гигантские культуры**: слишком сложно для первой версии
- **Дикие семена (wild seeds)**: фокус на обычных культурах

---

## Архитектура

### Компоненты системы

#### 1. **CropDefinition** (Определение культуры)
```
Класс: dev.flomik.stardew.core.crop.def.CropDef
Хранит статические данные о культуре:
- ID культуры
- Сезоны роста
- Фазы роста (количество дней в каждой)
- Метод сбора (рука/коса)
- Предмет урожая
- Регров (если есть)
- Специальные флаги (paddy, raised, needsWatering)
```

#### 2. **CropInstance** (Экземпляр растения)
```
Класс: dev.flomik.stardew.core.crop.state.CropInstance
BlockEntity, хранящий состояние конкретного растения:
- Текущая фаза роста (currentPhase)
- День в текущей фазе (dayOfCurrentPhase)
- Флаг полной зрелости (fullyGrown)
- Готовность к сбору (readyForHarvest)
- Мёртвое растение (dead)
- Скорректированные фазы (phaseDaysAdjusted)
- Случайный флип для визуального разнообразия
```

#### 3. **CropBlock**
```
Класс: dev.flomik.stardew.core.crop.block.BlockCrop
Блок растения с BlockState свойствами:
- AGE (0-7): визуальная стадия роста
- WATERLOGGED: полив не влияет, но для совместимости
```

#### 4. **GrowthSystem** (Система роста)
```
Класс: dev.flomik.stardew.core.crop.logic.GrowthSystem
Обрабатывает рост всех растений каждое утро:
- Проверка полива грядки
- Проверка сезона
- Прогресс по фазам
- Применение бонусов paddy-культур
- Обновление визуального AGE
```

#### 5. **HarvestSystem** (Система сбора)
```
Класс: dev.flomik.stardew.core.crop.logic.HarvestSystem
Обработка сбора урожая:
- Расчёт количества урожая
- Расчёт качества (basic/silver/gold/iridium)
- Дроп предметов
- Обработка регрова
```

#### 6. **FarmlandIntegration** (Интеграция с грядками)
```
Класс: dev.flomik.stardew.core.farmland.FarmlandState
Взаимодействие с грядками:
- Состояние полива
- Тип удобрения
- Влияние на скорость роста
```

---

## Механика роста растений

### Базовый алгоритм (из Stardew Valley Crop.cs)

#### Утренний тик (Day Update)
```java
public void newDay(int waterState) {
    // 1. Проверка гибели
    if (isOutdoors && (dead || !IsInSeason())) {
        Kill();
        return;
    }
    
    // 2. Проверка полива
    if (waterState != WATERED) {
        if (needsWatering) {
            return; // не растёт без полива
        }
    }
    
    // 3. Прогресс роста
    if (!fullyGrown) {
        dayOfCurrentPhase = Math.min(
            dayOfCurrentPhase + 1,
            phaseDays[currentPhase]
        );
    } else {
        // Регров: счётчик уменьшается
        dayOfCurrentPhase--;
    }
    
    // 4. Смена фазы
    if (dayOfCurrentPhase >= phaseDays[currentPhase] 
        && currentPhase < phaseDays.size() - 1) {
        currentPhase++;
        dayOfCurrentPhase = 0;
    }
    
    // 5. Пропуск нулевых фаз
    while (currentPhase < phaseDays.size() - 1 
           && phaseDays[currentPhase] <= 0) {
        currentPhase++;
    }
    
    // 6. Проверка готовности к сбору
    if (currentPhase >= phaseDays.size() - 1 
        && (!fullyGrown || dayOfCurrentPhase <= 0)) {
        readyForHarvest = true;
    }
}
```

#### Применение к Minecraft
- **Утренний тик** вызывается в `MorningPass.java` для всех растений
- **Грядки** должны быть политы накануне вечером
- **Визуальное представление**: AGE = (currentPhase * 7) / maxPhases

---

## Стадии роста

### Фазы (Phases)
Каждое растение проходит через несколько **фаз роста**, каждая длится определённое количество дней.

#### Пример: Помидор
```
Phases:  [1, 2, 2, 3, 99999]
              ^  ^  ^  ^  ^
            seed→→→→ready (final phase)

Итого: 8 дней до созревания
```

- **Фаза 0** (seed): семя, 1 день
- **Фаза 1**: росток, 2 дня
- **Фаза 2**: маленькое растение, 2 дня
- **Фаза 3**: большое растение, 3 дня
- **Фаза 4** (final): готово к сбору, **99999** дней (бесконечность)

#### Визуальные стадии (AGE)
Minecraft BlockState свойство `AGE: 0-7` отображает визуальную стадию:
```
AGE = Math.min(7, (currentPhase * 7) / (totalPhases - 1))
```

**Пример:**
- Phase 0 → AGE 0-1 (семя/росток)
- Phase 1 → AGE 2-3 (маленький)
- Phase 2 → AGE 4-5 (средний)
- Phase 3 → AGE 6 (большой)
- Phase 4 → AGE 7 (созревший)

### Последняя фаза (Final Phase)
- В SV всегда **99999** дней
- Означает "бесконечную" фазу
- Растение остаётся готовым к сбору до взаимодействия игрока
- После сбора: либо удаляется, либо переходит в регров

---

## Удобрения и бонусы роста

### Типы удобрений

#### 1. Удобрения скорости роста (Speed-Gro)
```
- Basic Speed-Gro:   10% ускорение
- Quality Speed-Gro: 25% ускорение  
- Hyper Speed-Gro:   33% ускорение
```

#### 2. Удобрения качества (Fertilizer)
```
- Basic Fertilizer:  уровень качества 1
- Quality Fertilizer: уровень качества 2
- Deluxe Fertilizer: уровень качества 3
```

#### 3. Удобрения удержания воды (Retaining Soil)
```
- Basic Retaining:   33% шанс сохранить полив
- Quality Retaining: 66% шанс
- Deluxe Retaining:  100% шанс
```

### Механизм ускорения (из SV)

#### Применение при посадке
```java
public void applySpeedIncreases() {
    // 1. Собираем бонусы
    float speedIncrease = 0f;
    speedIncrease += getFertilizerSpeedBoost(); // 0.1/0.25/0.33
    if (isPaddyCrop && isNearWater()) {
        speedIncrease += 0.25f; // paddy bonus
    }
    if (farmerHasTillerProfession) {
        speedIncrease += 0.1f; // профессия (опционально)
    }
    
    // 2. Считаем общее количество дней роста
    int totalDays = 0;
    for (int i = 0; i < phaseDays.size() - 1; i++) {
        totalDays += phaseDays[i];
    }
    
    // 3. Удаляем дни
    int daysToRemove = (int)Math.ceil(totalDays * speedIncrease);
    
    // 4. Распределяем удаление по фазам (3 попытки)
    int tries = 0;
    while (daysToRemove > 0 && tries < 3) {
        for (int i = 0; i < phaseDays.size(); i++) {
            int days = phaseDays[i];
            // Не трогаем первую фазу если она 1 день
            // Не трогаем финальную фазу (99999)
            if ((i > 0 || days > 1) && days != 99999 && days > 0) {
                phaseDays[i]--;
                daysToRemove--;
            }
            if (daysToRemove <= 0) break;
        }
        tries++;
    }
}
```

#### Пример: Помидор с Hyper Speed-Gro (33%)
```
Изначально: [1, 2, 2, 3] = 8 дней
Удаляем: ceil(8 * 0.33) = 3 дня

Итерация 1: [1, 1, 1, 2] - удалено 3 дня
Итого: 5 дней вместо 8
```

---

## Полив и грядки

### Механика полива (из HoeDirt.cs)

#### Состояния грядки
```
- DRY (0):      не полита
- WATERED (1):  полита
- INVISIBLE (2): специальное состояние (не используем)
```

#### Проверка необходимости полива
```java
public boolean needsWatering() {
    if (crop != null && (!readyForHarvest() || crop.RegrowsAfterHarvest())) {
        return crop.GetData().NeedsWatering;
    }
    return false;
}
```

#### Специальные культуры без полива
Некоторые культуры не требуют полива:
- **Fiber** (волокно): растёт без воды
- **Tea Sapling**: специальный саженец

### Paddy-культуры (рис)

#### Автоматический полив
Если культура **isPaddyCrop** и рядом (в радиусе 3 блока) есть вода:
```java
public boolean paddyWaterCheck() {
    if (!hasPaddyCrop()) return false;
    
    int range = 3;
    for (int x = -range; x <= range; x++) {
        for (int z = -range; z <= range; z++) {
            if (isWaterTile(tileX + x, tileY + z)) {
                return true; // автополив
            }
        }
    }
    return false;
}
```
- Грядка автоматически считается политой
- Дополнительный бонус **+25%** к скорости роста

### Удержание воды (Retaining Soil)
Каждое утро с шансом грядка остаётся политой:
```java
if (!game.random.nextDouble() < GetFertilizerWaterRetentionChance()) {
    state = DRY;
}
```

---

## Сбор урожая

### Методы сбора

#### 1. Сбор рукой (GRAB)
- **Взаимодействие** (ПКМ по культуре)
- Забирает урожай, возможен регров
- Большинство культур

#### 2. Сбор косой (SCYTHE)
- **Удар косой** по культуре
- Обычно выбрасывает предметы на землю
- Примеры: пшеница, кукуруза, amaranth

### Количество урожая

#### Базовое количество
```java
int numToHarvest = random.nextInt(harvestMin, harvestMax + 1);

// Дополнительный урожай (экстра-шанс)
if (extraHarvestChance > 0.0) {
    while (random.nextDouble() < Math.min(0.9, extraHarvestChance)) {
        numToHarvest++;
    }
}
```

#### Факторы влияния
- **harvestMin/Max**: базовый диапазон
- **extraHarvestChance**: геометрическая прогрессия (0..0.9)
- **Farming Level**: может влиять на max (опционально)
- **Удача**: малый шанс удвоения урожая

#### Пример: Клубника
```
harvestMin: 1
harvestMax: 1
extraHarvestChance: 0.2

Каждый урожай:
- Гарантированно 1 ягода
- 20% шанс на +1
- 4% шанс на +2 (0.2 * 0.2)
- 0.8% шанс на +3
```

### Обработка сбора (harvest)
```java
public boolean harvest(int x, int y, HoeDirt soil) {
    if (dead || !readyForHarvest) return false;
    
    // 1. Расчёт количества
    int count = calculateHarvestCount();
    
    // 2. Расчёт качества
    int quality = calculateQuality(soil);
    
    // 3. Дроп предметов
    for (int i = 0; i < count; i++) {
        Item item = createHarvestItem(quality);
        dropOrGive(item);
    }
    
    // 4. Регров или удаление
    if (regrowDays > 0) {
        fullyGrown = true;
        dayOfCurrentPhase = regrowDays;
        readyForHarvest = false;
    } else {
        // Удаляем культуру
        soil.crop = null;
    }
    
    return true;
}
```

---

## Регров (повторный урожай)

### Механизм регрова (из SV)

#### Культуры с регровом
```
Помидор:  regrowDays = 4
Клубника: regrowDays = 4  
Черника:  regrowDays = 4
Кукуруза: regrowDays = 4
```

#### Состояние после сбора
```java
public void onHarvestDone() {
    if (regrowDays <= 0) {
        // Однократный урожай - удаляем
        removeFromWorld();
    } else {
        // Регров
        fullyGrown = true;
        currentPhase = phaseDays.size() - 1; // последняя фаза
        dayOfCurrentPhase = regrowDays;      // таймер до нового урожая
        readyForHarvest = false;
    }
}
```

#### Рост после сбора
Каждый день:
```java
if (fullyGrown) {
    dayOfCurrentPhase--;  // УМЕНЬШАЕТСЯ
    
    if (dayOfCurrentPhase <= 0 && currentPhase == finalPhase) {
        readyForHarvest = true;
    }
}
```

### Визуализация
- **Фаза остаётся последней** (AGE = 7)
- **Можно добавить промежуточные текстуры** для регрова (опционально)

---

## Сезонность

### Сезоны Minecraft
```
- Spring (Весна)
- Summer (Лето)
- Fall (Осень)
- Winter (Зима)
```

### Проверка сезона
```java
public boolean IsInSeason(GameLocation location) {
    // Теплицы/закрытые локации - всегда растёт
    if (location.SeedsIgnoreSeasonsHere()) {
        return true;
    }
    
    // Проверка списка сезонов культуры
    return cropData.Seasons.contains(location.GetSeason());
}
```

### Смерть вне сезона
Каждое утро:
```java
if (isOutdoors && !IsInSeason()) {
    Kill(); // dead = true
}
```

#### Обработка мёртвых культур
- **Визуально**: коричневая увядшая текстура
- **Можно удалить** косой/мотыгой
- **Не растут**, не дают урожай

### Зимняя обработка
В SV зимой все культуры (кроме зимних семян) погибают:
```java
if (season == WINTER && !isWildSeedCrop() && !IsInSeason()) {
    destroyCrop();
}
```

### Примеры культур по сезонам

#### Весна
- Cauliflower (капуста)
- Potato (картофель)
- Parsnip (пастернак)
- Green Bean (зелёная фасоль)
- Coffee Bean (кофе)

#### Лето
- Tomato (помидор)
- Blueberry (черника)
- Hot Pepper (острый перец)
- Wheat (пшеница)
- Corn (кукуруза) - лето+осень

#### Осень
- Pumpkin (тыква)
- Eggplant (баклажан)
- Cranberries (клюква)
- Grape (виноград)

#### Всесезонные
- Ancient Fruit (древний фрукт) - теплица
- Sweet Gem Berry - только осень, но особые условия

---

## Типы растений

### 1. Обычные культуры (Regular Crops)
```
Характеристики:
- Растут на грядках
- Требуют полив
- Одноразовый или регров
- Метод сбора: рука
```

**Примеры:**
- Parsnip (пастернак): [1,1,1,2], весна, без регрова
- Cauliflower (капуста): [1,2,3,3], весна, без регрова
- Potato (картофель): [1,1,2,2], весна, без регрова

### 2. Культуры с регровом (Regrowable Crops)
```
Характеристики:
- После первого урожая продолжают плодоносить
- regrowDays > 0
- Остаются на грядке весь сезон
```

**Примеры:**
- Tomato: regrowDays = 4
- Strawberry: regrowDays = 4
- Corn: regrowDays = 4
- Blueberry: regrowDays = 4

### 3. Трелис-культуры (Trellis/Raised Crops)
```
Характеристики:
- isRaised = true
- Блокируют проход игрока
- Обычно высокие (виноград, хмель)
```

**Примеры:**
- Grape (виноград)
- Hops (хмель)
- Green Bean (фасоль)

#### Реализация в Minecraft
- BlockState с **collision box выше**
- Player не может пройти через зрелую культуру

### 4. Культуры для косы (Scythe Crops)
```
Характеристики:
- harvestMethod = SCYTHE
- Собираются косой
- Обычно зерновые
```

**Примеры:**
- Wheat (пшеница)
- Amaranth (амарант)
- Kale (капуста)

### 5. Paddy-культуры (Rice/Taro)
```
Характеристики:
- isPaddyCrop = true
- Автополив если рядом вода
- Бонус скорости +25% у воды
```

**Примеры:**
- Rice (рис)
- Taro Root (таро)

### 6. Особые культуры

#### Fiber (волокно)
```
needsWatering = false
Растёт без полива
Только коса
```

#### Ancient Fruit (древний фрукт)
```
Сложная культура:
- Долгий рост (28 дней)
- Регров каждые 7 дней
- Любой сезон в теплице
```

---

## Качество урожая

### Уровни качества
```
0 - Normal (обычный)
1 - Silver (серебряный)
2 - Gold (золотой)
4 - Iridium (иридиевый)
```

### Факторы влияния

#### 1. Удобрения качества
```
Basic Fertilizer:   qualityLevel = 1
Quality Fertilizer: qualityLevel = 2
Deluxe Fertilizer:  qualityLevel = 3
```

#### 2. Уровень фермерства (Farming Level)
```
chanceForGold = 0.2 * (farmingLevel / 10.0) 
              + 0.2 * qualityLevel * ((farmingLevel + 2.0) / 12.0) 
              + 0.01

chanceForSilver = Math.min(0.75, chanceForGold * 2.0)
```

#### 3. Удача
```
dailyLuck: -0.1 .. +0.1
Влияет на все расчёты качества
```

### Расчёт качества (алгоритм SV)
```java
public int calculateQuality(int farmingLevel, int qualityLevel, float luck) {
    Random r = new Random();
    
    double chanceGold = 0.2 * (farmingLevel / 10.0)
                      + 0.2 * qualityLevel * ((farmingLevel + 2.0) / 12.0)
                      + 0.01;
    double chanceSilver = Math.min(0.75, chanceGold * 2.0);
    
    int quality = 0;
    
    // Проверка иридиевого (только Deluxe Fertilizer)
    if (qualityLevel >= 3 && r.nextDouble() < chanceGold / 2.0) {
        quality = 4;
    }
    // Проверка золотого
    else if (r.nextDouble() < chanceGold) {
        quality = 2;
    }
    // Проверка серебряного
    else if (r.nextDouble() < chanceSilver || qualityLevel >= 3) {
        quality = 1;
    }
    
    // Ограничения культуры (если есть min/max quality)
    quality = Math.max(harvestMinQuality, 
                       Math.min(harvestMaxQuality, quality));
    
    return quality;
}
```

### Примеры

#### Без удобрений, уровень 5
```
chanceGold = 0.2 * 0.5 + 0 + 0.01 = 0.11 (11%)
chanceSilver = 0.22 (22%)

Результат:
- 11% - Gold
- 11% - Silver (22% - 11%)
- 78% - Normal
```

#### Deluxe Fertilizer, уровень 10
```
qualityLevel = 3
chanceGold = 0.2 * 1.0 + 0.2 * 3 * 1.0 + 0.01 = 0.81 (81%)
chanceSilver = 0.75 (cap at 75%)

Результат:
- 40.5% - Iridium (0.81 / 2)
- 40.5% - Gold
- 19% - Silver (гарантированно при qualityLevel >= 3)
```

---

## Особые механики

### 1. Гибель от недостаточного полива
В оригинальной SV культуры **не погибают** от недостатка полива, а просто **не растут**.
```java
// Minecraft адаптация:
if (!watered && needsWatering) {
    return; // не прогрессируем фазу
}
```

### 2. Crow (вороны)
Оригинал: вороны могут уничтожить культуры.
```java
// SV Farm.cs
int numCrops = countCrops();
int potentialCrows = Math.min(4, numCrops / 16);

for (each crow) {
    if (!protectedByScarecrow(cropPos)) {
        dirt.destroyCrop();
    }
}
```

**Адаптация для Minecraft:**
- Опциональная механика
- Пугала (Scarecrow) защищают в радиусе
- Шанс появления ворон ~30% каждое утро
- 1 ворона на каждые 16 культур

### 3. Lightning (молния)
SV: молния может ударить по культуре и убить её.
```java
if (isLightningStrike(pos)) {
    crop.Kill();
    terrainFeature = null;
}
```

**Для Minecraft:** используем ванильные молнии.

### 4. Мёртвые культуры (Dead Crops)
Условия смерти:
- Вне сезона на улице
- Удар молнией
- Механическое повреждение (опционально)

Состояние:
```java
dead = true;
readyForHarvest = false;
// Текстура: коричневая увядшая версия
```

Удаление:
- Коса
- Мотыга
- Удар инструментом

### 5. Giant Crops (гигантские культуры)
**НЕ реализуем в первой версии** - слишком сложно.
Оригинал: 3x3 некоторых культур (капуста, тыква, дыня) могут объединиться в гигантскую.

---

## Интеграция с Minecraft

### Адаптации под Minecraft

#### 1. Блоки вместо TerrainFeatures
```
SV: Crop - часть HoeDirt (TerrainFeature)
MC: Crop - отдельный Block на BlockState грядки
```

#### 2. BlockState AGE
```
Свойство: AGE (0-7)
Управляет моделью/текстурой
Не влияет на логику, только визуал
```

#### 3. BlockEntity для состояния
```
Хранит:
- ID культуры
- Текущую фазу
- День в фазе
- Флаги (fullyGrown, dead, readyForHarvest)
```

#### 4. NBT сохранение
```
CropInstance сохраняется в NBT:
{
  "cropId": "stardew:tomato",
  "currentPhase": 3,
  "dayOfCurrentPhase": 1,
  "fullyGrown": false,
  "readyForHarvest": false,
  "phaseDaysAdjusted": [1,2,1,2,99999]
}
```

#### 5. Полив через Farmland
```
FarmlandBlockEntity:
- hydrated: boolean
- fertilizer: FertilizerType
- Влияет на рост культуры сверху
```

### Взаимодействия игрока

#### Посадка
```
1. Игрок ПКМ семенем по грядке
2. Проверка сезона
3. Создание CropBlock + CropInstance
4. Применение бонусов скорости
5. Установка начальной фазы (0)
```

#### Полив
```
1. Игрок ПКМ лейкой по грядке
2. FarmlandBlockEntity.hydrated = true
3. Визуальное обновление грядки
```

#### Сбор рукой
```
1. Игрок ПКМ по зрелой культуре
2. Проверка readyForHarvest
3. Расчёт урожая + качество
4. Дроп предметов
5. Регров или удаление блока
```

#### Сбор косой
```
1. Игрок бьёт культуру косой
2. Проверка harvestMethod == SCYTHE
3. Дроп предметов на землю
4. Удаление/регров
```

### Визуализация

#### Текстуры
```
Каждая культура: 8 стадий роста (AGE 0-7)
Текстуры:
- stardew:block/crop/tomato_age_0
- stardew:block/crop/tomato_age_1
- ...
- stardew:block/crop/tomato_age_7
```

#### Модели
```json
{
  "parent": "minecraft:block/cross",
  "textures": {
    "cross": "stardew:block/crop/tomato_age_7"
  }
}
```

#### Флип (случайное отражение)
```java
// При создании растения
boolean flip = random.nextBoolean();

// В рендеринге (если поддерживается)
if (flip) {
    // Отразить текстуру горизонтально
}
```

---

## Структура данных

### CropDefinition
```java
public class CropDefinition {
    // Идентификация
    public final ResourceLocation id;              // "stardew:tomato"
    public final ResourceLocation seedItemId;      // "stardew:tomato_seeds"
    public final ResourceLocation harvestItemId;   // "stardew:tomato"
    
    // Сезоны
    public final List<Season> seasons;             // [SUMMER]
    
    // Рост
    public final List<Integer> daysInPhase;        // [1,2,2,3,99999]
    public final int regrowDays;                   // 4 or -1
    
    // Сбор
    public final HarvestMethod harvestMethod;      // GRAB/SCYTHE
    public final int harvestMinStack;              // 1
    public final int harvestMaxStack;              // 1
    public final float harvestMaxIncreasePerLevel; // 0.0
    public final float extraHarvestChance;         // 0.05
    public final int harvestMinQuality;            // 0
    public final int harvestMaxQuality;            // 4
    
    // Флаги
    public final boolean isRaised;                 // trellis
    public final boolean isPaddyCrop;              // rice
    public final boolean needsWatering;            // true (false для fiber)
    
    // Визуал (опционально)
    public final ResourceLocation customTexture;   // null = default
}
```

### CropInstance
```java
public class CropInstance {
    // Ссылка на определение
    private ResourceLocation cropId;
    private CropDefinition cachedDef;
    
    // Состояние роста
    private int currentPhase;           // 0..(phaseDays.size-1)
    private int dayOfCurrentPhase;      // текущий день в фазе
    private List<Integer> phaseDaysAdjusted; // скопированное с учётом бонусов
    
    // Флаги
    private boolean fullyGrown;         // после первого сбора (для регрова)
    private boolean readyForHarvest;    // можно собирать
    private boolean dead;               // погибло
    
    // Визуал
    private boolean flip;               // случайное отражение
    
    // Методы
    public void growOneDay(boolean watered);
    public void harvest(Player player, ServerLevel level);
    public void kill();
    public int calculateVisualAge();
    public void applySpeedBonuses(FertilizerType fert, boolean isPaddy);
}
```

### FarmlandState
```java
public class FarmlandState {
    // Полив
    private boolean hydrated;           // полито ли
    
    // Удобрения
    private FertilizerType fertilizer;  // NONE/SPEED/QUALITY/RETAINING
    
    // Методы
    public float getSpeedBoost();
    public float getWaterRetentionChance();
    public int getQualityBoostLevel();
    public boolean isHydrated();
    public void setHydrated(boolean value);
    public void applyFertilizer(FertilizerType type);
}
```

---

## Примеры реализации

### Пример 1: Томат
```java
CropDefinition TOMATO = new CropDefinition(
    id("tomato"),
    id("tomato_seeds"),
    id("tomato"),
    List.of(Season.SUMMER),
    List.of(1, 2, 2, 3, 99999),     // 8 дней + финал
    4,                               // регров каждые 4 дня
    HarvestMethod.GRAB,
    1, 1, 0.0f,                      // 1 помидор
    0.05f,                           // 5% экстра
    0, 4,                            // любое качество
    false,                           // не трелис
    false,                           // не paddy
    true,                            // нужен полив
    null                             // стандартная текстура
);
```

### Пример 2: Рис (Paddy)
```java
CropDefinition RICE = new CropDefinition(
    id("rice"),
    id("rice_shoots"),
    id("unmilled_rice"),
    List.of(Season.SPRING),
    List.of(1, 2, 2, 3, 99999),     // 8 дней
    -1,                              // без регрова
    HarvestMethod.SCYTHE,
    1, 1, 0.0f,
    0.0f,
    0, 4,
    false,
    true,                            // PADDY: автополив + бонус у воды
    true,
    null
);
```

### Пример 3: Виноград (Trellis + Regrow)
```java
CropDefinition GRAPE = new CropDefinition(
    id("grape"),
    id("grape_starter"),
    id("grape"),
    List.of(Season.FALL),
    List.of(1, 2, 2, 3, 2, 99999),  // 10 дней
    3,                               // регров 3 дня
    HarvestMethod.GRAB,
    1, 1, 0.0f,
    0.0f,
    0, 4,
    true,                            // TRELLIS: блокирует проход
    false,
    true,
    null
);
```

---

## Флоу-диаграммы

### Посадка культуры
```
[Игрок ПКМ семенем] 
        ↓
[Проверка: грядка обработана?] → НЕТ → [Отмена]
        ↓ ДА
[Проверка: правильный сезон?] → НЕТ → [Сообщение игроку]
        ↓ ДА
[Создание CropBlock + CropInstance]
        ↓
[Применение бонусов скорости:]
  - Удобрения
  - Paddy у воды
  - (Профессия фермера)
        ↓
[Установка фазы 0, день 0]
        ↓
[Установка AGE = 0]
        ↓
[Визуальное отображение семени]
```

### Утренний рост
```
[MorningPass вызывает GrowthSystem]
        ↓
[Для каждой CropInstance:]
        ↓
[Проверка: сезон подходит?] → НЕТ → [Kill()]
        ↓ ДА
[Проверка: грядка полита?] → НЕТ (и нужен полив) → [Пропуск]
        ↓ ДА
[fullyGrown?] → ДА → [dayOfCurrentPhase--]
        ↓ НЕТ
[dayOfCurrentPhase++]
        ↓
[dayOfCurrentPhase >= phaseDays[currentPhase]?] → ДА → [currentPhase++, day=0]
        ↓ НЕТ
[Пропуск нулевых фаз]
        ↓
[Обновление readyForHarvest]
        ↓
[Расчёт AGE из currentPhase]
        ↓
[Обновление BlockState]
```

### Сбор урожая
```
[Игрок ПКМ по культуре]
        ↓
[readyForHarvest?] → НЕТ → [Нет действия]
        ↓ ДА
[Расчёт количества:]
  - random(min, max)
  - цикл extraHarvestChance
        ↓
[Расчёт качества:]
  - FarmingLevel
  - QualityFertilizer
  - DailyLuck
        ↓
[Создание ItemStack × count]
        ↓
[Дроп/выдача игроку]
        ↓
[regrowDays > 0?] → ДА → [Регров: fullyGrown=true, day=regrowDays]
        ↓ НЕТ
[Удаление CropBlock]
```

---

## Совместимость и расширяемость

### Регистрация кастомных культур
```java
// Мод может зарегистрировать свою культуру
CropRegistry.register(
    new ResourceLocation("mymod", "custom_crop"),
    customCropDefinition
);
```

### Datapack поддержка (будущее)
```json
{
  "type": "stardew:crop",
  "id": "mypack:super_tomato",
  "seed_item": "mypack:super_tomato_seeds",
  "harvest_item": "mypack:super_tomato",
  "seasons": ["summer", "fall"],
  "days_in_phase": [1, 1, 2, 2, 99999],
  "regrow_days": 3,
  "harvest_method": "grab",
  "harvest_min": 2,
  "harvest_max": 4,
  "extra_harvest_chance": 0.1,
  "is_raised": false,
  "is_paddy": false,
  "needs_watering": true
}
```

### События для модов
```java
// ForgeBus события
CropPlantedEvent
CropGrowthTickEvent
CropHarvestedEvent
CropDeadEvent
```

---

## Заключение

Эта система полностью воссоздаёт механику растений Stardew Valley, адаптированную под Minecraft:

### Ключевые особенности
1. **Фазовый рост** с точным подсчётом дней
2. **Удобрения** влияют на скорость и качество
3. **Регров** для повторяющихся культур
4. **Сезонность** с гибелью вне сезона
5. **Качество урожая** основано на формулах SV
6. **Paddy-культуры** с автополивом
7. **Трелис-культуры** блокируют проход
8. **Сбор косой** для зерновых

### Отличия от оригинала
- Нет гигантских культур (пока)
- Нет wild seeds (дикие семена)
- Упрощённая визуализация (без анимаций)
- Используем Minecraft блоки/стейты

### Дальнейшее развитие
- Giant Crops (3x3)
- Зимние семена (wild seeds)
- Профессии фермера
- JEI интеграция
- Datapack поддержка
- Анимации сбора/роста

---

**Дата создания документации:** 2025-10-28  
**Версия:** 1.0  
**Для мода:** StardewMC


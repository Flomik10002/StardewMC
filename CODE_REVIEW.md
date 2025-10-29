# Обзор кода StardewMC
## Системы: Календарь/Погода/Время и Растения/Почва

---

## 📅 Система календаря/погоды/времени

### Общая архитектура

Система времени реализована через **event-driven подход** с использованием Forge event bus. Основные компоненты разделены на:
- **Хранение данных** (`StardewDateData`)
- **Утилиты времени** (`StardewTimeUtils`)
- **Компонент тиков** (`StardewClock`)
- **Система погоды** (`WeatherSystem`)
- **Планировщик событий** (`ScheduleManager`, `ScheduleEntry`)
- **Вспомогательные enum** (`Season`, `DayOfWeek`, `Weather`)

---

### Класс: `StardewDateData`

**Назначение:** СамаядедData контейнер, сохраняющий состояние даты/погоды игрового мира в NBT.

**Критические проблемы:**
1. **❌ Проблема дублирования состояния**: В классе хранятся `season`, `day`, `totalDays`, которые могут рассинхронизироваться. Метод `syncTotalDaysFromDate()` вызывается не всегда при изменении даты, что может привести к багам.
2. **⚠️ Не атомарное обновление**: Метод `advance()` читает `tomorrowWeather` в `todayWeather`, затем вызывается `WeatherSystem.generateTomorrow()` внешне — это race condition. Нужно делать в правильном порядке: сначала генерировать новую погоду на завтра, затем делать advance.
3. **⚠️ Сохранение/загрузка:** Используется старый подход `load()` как статический метод + конструктор. Более идиоматично было бы использовать только `load()` как фабрику из `CompoundTag`.

**Положительные моменты:**
- ✅ Использование `SavedData` для персистентности — правильный подход
- ✅ Правильное использование `setDirty()` для триггера сохранения
- ✅ Хранение погоды на "сегодня" и "завтра" разумно для логики дождя

**Рекомендации:**
```java
// Переписать метод advance() так:
public void advance() {
    // СНАЧАЛА генерируем погоду на новый день
    // (это должно быть внутри или до вызова advance)
    totalDays++;
    day++;
    
    if (day > 28) {
        day = 1;
        season = season.next();
    }
    
    // Promote tomorrow -> today ONLY if tomorrowWeather уже установлен
    if (tomorrowWeather != null) {
        this.todayWeather = this.tomorrowWeather;
        this.festivalToday = this.festivalTomorrow;
    }
    
    setDirty();
}
```

---

### Класс: `StardewTimeUtils`

**Назначение:** Утилитный класс для конвертации Minecraft ticks в человеко-читаемое время.

**Критические проблемы:**
1. **❌ Хардкод констант:** `TICKS_PER_MINUTE = 17`, `MINUTES_IN_DAY = 1200` — эти значения должны быть либо в конфиге, либо хотя бы задокументированы, почему именно такие.
2. **⚠️ Magic number 24000:** В коде используется `ticks % 24000` для получения времени дня — это внутренняя константа Minecraft. Нужен комментарий, что 24000 ticks = 1 полный игровой день.
3. **⚠️ Разные подходы к расчету времени:** В `formatTicksToClock()` и `getHour()` используются разные логики для расчета часа. Это может привести к различиям в одну минуту в некоторых случаях.

**Положительные моменты:**
- ✅ Логика работы с "временем с 6 утра" соответствует оригинальному Stardew Valley
- ✅ Метод `shouldPassOut()` корректно определяет момент "засыпания"
- ✅ Метод `toTicks()` позволяет конвертировать обратно

**Рекомендации:**
Добавить документацию для методов:
```java
/**
 * Конвертирует Minecraft ticks в строку формата HH:MM.
 * Начало дня — 6:00 (2400 ticks).
 * Один игровой день Stardew = 1200 минут (6:00 → 2:00 следующего дня).
 * 
 * @param ticks Minecraft ticks (24000 = полный день)
 * @return Строка формата "HH:MM"
 */
public static String formatTicksToClock(long ticks) { ... }
```

---

### Класс: `WeatherSystem`

**Назначение:** Генерация погоды на следующий день на основе детерминированного PRNG и сезона.

**Критические проблемы:**
1. **❌ Нарушение детерминизма:** Используется `Random` класс с seed, что **не потокобезопасно**. Если `generateTomorrow()` вызывается параллельно или несколько раз за тик, результаты могут отличаться. Нужно либо `ThreadLocalRandom`, либо `new Random(seed)` только для вычисления, но не хранить экземпляр.
2. **⚠️ Hardcoded вероятности:** Все вероятности погоды захардкожены в коде. Для тонкой настройки нужно вынести в конфиг.
3. **⚠️ Проблема с зимой:** В случае `WINTER` всегда возвращается `SNOW`, но в оригинале SV бывают солнечные дни. Это делает зиму однообразной.
4. **❌ Много дублирования кода:** В трех блоках для SPRING/SUMMER/FALL почти одинаковая логика — можно рефакторить.

**Положительные моменты:**
- ✅ Использование детерминированного seed на основе `worldSeed ^ (dayIndex * magic)` — правильный подход
- ✅ Раздельный seed для погоды и удачи — хорошо
- ✅ Обработка `festivalToday` через override погоды — логично

**Рекомендации:**
```java
// Рефакторинг для избежания дублирования:
private static Weather pickWeatherForTomorrow(ServerLevel level, StardewDateData data) {
    long seed = level.getSeed() ^ (data.getTotalDays() + 1L) * 0x9E3779B97F4A7C15L;
    
    // Конфиг вместо hardcoded значений
    WeatherChance chances = SEASON_CHANCES.getOrDefault(data.getSeason(), DEFAULT);
    return chances.roll(seed);
}

private static class WeatherChance {
    final float sunny, rain, storm, wind;
    Weather roll(long seed) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        // ... логика
    }
}
```

---

### Класс: `StardewClock`

**Назначение:** Event handler для Forge event bus, который обрабатывает каждый тик уровня и проверяет, нужно ли "отключать" игрока.

**Критические проблемы:**
1. **❌ Повторяющийся код:** `lastHour` хранится И в `StardewClock`, И в `ScheduleManager`. Это дублирование логики.
2. **❌ Неэффективность:** Проверка `shouldPassOut()` происходит **каждый тик**, хотя достаточно проверять раз в минуту или при смене часа. Для 20 тиков в секунду это избыточно.
3. **⚠️ Hardcoded сообщения:** Сообщение "Вы потеряли сознание от усталости..." — должно быть локализуемо.

**Положительные моменты:**
- ✅ Правильная проверка `event.phase != TickEvent.Phase.END` — избегает двойной обработки
- ✅ Проверка `isClient()` и `LogicalSide.SERVER` предотвращает запуск на клиенте

**Рекомендации:**
Оптимизировать проверку пассаута:
```java
// Проверять только раз в секунду
private static int lastPassOutCheck = -1;

if (hour != lastPassOutCheck && shouldPassOut(ticks)) {
    // ... телепорт
    lastPassOutCheck = hour;
}
```

---

### Класс: `ScheduleManager`

**Назначение:** Система запланированных событий на время суток (например, обновление культур в 6:00).

**Критические проблемы:**
1. **❌ Глобальная мутабельная коллекция:** `private static final List<ScheduleEntry> entries` — **ПРОБЛЕМА для многопоточности**. Если несколько игроков используют команды для добавления событий одновременно — race condition.
2. **❌ Memory leak:** `List<ScheduleEntry>` хранится статически навсегда, даже если уровень был выгружен (например, WorldSave unload). Это приводит к утечкам памяти в долгих сессиях.
3. **⚠️ Сброс при 6:00:** Логика `if (hour == 6 && minute == 0 && (lastHour != 6 || lastMinute != 0))` — хрупкая, может не сработать если тик пропущен (например, сервер был остановлен).
4. **⚠️ Проблема порядка:** Метод `forceNextDay()` триггерит события в 6:00 **до** вызова `date.advance()`. Это может привести к тому, что события получат "вчерашнюю" дату.

**Положительные моменты:**
- ✅ Концепция `ScheduleEntry` с `startTick`/`endTick` и `triggered` флагом — хорошая архитектура
- ✅ Реализация через `Runnable` позволяет гибко определять действие

**Рекомендации:**
Использовать per-level хранение:
```java
public class ScheduleManager {
    // Вместо статического списка — Map<ServerLevel, List<ScheduleEntry>>
    private static final Map<ServerLevel, List<ScheduleEntry>> PER_LEVEL_ENTRIES = new WeakHashMap<>();
    
    public static void register(ServerLevel level, int hour, int minute, Runnable action) {
        PER_LEVEL_ENTRIES.computeIfAbsent(level, k -> new ArrayList<>()).add(new ScheduleEntry(hour, minute, action));
    }
}
```

---

### Класс: `ScheduleEntry`

**Назначение:** Контейнер для одного запланированного события.

**Проблемы:**
1. **⚠️ Несогласованность:** Поле `triggered` сбрасывается через `reset()` каждые сутки в 6:00, но если событие было триггернуто в 23:00 вчера, а сегодня сервер перезапустился в 7:00 — оно не сработает. Нужна более надежная логика.
2. **⚠️ Отсутствие валидации:** Нет проверки, что `startTick < endTick` при создании с диапазоном. Можно создать некорректное событие с `start=100, end=50`.

---

### Enum: `Season`, `DayOfWeek`, `Weather`

**Базовые структуры данных.**

**Проблемы:**
1. **⚠️ Season.next():** Использует `ordinal()`, что хрупко при изменении порядка enum. Если добавить новый сезон — ломается. Лучше явный `switch`.
2. **⚠️ Weather:** Отсутствуют значения для специфических погод (например, `SUN_SHOWER` из модов). Хорошо бы сделать систему расширяемой.
3. **⚠️ Отсутствует документация:** Не объяснено, что делает `FEST_OVERRIDE` vs `WEDDING_OVERRIDE`.

**Рекомендации:**
```java
public enum Season {
    SPRING, SUMMER, FALL, WINTER;
    
    private static final Season[] VALUES = values();
    
    public Season next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }
}
```

---

## 🌱 Система растений/почвы

### Общая архитектура

Система растений реализована через:
- **Блок культуры** (`BlockCrop`) + **BlockEntity** (`CropBlockEntity`)
- **Определения культур** (`CropDef`) + **Реестр** (`CropRegistry`)
- **Систему удобрений** (`FertilizerType`)
- **Блок почвы** (`BlockFarmland`) + **BlockEntity** (`FarmlandBlockEntity`)
- **Логику роста** (`GrowthSystem`), **логику сбора** (`HarvestHelper`), **утреннюю логику** (`MorningPass`)
- **Трекеры** (`CropTracker`, `FarmlandTracker`)

---

### Класс: `CropDef`

**Назначение:** Immutable data class для определения одной культуры.

**Критические проблемы:**
1. **❌ `List<String> seasons`:** Хранит строки вместо `List<Season>`. Это приводит к:
   - Опечаткам в регистре ("spring" vs "SPRING")
   - Необходимости сравнения через `.toLowerCase()` в `MorningPass`
   - Отсутствию типобезопасности
2. **⚠️ Отсутствие валидации:** Конструктор принимает любые значения. Можно создать культуру с `daysInPhase = []` или `harvestMin > harvestMax`.
3. **⚠️ Много параметров:** 14 параметров в конструкторе — это "code smell" для God Object. Возможно, стоит разделить на: `GrowthProperties`, `HarvestProperties`, `SpecialProperties`.

**Положительные моменты:**
- ✅ Immutable класс — безопасен для многопоточности
- ✅ Понятная структура полей

**Рекомендации:**
```java
public final class CropDef {
    public final ResourceLocation id;
    public final List<Season> seasons;  // <-- вместо String
    // ... остальные поля
    
    // Добавить валидацию в конструктор
    public CropDef(...) {
        if (daysInPhase.isEmpty()) throw new IllegalArgumentException("...");
        if (harvestMin > harvestMax) throw new IllegalArgumentException("...");
        // ...
    }
}
```

---

### Класс: `CropRegistry`

**Назначение:** Центральный реестр всех культур в игре.

**Критические проблемы:**
1. **❌ Hardcoded данные:** Все культуры (`bootstrapVanillaLike()`) захардкожены в коде. Для расширяемости нужен datapack-подход или JSON-конфиги.
2. **❌ Отсутствие уникальности:** `HashMap` позволяет перезаписать существующую культуру без предупреждения. Нужна проверка на дубликаты.
3. **⚠️ No thread safety:** `HashMap` не thread-safe. Если культура добавляется динамически — возможна race condition.

**Положительные моменты:**
- ✅ Использование `ResourceLocation` для ID — стандарт Minecraft
- ✅ Два индекса (`BY_ID`, `SEED_TO_CROP`) для быстрого поиска
- ✅ `Optional` в `bySeed()` — хороший null-safety подход

**Рекомендации:**
```java
public static void register(ResourceLocation cropId, CropDef def, ResourceLocation seedItemId) {
    if (BY_ID.containsKey(cropId)) {
        throw new IllegalArgumentException("Crop already registered: " + cropId);
    }
    BY_ID.put(cropId, def);
    SEED_TO_CROP.put(seedItemId, cropId);
}
```

---

### Класс: `CropBlockEntity`

**Назначение:** BlockEntity культуры, хранящий фазу роста, готовность к сбору, и т.д.

**Критические проблемы:**
1. **❌ Дублирование состояния:** Хранятся И `fullyGrown`, И `readyToHarvest`, И `dayOfCurrentPhase` для regrow. Логика запутанная: regrow использует `dayOfCurrentPhase` (отсчет вниз), а основной рост — вверх. **Это очень сложно поддерживать**.
2. **❌ Гонка данных:** `phaseDaysAdjusted` применяется через `applyPlantingSpeedBonuses()` при посадке, но также может быть пустым и копироваться из `def.daysInPhase` в нескольких местах. Нет четкого контракта, когда и как массив должен быть заполнен.
3. **⚠️ Метод `growOneDay()` слишком сложный:** 70 строк логики с вложенными условиями. Это признак, что нужно разбить на методы: `growPhase()`, `handleRegrow()`, `checkReady()`.
4. **⚠️ Хрупкая синхронизация:** В методе `growOneDay()` есть проверка `if (phaseDaysAdjusted.isEmpty())` — но это может быть false из-за того, что массив копировался ранее. Нет гарантии, что массив всегда актуален.
5. **❌ Переиспользование `dayOfCurrentPhase`:** Это поле используется для СОВЕРШЕННО разных целей:
   - В основном росте: "сколько дней прошло в текущей фазе"
   - В regrow: "сколько дней осталось до регроза"
   Это ОПАСНО. Нужны отдельные поля: `daysInCurrentPhase` и `regrowCountdown`.

**Положительные моменты:**
- ✅ Правильное использование NBT для сериализации
- ✅ Отслеживание всех культур через `CropTracker` — хорошо

**Рекомендации:**
Рефакторинг структуры данных:
```java
public class CropBlockEntity {
    private ResourceLocation cropId;
    private int currentPhase;  // 0..(phases-1)
    private int daysInCurrentPhase; // дни в текущей фазе (вверх)
    private boolean readyToHarvest;
    
    // Regrow состояние (отдельно!)
    private boolean isRegrowing;
    private int regrowCountdown; // дни до регроза (вниз)
    
    private List<Integer> adjustedPhaseDays; // фиксируется при посадке
    
    public void growOneDay(boolean watered) {
        if (readyToHarvest && !isRegrowing) return;
        
        var d = def();
        if (d == null) return;
        if (d.needsWatering && !watered) return;
        
        if (isRegrowing) {
            regrowCountdown = Math.max(0, regrowCountdown - 1);
            if (regrowCountdown <= 0) {
                readyToHarvest = true;
                isRegrowing = false;
            }
        } else {
            // основной рост
            int phaseLen = adjustedPhaseDays.get(Math.min(currentPhase, adjustedPhaseDays.size() - 1));
            daysInCurrentPhase++;
            
            if (daysInCurrentPhase >= phaseLen && currentPhase < adjustedPhaseDays.size() - 1) {
                currentPhase++;
                daysInCurrentPhase = 0;
            }
            
            if (currentPhase >= adjustedPhaseDays.size() - 1) {
                readyToHarvest = true;
            }
        }
        setChanged();
    }
}
```

Также метод `applyPlantingSpeedBonuses()` — **50 строк** с вложенными циклами. Нужен рефакторинг или хотя бы комментарии.

---

### Класс: `BlockCrop`

**Назначение:** Block-обертка для BlockEntity культуры.

**Проблемы:**
1. **⚠️ Визуальный возраст:** Поле `AGE` (0..7) используется для визуального отображения, но маппится через формулу `age = Math.round((cropBe.currentPhase() * 7f) / max)` в `GrowthSystem`. Эта формула **НЕ обратная**: нельзя восстановить точную фазу из AGE. Это может привести к багам в UI.
2. **⚠️ Отсутствует harvest для SCYTHE:** В `use()` только GRAB метод. Нет обработки коса-сборки (например, через правый клик с косой).
3. **⚠️ Проверка `canSurvive()`:** Проверяет только наличие farmland снизу, но не проверяет сезонность. Культура может существовать вне сезона до утренней проверки.

---

### Класс: `GrowthSystem`

**Назначение:** Система, вызываемая ежедневно для роста всех культур.

**Критические проблемы:**
1. **❌ Избыточные параметры:** В `growOneDay(boolean watered, boolean applySpeed, float speedMul, boolean paddy)` — параметры `applySpeed` и `speedMul` **не используются внутри метода** (т.к. скорость применяется при посадке). Это мертвый код.
2. **⚠️ Неэффективность:** Проходится по ВСЕМ культурам каждый день через `CropTracker.all()`. Если культур тысячи — это O(n) каждый день. Нет оптимизации для "бездействующих" культур (например, вне сезона и просто ждут удаления).
3. **⚠️ Обновление AGE:** Логика обновления `BlockState` для AGE находится здесь, а не в `CropBlockEntity`. Это нарушает инкапсуляцию — "система роста" не должна управлять визуалом.

**Положительные моменты:**
- ✅ Правильная проверка на существование BlockEntity перед обработкой
- ✅ Получение данных из FarmlandBlockEntity для проверки hydration

---

### Класс: `HarvestHelper`

**Назначение:** Логика сбора урожая с учетом качества и удобрений.

**Критические проблемы:**
1. **❌ Статический Random:** `private static final Random RNG = new Random()` — **не thread-safe**. Если несколько игроков собирают урожай одновременно — возможны race conditions.
2. **⚠️ Качество не используется:** Метод `computeQuality()` вычисляет качество (0..3), но **не применяет его к ItemStack**. В комментарии сказано "можно позже добавить", но пока это мертвый код.
3. **⚠️ Hardcoded алгоритм качества:** Логика `computeQuality()` — приблизительная эвристика. В оригинале SV алгоритм более сложный (зависит от полива каждый день, от типа удобрения, от дневной удачи). Текущая реализация упрощенная.
4. **⚠️ Дублирование импорта:** `import dev.flomik.stardew.core.registry.blockentity.FarmlandBlockEntity;` импортируется дважды.

**Положительные моменты:**
- ✅ Правильное использование геометрического распределения для extra harvest (`while (RNG.nextFloat() < extra)`)
- ✅ Проверка на существование crop definition перед обработкой

**Рекомендации:**
```java
// Использовать ThreadLocalRandom
private static int computeQuality(ServerLevel level, CropBlockEntity cropBe) {
    ThreadLocalRandom rng = ThreadLocalRandom.current();
    // ... остальная логика
}

// Применить качество к ItemStack
ItemStack drop = new ItemStack(item, count);
if (quality > 0) {
    CompoundTag qualityTag = new CompoundTag();
    qualityTag.putInt("quality", quality);
    drop.addTagElement("StardewQuality", qualityTag);
}
```

---

### Класс: `MorningPass`

**Назначение:** Логика, выполняющаяся каждое утро: обезвоживание, орошение от дождя, удаление культур вне сезона.

**Критические проблемы:**
1. **❌ Дублирование данных:** Получает `Weather w = date.getTodayWeather()` в начале, но также принимает параметр `isRaining`. При этом в строке 27 есть `|| isRaining` — **параметр игнорирует данные из date**. Это может привести к десинхронизации.
2. **⚠️ Отсутствие проверки на Greenhouse:** Комментарий в строке 46 "у тебя пока нет теплицы — считаем весь мир уличным" — это временное решение, которое нужно будет менять.
3. **⚠️ Inefficient:** Проходится по всем Farmland дважды (один раз для `dehydrate()`, другой для `hydrate()`). Можно объединить в один проход.

**Положительные моменты:**
- ✅ Правильная логика: сначала обезвоживание, потом проверка дождя — соответствует SV
- ✅ Использование retention soil через `fert.isRetention()` — хорошо

**Рекомендации:**
Оптимизировать циклы:
```java
for (BlockPos pos : FarmlandTracker.all(level)) {
    var be = level.getBlockEntity(pos);
    if (be instanceof FarmlandBlockEntity fb) {
        fb.dehydrate();  // сначала обезвоживаем
        
        if (w == Weather.RAIN || w == Weather.STORM || isRaining) {
            fb.hydrate();  // затем проверяем дождь
        } else {
            // retention логика
        }
    }
}
```

---

### Класс: `FertilizerType`

**Назначение:** Enum для типов удобрений с их свойствами.

**Проблемы:**
1. **⚠️ Дублирование логики:** Методы `isRetention()`, `isSpeedBoost()`, `isQualityBoost()`, `isTreeOnly()`, `isNone()` проверяют `effect == FertilizerEffect.???`. Это можно автоматизировать, если добавить метод `public boolean hasEffect(FertilizerEffect e)`.
2. **⚠️ Много enum values:** 12 значений в одном enum — много. Возможно, стоит разделить на подклассы или использовать паттерн Strategy.

**Положительные моменты:**
- ✅ Четкая структура полей: `canApplyBeforePlanting`, `canApplyAfterPlanting`, `effect`, `strength`
- ✅ Использование параметра `strength` для числовых эффектов

---

### Класс: `BlockFarmland`

**Назначение:** Block для вспаханной земли с визуальными состояниями (shape, wet_shape).

**Критические проблемы:**
1. **❌ Сложная логика shapes:** Методы `calculateShape()` и `calculateWetShape()` — **ТОЧНО ОДИНАКОВЫЙ КОД** (28 строк каждый). Это нарушение DRY. Нужен общий метод с параметром `Predicate<BlockState>`.
2. **⚠️ Неэффективность `updateShape()`:** Вызывается **каждый раз** при изменении соседнего блока (в т.ч. при загрузке чанков). Это может быть дорого для больших ферм.
3. **⚠️ Отсутствие кэша:** `calculateShape()` выполняется каждый раз заново. Можно кэшировать результат, если соседи не изменились.

**Положительные моменты:**
- ✅ Использование `EnumProperty` для фертилизатора — типобезопасно
- ✅ Правильная регистрация BlockEntity

**Рекомендации:**
Рефакторинг дублирования:
```java
private Shape calculateShapeInternal(LevelAccessor level, BlockPos pos, Predicate<BlockState> predicate) {
    boolean up    = predicate.test(level.getBlockState(pos.north()));
    boolean down  = predicate.test(level.getBlockState(pos.south()));
    boolean left  = predicate.test(level.getBlockState(pos.west()));
    boolean right = predicate.test(level.getBlockState(pos.east()));
    
    // ... остальная логика
}

private Shape calculateShape(LevelAccessor level, BlockPos pos) {
    return calculateShapeInternal(level, pos, this::isSameFarmland);
}

private Shape calculateWetShape(LevelAccessor level, BlockPos pos) {
    return calculateShapeInternal(level, pos, this::isHydratedFarmland);
}
```

---

### Класс: `FarmlandBlockEntity`

**Назначение:** BlockEntity для хранения состояния почвы: увлажненность и удобрение.

**Проблемы:**
1. **⚠️ Дублирование состояния:** `hydrated` хранится И в BlockState (`HYDRATED`), И в BlockEntity. Это может привести к рассинхронизации если одно из них изменится без обновления другого.
2. **⚠️ Отсутствие проверки на валидность:** Метод `applyFertilizer()` не проверяет, можно ли применять данное удобрение после посадки (используется флаг `canApplyAfterPlanting`).
3. **⚠️ NPE риски:** В методе `hydrate()` и `dehydrate()` есть проверка `if (level != null && !level.isClientSide)`, но если уровень null — состояние в BE не обновится, но в BlockState может обновиться через другую логику.

**Положительные моменты:**
- ✅ Правильная регистрация в трекер через `onLoad()`/`setRemoved()`
- ✅ Использование `setChanged()` для триггера сохранения

---

### Класс: `CropTracker` и `FarmlandTracker`

**Назначение:** Трекеры позиций всех культур/почвы для быстрого доступа.

**Критические проблемы:**
1. **❌ Непотокобезопасность:** `Map<ServerLevel, Set<BlockPos>> MAP` использует `HashMap` и `HashSet`, которые **не thread-safe**. Если BlockEntity загружается/выгружается из разных потоков — возможны ConcurrentModificationException.
2. **⚠️ Утечки памяти:** Используется `WeakHashMap` для `ServerLevel`, но не для внутренних `Set<BlockPos>`. Если чанк выгружается, но позиции остаются в Set — возможна утечка памяти.
3. **⚠️ Отсутствие валидации:** Нет проверки, что BlockEntity действительно соответствует типу перед добавлением в трекер. Можно случайно добавить `CropBlockEntity` в `FarmlandTracker`.

**Положительные моменты:**
- ✅ Использование `WeakHashMap` для автоматической очистки при выгрузке уровня
- ✅ Immutable позиции через `.immutable()`

**Рекомендации:**
Добавить потокобезопасность:
```java
public final class CropTracker {
    private static final Map<ServerLevel, Set<BlockPos>> MAP = new ConcurrentHashMap<>();
    
    public static void onLoad(CropBlockEntity be) {
        if (be.getLevel() instanceof ServerLevel sl) {
            MAP.computeIfAbsent(sl, k -> ConcurrentHashMap.newKeySet()).add(be.getBlockPos().immutable());
        }
    }
}
```

---

## 📊 Общие выводы

### Критические проблемы архитектуры:

1. **Thread safety:** Почти везде отсутствует синхронизация для многопоточного доступа. При одновременной сборке урожая несколькими игроками возможны баги.

2. **Дублирование состояния:** 
   - `StardewDateData`: `season`, `day`, `totalDays`
   - `FarmlandBlockEntity`: `hydrated` в BE и в BlockState
   - `CropBlockEntity`: `dayOfCurrentPhase` используется для двух разных целей

3. **Memory leaks:**
   - `ScheduleManager`: статический список никогда не очищается
   - `CropTracker`/`FarmlandTracker`: возможны утечки при выгрузке чанков

4. **Хардкод данных:**
   - Все культуры захардкожены в `CropRegistry`
   - Вероятности погоды захардкожены в `WeatherSystem`

5. **Сложность поддержки:**
   - `CropBlockEntity.growOneDay()` — 70 строк запутанной логики
   - `BlockFarmland.calculateShape()` — дублирование кода
   - Отсутствие документации для большинства методов

### Рекомендации для улучшения:

1. **Рефакторинг CropBlockEntity:** Разделить на отдельные методы для основного роста и regrow.

2. **Добавить валидацию данных:** Проверять корректность значений в конструкторах `CropDef`, проверять дубликаты в реестре.

3. **Вынести конфиги:** Все hardcoded данные (культуры, вероятности погоды) в JSON/datapack.

4. **Добавить тесты:** Особенно для `StardewTimeUtils`, `WeatherSystem`, `CropBlockEntity.growOneDay()`.

5. **Документация:** Добавить JavaDoc для всех публичных методов, особенно для тех, где много параметров.

6. **Оптимизации:**
   - Кэшировать вычисления в `BlockFarmland.calculateShape()`
   - Проверять пассаут только раз в минуту в `StardewClock`
   - Использовать `ConcurrentHashMap` в трекерах

7. **Типобезопасность:** Заменить `List<String> seasons` на `List<Season>` в `CropDef`.

---

## Заключение

Код в целом **функциональный и рабочий**, но имеет **архитектурные проблемы**, которые усложняют поддержку и масштабирование. Основные проблемы:
- Непотокобезопасность
- Дублирование данных
- Отсутствие валидации
- Hardcoded данные
- Сложная логика в больших методах

Для production-ready мода нужно проделать работу по рефакторингу и добавлению потокобезопасности.


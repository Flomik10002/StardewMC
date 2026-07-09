# Шаблон мира: "Play Stardew Valley"

Дизайн-документ для запуска встроенного заранее подготовленного мира (`map/SV`, примерно 150 МБ) как стартовой точки в один клик, после чего игрок проходит внутриигровой поток создания персонажа. Ничего из описанного здесь пока не реализовано - это план с конкретными набросками кода, которые соответствуют текущим соглашениям проекта (`PlayerStardewState` capability, паттерн пакетов `PacketHandler`/`S2C*`, дерево команд `ModCommands`).

## 1. Что нам реально нужно

- Заранее подготовленный save (`map/SV`), где ферма, здания, декор и прочее уже расставлены.
- Способ для игрока сказать "начать новую игру отсюда" и получить **свежую копию** этого save как свой собственный мир, а не общий template. Каждая новая игра должна получать отдельную независимую save directory.
- Сразу после первого spawn в своей копии - **экран создания персонажа**: имя, название фермы, внешность. Это повторяет new-game flow Stardew Valley перед началом обычной игры.

## 2. Почему это не "world preset" в ванильном смысле

Система `CreateWorldScreen` "World Type" / preset в Minecraft (`WorldPreset` datapack entries, `WorldGenSettings`, кнопка "Import Settings" в `CreateWorldScreen.createTempDataPackDirFromExistingWorld`) отвечает за выбор **правил генерации**: superflat layers, biome overrides, structure toggles. Мир при этом всё равно генерируется заново. У этой системы нет понятия "скопируй конкретные уже построенные chunks/region files из существующего save". Это feature "world template" из Bedrock Edition, а не Java Edition.

Попытка втиснуть клонирование save в `CreateWorldScreen` означает борьбу с экраном, чья data model (`WorldCreationContext`, `WorldGenSettings`) предполагает "мы сейчас будем генерировать", а не "terrain уже есть". Это неправильный инструмент. Скопировать directory tree и открыть её как мир проще, и Minecraft уже делает именно это внутри при дублировании мира. Кнопки `Recreate` / `Copy` в списке миров используют тот же copy mechanism `LevelStorageSource`, который нужен нам.

**Выбранный подход:** вообще не трогать `CreateWorldScreen`. Добавить собственную точку входа - кнопку "Play Stardew Valley", которая копирует bundled template в новый save slot и открывает его напрямую, полностью пропуская генерацию мира. Генерировать нечего: terrain уже существует.

## 3. Доставка template на 150 МБ

`map/` сейчас находится в `.gitignore` (см. root репозитория), потому что 150 МБ не должны попадать в обычный git diff. Варианты:

| Вариант | Плюсы | Минусы |
|---|---|---|
| **A. Только для dev, вручную** (текущее состояние): contributors сами кладут `map/SV` в `run/saves/` | Нулевая build-работа | Нельзя нормально отдать игрокам |
| **B. GitHub Release asset** (обычный файл-приложение к релизу, БЕЗ LFS), скачиваемый Gradle task'ом в `run/saves/` для dev и самим модом при первом запуске для игроков | Репозиторий остаётся маленьким, реальный путь распространения, **бесплатно без ограничений по трафику** для публичного репо | Нужен небольшой downloader + checksum, нужен сам release |
| **C. Git LFS** для `map/` | Мысленно просто, остаётся "в" репозитории | **Платно при реальном использовании**: бесплатный тир GitHub LFS — 1 ГБ storage + 1 ГБ traffic/месяц. При 150 МБ на файл это ~6-7 скачиваний в месяц, и всё, лимит исчерпан. Для мода, который реально ставят игроки, это не вариант без платной подписки. |
| **D. Вшить в mod jar** (`src/main/resources` под assets-like path) | Нет runtime-зависимости от сети | Раздувает distributed jar для *каждой* установки, даже если template не используется; большинство mod hosts (CurseForge/Modrinth) не любят огромные jars |

**Важно: Release asset (B) и Git LFS (C) — это НЕ одно и то же по деньгам.** Обычные файлы, приложенные к GitHub Release (Releases → Assets), раздаются через тот же CDN, что и сами репозитории/архивы кода, и не считаются против LFS-квоты вообще — это просто статический файл, бесплатный без объявленного лимита трафика для публичных репо. Git LFS — отдельная платная-по-факту система хранения больших файлов *внутри* истории git с собственной, гораздо более скромной бесплатной квотой. **Рекомендация — B, и именно НЕ через LFS**: заливаем `SV.zip` как asset к GitHub Release, `map/` в репозитории не участвует вообще ни в каком виде.

**Кэш на стороне игрока.** Скачивается шаблон один раз и кладётся в кэш где-то под `.minecraft` (например `.minecraft/stardewmc/templates/<templateId>/`, распакованный, plus рядом файл с версией/checksum). При каждом новом клике "Play Stardew Valley" мы **копируем** из этого кэша в новый save-слот (см. §4) — сам кэш не трогаем и не расходуем повторным скачиванием. Повторное скачивание — только если кэша нет или checksum не совпал (шаблон обновился).

**Задел на несколько ферм на будущее.** Раз позже наверняка захочется несколько стартовых карт (по аналогии с выбором типа фермы в оригинале: Standard, Riverland, Forest, Hills, Wilderness...), с самого начала проектируем не "один захардкоженный SV.zip", а небольшой **каталог шаблонов** — манифест вида:

```json
{
  "templates": [
    { "id": "standard",  "displayName": "Standard Farm",  "url": "https://github.com/.../releases/download/templates-v1/standard.zip",  "sha256": "..." },
    { "id": "riverland", "displayName": "Riverland Farm", "url": "https://github.com/.../releases/download/templates-v1/riverland.zip", "sha256": "..." }
  ]
}
```

Манифест — тоже просто release asset (маленький json, обновляется отдельно от самих zip'ов). Кэш на клиенте раскладывается по `templateId` (`.minecraft/stardewmc/templates/standard/`, `.../riverland/`), и кнопка "Play Stardew Valley" в итоге превращается в небольшой список карт на выбор вместо одной фиксированной. Ничего в §4/§5 ниже от этого не меняется, просто `templateId` — параметр, а не константа.

Этот документ предполагает, что template в итоге лежит там, откуда Minecraft может прочитать folder, совместимую с `LevelStorageSource.LevelStorageAccess`. Неважно, это game-dir cache folder или `run/saves/_template` в dev. Ниже этот путь называется `TEMPLATE_SOURCE`.

## 4. Копирование template в новый save

World save - это просто directory (`level.dat`, `region/`, `entities/`, `data/`, `datapacks/` - ровно то, что остаётся в `map/SV` после cleanup). Клонирование - это recursive directory copy в новую уникально названную folder внутри `saves/` игрока, затем передача этой folder обычному flow "загрузить этот мир" (`Minecraft.createWorldOpenFlows().loadLevel(...)`). Это то же самое, что double-click по существующему save в списке миров, без `CreateWorldScreen` на любом этапе.

```java
package dev.flomik.stardew.client.worldtemplate;

import net.minecraft.client.Minecraft;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Копирует выбранный шаблон фермы в новый, независимый save-слот игрока и
 * открывает его напрямую — без экрана создания мира, генерировать нечего.
 * templateId соответствует записи в каталоге шаблонов (см. §3) — сейчас это
 * будет только "standard" (= map/SV), но метод уже не завязан на один
 * захардкоженный путь.
 */
public final class WorldTemplateManager {

    private WorldTemplateManager() {
    }

    /**
     * @param templateId  id из каталога шаблонов (см. §3), например "standard"
     * @param newSaveName имя новой папки под saves/ (уже проверено на
     *                    уникальность вызывающим кодом)
     */
    public static void createFromTemplate(String templateId, String newSaveName) throws IOException {
        Path templateSource = TemplateCache.getOrDownload(templateId); // §3: кэш под .minecraft/stardewmc/templates/<id>/
        LevelStorageSource storageSource = Minecraft.getInstance().getLevelSource();
        Path targetDir = storageSource.getBaseDir().resolve(newSaveName);

        copyDirectory(templateSource, targetDir);

        // level.dat несёт "LevelName" — переписываем на имя новой папки,
        // иначе список миров покажет старое имя шаблона.
        renameLevelNameInLevelDat(targetDir.resolve("level.dat"), newSaveName);
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void renameLevelNameInLevelDat(Path levelDat, String newName) {
        // NBT read/modify/write строки "LevelName" внутри root compound
        // "Data" (net.minecraft.nbt.NbtIo.readCompressed +
        // CompoundTag#putString + NbtIo.writeCompressed). Здесь опущено:
        // это прямолинейные ~10 строк, см. NbtIo для точных compression flags,
        // ожидаемых этой версией Minecraft.
    }
}
```

Hooking кнопки: отдельная кнопка **на экране списка синглплеерных миров** (`WorldSelectionList`), рядом с существующей "Create New World" — именно там, а не на титульном экране, раз выбор "с кем играть" (singleplayer/LAN) там и так уже происходит. Пока шаблон один, кнопка называется "Play Stardew Valley" и сразу вызывает `WorldTemplateManager.createFromTemplate("standard", newSaveName)`, затем `Minecraft.getInstance().createWorldOpenFlows().openWorld(newSaveName, () -> {})` — тот же вызов, что делает vanilla-кнопка "Play Selected World". Когда шаблонов станет больше (§3), кнопка вместо прямого запуска открывает маленький список карт на выбор, и в `createFromTemplate` передаётся выбранный `templateId`.

## 5. Кто что заполняет: персонаж (per-player) vs ферма (per-world)

Раз решили, что при мультиплеере экран создания получает **каждый** заходящий игрок, а не только первый — важно сразу разделить, какое из состояний персональное (имя фермера, внешность), а какое общее на весь мир (название фермы, сброс календаря/shipping). Если этого не сделать, второй игрок на shared-ферме своим "подтверждением" повторно переименует уже названную ферму или откатит календарь — плохо.

- **Per-player** (в уже существующем `PlayerStardewState`, тот же паттерн, что `isExhausted`, persistence через `saveNBT`/`loadNBT`): флаг `characterCreated` + `farmerName` (+ позже appearance/pronoun/favorite thing).
- **Per-world** (новая небольшая `SavedData`, по образцу `StardewDateData`): `farmFounded` + `farmName`. Выставляется **один раз** — тем, кто первый успешно прошёл creation screen в этом мире (host в singleplayer, либо первый успевший на dedicated-сервере). Именно на этот же момент завязан сброс `stardew_date.dat`/`stardew_shipping.dat` (см. код handler'а ниже) — одноразовый, не должен повторяться при каждом новом join.

```java
// PlayerStardewState.java — добавить рядом с существующими fields (per-player)
private boolean characterCreated = false;
private String farmerName = "";

public boolean hasCreatedCharacter() { return characterCreated; }

public void completeCharacterCreation(String farmerName) {
    this.characterCreated = true;
    this.farmerName = farmerName;
    sync();
}
```

```java
// saveNBT/loadNBT — тот же файл, расширить существующие методы
public void saveNBT(CompoundTag tag) {
    // ...существующие поля...
    tag.putBoolean("CharacterCreated", characterCreated);
    tag.putString("FarmerName", farmerName);
}

public void loadNBT(CompoundTag tag) {
    // ...существующие поля...
    if (tag.contains("CharacterCreated")) {
        characterCreated = tag.getBoolean("CharacterCreated");
        farmerName = tag.getString("FarmerName");
    }
}
```

```java
// StardewFarmData.java — новый SavedData, по образцу StardewDateData: одна
// запись на весь мир (не на игрока), держит "кто и когда основал ферму".
public class StardewFarmData extends SavedData {
    private boolean farmFounded = false;
    private String farmName = "";

    public boolean isFarmFounded() { return farmFounded; }
    public String getFarmName() { return farmName; }

    /** @return true, если это первое основание (вызывающий должен сделать reset даты/shipping) */
    public boolean tryFoundFarm(String farmName) {
        if (farmFounded) return false;
        this.farmFounded = true;
        this.farmName = farmName;
        setDirty();
        return true;
    }

    public static StardewFarmData get(ServerLevel level) { /* как у StardewDateData */ return null; }
}
```

При каждом login, если `characterCreated` не выставлен у ЭТОГО игрока — сервер просит client открыть creation screen, и заодно сообщает, founder это или joiner (чтобы UI показал/скрыл поле "Farm Name"):

```java
package dev.flomik.stardew.common.module.player.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenCharacterCreation {
    private final boolean isFounder;      // true = первый заход в этот мир вообще
    private final String existingFarmName; // для joiner'а: показать "Welcome to <name> Farm!"

    public S2COpenCharacterCreation(boolean isFounder, String existingFarmName) {
        this.isFounder = isFounder;
        this.existingFarmName = existingFarmName;
    }

    public static void encode(S2COpenCharacterCreation msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isFounder);
        buf.writeUtf(msg.existingFarmName);
    }

    public static S2COpenCharacterCreation decode(FriendlyByteBuf buf) {
        return new S2COpenCharacterCreation(buf.readBoolean(), buf.readUtf());
    }

    public static void handle(S2COpenCharacterCreation msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->
                        () -> dev.flomik.stardew.client.screen.CharacterCreationScreen.open(msg.isFounder, msg.existingFarmName)));
        ctx.get().setPacketHandled(true);
    }
}
```

```java
// CapabilityEvents.java — расширить существующий onPlayerJoin
@SubscribeEvent
public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
    event.getEntity().getCapability(PlayerProvider.STARDEW_CAPABILITY).ifPresent(state -> {
        state.sync();
        if (event.getEntity() instanceof ServerPlayer sp && !state.hasCreatedCharacter()) {
            StardewFarmData farm = StardewFarmData.get(sp.serverLevel());
            PacketHandler.sendToPlayer(
                    new S2COpenCharacterCreation(!farm.isFarmFounded(), farm.getFarmName()), sp);
        }
    });
}
```

Client screen (`CharacterCreationScreen.open(isFounder, existingFarmName)`) всегда спрашивает имя фермера (+ позже appearance/pronoun/favorite thing). Поле "Farm Name" показывается только если `isFounder == true`; иначе вместо него — надпись `"Welcome to " + existingFarmName + " Farm!"`. Подтверждение шлёт `C2SFinishCharacterCreation(farmerName, farmNameOrNull)`.

Server handler различает founder/joiner **по возврату `tryFoundFarm`**, а не по флагу из пакета (клиенту нельзя доверять — второй игрок теоретически может успеть отправить founder-пакет, если оба одновременно жмут "Готово"; `tryFoundFarm` атомарно на сервере решает, кто на самом деле первый):

```java
// C2SFinishCharacterCreation server handler, набросок
StardewFarmData farm = StardewFarmData.get(serverLevel);
if (farm.tryFoundFarm(msg.farmName())) {
    // Реально первое основание этой фермы — одноразовый сброс мирового state,
    // которое мы специально НЕ правили руками в сырых файлах template'а
    // (data/stardew_date.dat, data/stardew_shipping.dat).
    StardewDateData.get(serverLevel).resetToNewGame(); // новый метод: Spring, day 1, sunny
    ShippingManager.get(serverLevel).clearHistory();    // новый метод: очистить записанные продажи
}
state.completeCharacterCreation(msg.farmerName());
```

Пока screen не подтверждён, gameplay должен быть заморожен тем же механизмом, который уже замораживает время в сундуках (`TimeFreezeManager` + `C2STimeFreezePacket`). Нужно переиспользовать существующий механизм, а не создавать второй.

## 6. Мультиплеер — сценарий захода

Решено: копия template через кнопку "Play Stardew Valley" создаётся только в singleplayer-списке миров (см. §4) — это единственная точка входа "с нуля". А вот дальше мир может стать shared (LAN "Open to Network" или вынесенный на dedicated-сервер) — и в этом случае **каждый** заходящий, у кого `characterCreated == false` именно в этом save, получает экран создания персонажа, просто в одном из двух режимов (см. §5):

1. **Founder** (обычно — тот, кто нажал "Play Stardew Valley" и зашёл первым): видит поле "Farm Name", по подтверждению `tryFoundFarm(...)` возвращает `true`, происходит одноразовый сброс календаря/shipping.
2. **Joiner** (все следующие, в т.ч. на LAN/dedicated-сервере): видит `"Welcome to <FarmName> Farm!"` вместо поля ввода, календарь/shipping не трогает — просто заводит своего персонажа (имя, позже — внешность).

Кто именно окажется founder'ом, решает не порядок клика в UI, а `StardewFarmData.tryFoundFarm` на сервере (см. §5) — это атомарная операция, так что даже одновременный "Готово" от двух игроков не заведёт ферму дважды и не запутает сброс даты.

Отдельно стоит вопрос: должен ли joiner на shared-ферме иметь право выбрать стартовое здание/кровать (в оригинале у второго фермера в мультиплеере — свой домик) — сейчас не спроектировано, потребует отдельного шага в creation screen для joiner-режима, когда до этого дойдём.

## 7. Открытые вопросы / дальнейшие задачи

- **Template updates**: если шаблон изменится (mod обновит стартовую ферму), уже скопированные worlds существующих игроков, очевидно, не изменятся — влияет только на *новые* игры с этого момента. Миграция не нужна.
- **NBT editing dependency**: `renameLevelNameInLevelDat` требует небольшой helper на `NbtIo` для read/patch/write. Он пока не написан, отмечено в §4.
- **UX загрузки**: первая загрузка на 150 МБ требует экрана прогресса и пути "cancel" обратно в главное меню. Здесь это пока не спроектировано.
- **Выбор стартового здания для joiner'а** на shared-ферме — см. §6, не спроектировано.
- **`StardewFarmData.get(...)`** в наброске §5 — заглушка; реализация ровно как у `StardewDateData.get(...)` (уже существующий паттерн в проекте), просто скопировать подход.

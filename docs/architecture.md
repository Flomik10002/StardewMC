# 1. Project Overview

StardewMC is a Java NeoForge 1.20.1 / 47.1.106 mod under the mod id `stardew`.

The project goal is to systematically port Stardew Valley gameplay systems into Minecraft NeoForge 1.20.1. The current codebase already implements a partial technical foundation for that port: time, seasons, weather, farming, crop growth, energy, wallet, tools, machines, shipping, HUD, quality, and stack-size changes.

This document describes the current implementation state, not the final target.

- Custom blocks and items registered under `stardew`.
- Seasonal dirt, grass, and farmland visuals.
- Farmland hydration, fertilizer state, crop block entities, crop growth, harvesting, and seed planting.
- Player energy, exhaustion, sleep/end-day handling, and wallet state through a Forge capability.
- Time, season, weather, daily luck, and scheduled morning processing through world `SavedData`.
- Farming tools with area patterns: hoes and watering cans.
- Custom chests, big chests, processing machines, and a shipping bin.
- Client HUD overlays for energy and clock.
- Custom item tooltips, quality stars/icons, and stack-size changes.

Systems not found yet: NPCs, relationships, gifting, shops, fishing, cooking stations, animals/ranching, quests, festivals as gameplay, world generation, recipes JSON, loot tables, and villager integration.

# 2. Technical Stack

- Minecraft: `1.20.1` from `gradle.properties`.
- Mod loader/API target: NeoForge 1.20.1. Source imports use `net.minecraftforge.*`.
- NeoForge version property: `neo_version=47.1.106`; this property name is confusing, but `mods.toml` declares dependency on `forge` and code uses Forge packages.
- Java: 17 via `java.toolchain.languageVersion = JavaLanguageVersion.of(17)`.
- Build system: Gradle.
- Mappings: official.
- Main Gradle plugins: `eclipse`, `idea`, `maven-publish`, `net.neoforged.gradle`, `org.spongepowered.mixin`.
- Mixin config: `src/main/resources/stardew.mixins.json`.
- Declared/runtime dependencies include `flib`, JEI APIs/runtime, GeckoLib Forge artifact, `mclib`, and local BiggerStacks-related jars from `libs/`.
- Resource output includes `src/generated/resources` in `sourceSets.main.resources`.

Do not treat the Gradle plugin/property naming as permission to use NeoForge APIs. The project constraints and current source code target Forge 1.20.1.

# 3. Entry Points

Main mod class:

- `dev.flomik.stardew.StardewMod`
- Annotated with `@Mod(StardewMod.MODID)`.
- `MODID = "stardew"`.

Constructor responsibilities:

- Gets the mod event bus from `FMLJavaModLoadingContext`.
- Calls `StardewRegistry.init(modEventBus)`.
- Loads registry holder classes: `ModTabs`, `ModBlocks`, `ModBlockEntities`, `ModItems`, `ModSounds`, `ModMenuTypes`, `ModIcons`.
- Registers `SeasonArgument` with `ArgumentTypeInfos.registerByClass`.
- Adds mod bus listeners for common setup, datagen, and capability registration.
- Registers Forge config through `StardewConfig.register()`.

Common setup:

- `PacketHandler.init()` is called in `FMLCommonSetupEvent.enqueueWork`.
- `CropRegistry.bootstrapVanillaLike(StardewMod.MODID)` registers hardcoded crop definitions.
- `ScheduleManager.register(6, 0, ...)` registers a morning pass for overworld server levels.
- Morning pass applies weather, runs `MorningPass.run`, then runs `GrowthSystem.run`.

Datagen entry:

- `StardewMod.onGatherData(GatherDataEvent)` registers `StardewItemModels` when client data is included.

Capability entry:

- `StardewMod.registerCapabilities(RegisterCapabilitiesEvent)` registers `PlayerStardewState`.

Forge/event subscribers found:

- `CommandEvents` registers direct test/admin commands: date, rain, soil, randomize dirt, energy, wallet.
- `ModCommands` registers `/stardew time get` and `/stardew time set`.
- `ForgeEvents` syncs season/world data on login/dimension change and blocks breaking non-empty custom chests.
- `SleepEventHandler` intercepts bed right-click, processes shipping, restores energy, and advances the day.
- `CapabilityEvents` attaches, clones, and syncs player capability state.
- `EnergyEventHandler` consumes/restores energy and handles exhaustion/pass-out checks.
- `ClientSetup` registers block entity renderers, layer definitions, GUI overlays, render layers, item property overrides, and menu screens.
- `ClientModelRegistry` registers and swaps seasonal baked models.
- `PatternOverlayRenderer` renders client-side tool area overlays.
- `TabManager` fills creative tab contents on the mod event bus.

Mixin entry points:

- `TransformerEngine` is configured as the Mixin plugin and installs BiggerStacks transformers.
- `ServerLevelTimeMixin` can cancel `ServerLevel.tickTime` when time is frozen.
- `MinecraftStardewMixin` calls `StardewFontHandler.setup()` after `Minecraft` construction.
- Stack-size mixins patch item stack/container/network behavior.
- Client GUI/font mixins support custom font and tooltip rendering.

# 4. Package Structure

Confirmed package layout:

- `dev.flomik.stardew`: main mod class.
- `dev.flomik.stardew.client`: client-only setup, seasonal model handling, font setup, overlay rendering, screens, renderers, and client data mirrors.
- `dev.flomik.stardew.common.admin`: commands and custom command argument.
- `dev.flomik.stardew.common.api`: small shared APIs such as block shape/item visual and quality.
- `dev.flomik.stardew.common.module.farming`: farmland block/entity, crop runtime/logic/definition, and seed item.
- `dev.flomik.stardew.common.module.machinery`: machine blocks, block entities, menus, packets, recipes, and processing base.
- `dev.flomik.stardew.common.module.nature`: dirt and grass surface blocks.
- `dev.flomik.stardew.common.module.player`: player events, sleep handling, energy/wallet capability, and player sync packet.
- `dev.flomik.stardew.common.module.shipping`: shipping bin block/entity/menu, shipping manager, and shipping result packet.
- `dev.flomik.stardew.common.module.time`: season/date/weather/time utilities, scheduled actions, time freeze, and time sync packets.
- `dev.flomik.stardew.common.module.tools`: tool patterns, tool enchant metadata, and hoe/watering can/pickaxe classes.
- `dev.flomik.stardew.common.registry`: mod registries and high-level registry holder classes.
- `dev.flomik.stardew.common.registry.framework`: builder/tooltip/datagen helpers for item/block registration.
- `dev.flomik.stardew.common.stacksize`: stack-size constants and helper logic.
- `dev.flomik.stardew.core.config`: Forge config specs.
- `dev.flomik.stardew.core.network`: shared packet channel setup.
- `dev.flomik.stardew.core.util`: shape calculation utilities.
- `dev.flomik.stardew.datagen`: item model datagen provider.
- `dev.flomik.stardew.mixin`: mixins and mixin plugin.

# 5. Registry System

Central registry class:

- `StardewRegistry`
- Uses Forge `DeferredRegister`.
- Exposes `BLOCKS`, `ITEMS`, `BLOCK_ENTITIES`, `MENUS`, `SOUNDS`, and `TABS`.
- Uses `ForgeRegistries` for Forge registries and `Registries.CREATIVE_MODE_TAB` for creative tabs.
- Provides `id(String path)` for `stardew:<path>` resource locations.

Registry holder classes:

- `ModBlocks`
- `ModItems`
- `ModBlockEntities`
- `ModMenuTypes`
- `ModSounds`
- `ModTabs`
- `ModIcons`

Builder/framework classes:

- `BlockBuilder` registers blocks, optional block items, optional block entities, optional item model datagen, creative tab assignment, and renderer providers.
- `ItemBuilder` registers items, tooltips, categories, creative tab assignment, item model datagen, and default stack size of 999.
- `BlockEntry` wraps block, block item, and block entity type registry objects.
- `TabManager` stores tab assignments and fills creative tab contents during `BuildCreativeModeTabContentsEvent`.
- `RendererRegistry` stores block entity renderer providers and registers them from `ClientSetup`.
- `DataGenManager` stores item model generators used by `StardewItemModels`.

Blocks registered in `ModBlocks`:

- Machines/craftables: `keg`, `oil_maker`, `chest`, `big_chest`, `stone_chest`, `big_stone_chest`, `bee_house`, `cheese_press`, `shipping_bin`.
- Farming/nature: `farmland`, `dirt`, `grass`, `grass_full`, `crop`.

Items registered in `ModItems`:

- Crop items/seeds found: `tomato`, `tomato_seeds`.
- Animal products/artisan goods found: eggs, milk, wool, cloth, honey, wine, cheese, mayonnaise variants, oil, pickles, jelly, caviar, aged roe, dried goods, coffee, tea, juice, vinegar, and related items.
- Tools found: basic/copper/steel/gold/iridium hoes, watering cans, and pickaxes.

Creative tabs:

- `blocks`
- `tools`
- `craftables`
- `artisan_goods`
- `animal_product`
- `crops`

Sounds:

- `hoe_till`
- `watering_can_use`
- `open_chest`
- `close_chest`
- `tool_charge`
- `machine_insert_1`
- `machine_insert_2`
- `machine_collect`

Block entities:

- Registered through `BlockBuilder`: keg, oil maker, chest, big chest, stone chest, big stone chest, bee house, cheese press, farmland, crop.
- Registered separately through `ModBlockEntities`: shipping bin.

Menu types:

- `chest`
- `big_chest`
- `shipping_bin`

# 6. Gameplay Systems

Time, seasons, and weather:

- `StardewDateData` is world `SavedData` with season, day, total days, weather, daily luck, and festival placeholder fields.
- `Season`, `Weather`, and `DayOfWeek` define time domain enums.
- `StardewTimeUtils` maps a day to 24,000 ticks, with 6:00 AM at tick 0 and 2:00 AM at tick 20,000.
- `WeatherSystem` initializes/generates deterministic weather from world seed and applies rain/thunder or clear weather to the Minecraft world.
- `ScheduleManager` triggers scheduled entries and has `forceNextDay`.
- `ServerLevelTimeMixin` plus `TimeFreezeManager` can freeze time ticking.
- Festival fields exist in `StardewDateData`, but festival gameplay is not found yet.

Farming, soil, and crops:

- `BlockFarmland` stores hydrated, fertilizer, dry shape, and wet shape blockstate properties.
- `FarmlandBlockEntity` stores hydration and fertilizer in NBT and participates in `FarmlandTracker`.
- `FertilizerType` defines quality, retaining soil, speed growth, tree fertilizer, and none.
- `BlockCrop` is a block entity crop with an `AGE` property and harvest interaction.
- `CropBlockEntity` stores crop id, current phase, days in phase, ready state, regrow state, and adjusted phase days.
- `CropRegistry` contains hardcoded bootstrap definitions for tomato, cauliflower, and rice.
- `ItemStardewSeed` plants `BlockCrop` above custom farmland and validates current season.
- `MorningPass` dehydrates/hydrates farmland and removes crops outside their valid season.
- `GrowthSystem` advances crop phases for tracked crop block entities.
- `HarvestHelper` drops harvest items, computes basic quality, and handles regrow/removal.

Nature/seasonal visuals:

- `BlockDirt` has random variants 0 to 13.
- `BlockGrassSurface` stores connected `Shape`.
- `ShapeCalculator` computes grass and farmland tiling shapes.
- `ClientModelRegistry`, `SeasonShapeModel`, and `FarmlandSeasonModel` swap baked models per current season/shape.

Tools:

- `ToolHoe` tills custom dirt into custom farmland, supports area patterns, consumes energy, and hydrates when raining.
- `ToolWateringCan` stores water as durability, hydrates farmland, supports area patterns, and consumes energy.
- `ToolPickaxe` currently stores tier only.
- `PatternType` and `PatternProvider` define single, line, and grid patterns.
- `PatternOverlayRenderer` previews affected blocks on the client.
- `ToolEnchantment` currently stores NBT keys/display names for tooltip use; no full enchantment system found yet.

Player state, energy, and wallet:

- `PlayerStardewState` stores energy, max energy, exhausted flag, money, and total earnings.
- `PlayerProvider` exposes `PlayerStardewState` as a Forge capability.
- `CapabilityEvents` attaches capability data, copies it on clone, and syncs on login.
- `EnergyEventHandler` consumes energy for tool/block actions, restores energy on eating, blocks sprinting when config disables it, applies slow effects at low energy, and handles pass-out.
- `SleepEventHandler` intercepts bed interaction, calculates sleep restoration, processes shipping, opens shipping result screen when applicable, and advances to the next day.

Quality, food, tooltips, and icons:

- `Quality` stores normal/silver/gold/iridium quality and price/stamina/health calculations.
- `StardewItemBase` appends quality icons to names.
- `StardewFoodItem` uses edibility to restore or consume energy and heal players.
- `TooltipPresets` provides description, category, separator, pattern, enchant, price, and food stat tooltip logic.
- `ModIcons` maps private-use Unicode codepoints to GUI icon textures.
- `StardewFontHandler` and client mixins replace the Minecraft font renderer for icon rendering.

Machinery and storage:

- `BlockEntityChest` and `BlockEntityBigChest` implement custom chest inventory sizes, lid animation, sounds, menus, variant data, and non-empty break prevention.
- Stone chest variants extend chest behavior.
- `AbstractProcessingBlockEntity` processes one input into one output with tick-based progress and visual output rendering.
- `BlockEntityCheesePress` has milk to cheese recipes.
- `BlockEntityOilMaker` has oil/truffle oil recipes.
- `BlockEntityKeg` is registered but currently returns an empty recipe list.
- `BlockEntityBeeHouse` renders honey when its blockstate says it has honey.
- `LoomRecipes` exists, but no loom block/entity/menu registration was found yet.

Shipping/economy:

- `ShippingBinBlock` opens a one-slot menu and supports shift-right-click retrieval of the last shipped item.
- `ShippingBinBlockEntity` stores one item slot, exposes item handler capability, tracks lid animation, and ships contents to `ShippingManager` when the menu closes.
- `ShippingManager` is server-only world `SavedData`, stores per-player shipment queues, supports retrieving the last item, calculates sell price from `PriceTooltip`, applies `Quality`, and pays online players through `PlayerStardewState`.
- `ShippingBinMenu` defines the shipping slot and player inventory slots.
- `ShippingResultScreen` is opened client-side by `S2COpenShippingResultScreen`.
- Shops/buying/trading systems are not found yet.

Stack size:

- `StackConfig.MAX_STACK_SIZE` is 999.
- `ItemBuilder` defaults item properties to `stacksTo(999)`.
- `TransformerEngine`, stack-size mixins, and `transformers/vanilla.xml` patch container/slot/item handler limits.

Commands/admin tools:

- Test/admin commands exist for date, date test, rain, soil, dirt randomization, energy, wallet, and `/stardew time`.

# 7. Client/Server Separation

Confirmed client-only package:

- `dev.flomik.stardew.client.*`

Client-only setup:

- `ClientSetup` is annotated with `@Mod.EventBusSubscriber(... value = Dist.CLIENT, bus = MOD)`.
- Registers block entity renderers through `RendererRegistry.registerAll`.
- Registers layer definitions for chest renderers.
- Registers `energy_bar` and `clock_hud` GUI overlays.
- Registers menu screens for chest, big chest, and shipping bin.
- Sets translucent render layers for farmland and oil maker.
- Registers item property overrides for egg variants.

Client-side data mirrors:

- `ClientStardewData` stores synced energy, money, season, weather, and day.
- `ClientSeasonManager` stores current season for model refresh.

Networking:

- `PacketHandler` owns `SimpleChannel` at `stardew:main` with protocol version `1`.
- S2C packets found: `S2CSeasonSync`, `S2CWorldDataSync`, `S2CSyncPlayerState`, `S2COpenShippingResultScreen`, `PacketPlayChestSound`.
- C2S packet found: `C2STimeFreezePacket`.
- Mixed or server-handled packet found: `PacketChangeChestVariant`.
- Packet helpers: `sendToAll`, `sendToPlayer`, `sendToServer`.

Potential side-boundary risks to review:

- `common.registry.framework.BlockBuilder` and `RendererRegistry` import client renderer provider classes while living under `common`.
- `PacketPlayChestSound` directly imports and uses `Minecraft` in a class registered from the common packet handler.
- `S2COpenShippingResultScreen` imports `ShippingResultScreen` and `Minecraft` in a common package packet class.
- Dedicated server behavior has not been verified in this documentation pass.

# 8. Resources/Data

Main resource roots:

- `src/main/resources`
- `src/generated/resources`

Mod metadata/resources:

- `src/main/resources/META-INF/mods.toml`
- `src/main/resources/META-INF/accesstransformer.cfg`
- `src/main/resources/pack.mcmeta`
- `src/main/resources/logo.png`
- `src/main/resources/stardew.mixins.json`
- `src/main/resources/transformers/vanilla.xml`

Assets found:

- `assets/stardew/blockstates`: blockstates for machine blocks, custom chest blocks, dirt, farmland, grass, grass_full, and scarecrow.
- `assets/stardew/models/block`: craftable block models, seasonal dirt models, seasonal grass models, seasonal farmland models, and scarecrow model.
- `assets/stardew/models/item`: manually present models for some tools and basic blocks.
- `assets/stardew/textures/block`: craftables, seasonal dirt, seasonal farmland, seasonal grass, and scarecrow textures.
- `assets/stardew/textures/item`: many item textures for tools, crops/products, animal products, and artisan goods.
- `assets/stardew/textures/gui`: clock, energy, icons, chest buttons, container textures, shipping textures, and item popup.
- `assets/stardew/textures/misc`: selection overlays.
- `assets/stardew/sounds.json` and `.ogg` sound files for tools, chests, and machines.
- `assets/stardew/lang/en_us.json` and `assets/stardew/lang/ru_ru.json`.
- Some vanilla `assets/minecraft/textures/...` overrides are present.

Data found:

- `data/stardew/tags/blocks/can_be_tilled.json` contains `stardew:dirt`.

Generated resources:

- `src/generated/resources/assets/stardew/models/item/*.json` contains generated item models.
- `src/generated/resources/META-INF/jarjar/metadata.json` exists.

Datagen:

- `StardewItemModels` extends `ItemModelProvider`.
- `DataGenManager` drives item model generation from item/block builders.
- Gradle `data` run outputs to `src/generated/resources/` and uses `src/main/resources/` as existing resources.

Resources not found yet:

- `shipping_bin` blockstate/model/texture resources not found yet.
- `crop` blockstate/model/texture resources not found yet.
- JSON recipes not found yet.
- Loot tables not found yet.
- Worldgen data not found yet.

# 9. Build Commands

Common commands:

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runData
```

Notes:

- `runClient`, `runServer`, `gameTestServer`, and `data` run configurations are defined in `build.gradle`.
- The `data` run writes generated resources to `src/generated/resources/`.
- `processResources` expands `mods.toml` and `pack.mcmeta`, and depends on `copyJarJarLibs`.
- `jar` depends on `copyJarJarLibs` and is finalized by `reobfJar`.

# 10. Known Gaps / Unknowns

- `docs/architecture.md` was empty before this pass; this file is the first architecture snapshot.
- `docs/forge-1.20.1-notes.md` exists but is empty.
- `tools/context.sh` exists, but currently exits with code 141 in this workspace because `pipefail` observes `head` closing the file-list pipe.
- Worktree is dirty with many unrelated changes; this document does not try to classify or revert them.
- The build was not run for this documentation-only update.
- Dedicated server compatibility needs validation because some common classes and packet classes reference client-only Minecraft classes.
- `BlockBuilder.seasonal(true)` stores a flag, but no code path was found that registers those blocks into `SeasonalRegistry`; seasonal rendering is currently handled directly by `ClientModelRegistry`.
- `SeasonalRegistry` exists but no current usage was found.
- `CropRegistry` bootstraps tomato, cauliflower, and rice, but registered crop items/seeds found in `ModItems` only include tomato and tomato seeds.
- `CropRegistry` tomato harvest item is `stardew:tomato_item`, while the registered item found is `stardew:tomato`.
- Cauliflower/rice item and seed registries/resources not found yet.
- Crop block resources not found yet.
- Shipping bin resources not found yet.
- `Keg` is registered but has no recipes yet.
- `LoomRecipes` exists, but loom block/entity/menu registration not found yet.
- Some oil maker recipe names/inputs appear placeholder-like in code; verify before treating them as final gameplay.
- Shipping bin slot accepts all items; unsellable items resolve to zero value instead of being blocked.
- Offline player shipping payouts are marked TODO in `ShippingManager`.
- Home detection and money loss for 2:00 AM pass-out are placeholders in `EnergyEventHandler`.
- Festival fields exist in date data, but festival logic not found yet.
- Skill/experience fields are TODO comments in `PlayerStardewState`; no implemented skill system found yet.
- No implemented systems found yet for NPCs, relationships, gifting, shops, fishing, cooking, animal ranching, quests, festivals, worldgen, JSON recipes, or loot tables.

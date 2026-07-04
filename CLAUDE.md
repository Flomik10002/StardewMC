# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

StardewMC (`mod_id = stardew`, package root `dev.flomik.stardew`) is a systematic port of Stardew Valley
gameplay systems into Minecraft 1.20.1, built on a NeoForge 1.20.1 / NeoForged Gradle toolchain that still
uses Forge-era packages (`net.minecraftforge.*`) during the Forge-to-NeoForge transition period.

Full development rules, constraints, and porting philosophy live in `AGENTS.md` — read it before making
non-trivial changes. Key pins from there, repeated here because they're load-bearing: Minecraft `1.20.1`,
NeoForge `47.1.106`, Java `17`, mappings `official`. Never upgrade these, use Fabric APIs, or use 1.21+ APIs.

A full generated architecture snapshot (entry points, every registered block/item, per-system behavior notes,
and known gaps) lives in `docs/architecture.md`. It is more detailed than this file and should be treated as
the source of truth for "does X already exist" questions — this file is a map to get oriented quickly.

## Common Commands

```bash
./gradlew build          # compile + datagen + processResources
./gradlew runClient       # launch a dev client with the mod loaded
./gradlew runServer       # launch a dev dedicated server
./gradlew runData         # regenerate src/generated/resources/ (item models etc.)
```

There is no test suite (`src/test` is empty) and no configured linter — don't invent one unless asked.

`tools/context.sh "<regex>"` greps `.java/.gradle/.properties/.toml` files for a pattern plus prints `git status`;
use it instead of reading the whole repo. Note: it currently exits with code 141 in some shells due to
`set -o pipefail` + `head` closing the pipe early — treat that specific exit code from this script as benign.

## Architecture

**Entry point**: `dev.flomik.stardew.StardewMod` (`@Mod("stardew")`). Its constructor wires up, in order:
registry init (`StardewRegistry.init`), registry holder classes (`ModBlocks`, `ModItems`, `ModBlockEntities`,
`ModSounds`, `ModMenuTypes`, `ModTabs`, `ModIcons`), capability registration, config registration, and mod-bus
listeners for common setup / datagen. Common setup boots `PacketHandler`, `CropRegistry.bootstrapVanillaLike`,
and registers a 6:00 AM `ScheduleManager` morning pass that runs weather, `MorningPass`, then `GrowthSystem`.

**Registry pattern**: `StardewRegistry` holds Forge `DeferredRegister`s for blocks/items/block entities/menus/
sounds/tabs and exposes `id(String)` for `stardew:<path>` locations. New content goes through
`common/registry/framework`'s `BlockBuilder` / `ItemBuilder`, which also wire tooltips, creative tab
assignment, and item-model datagen — follow that builder pattern rather than registering raw objects.

**Package layout** (`src/main/java/dev/flomik/stardew/`):
- `client/` — client-only: HUD overlays, screens, renderers, seasonal model swapping, custom font/tooltip
  rendering. Must never be referenced from common code.
- `common/module/*` — one package per gameplay system: `farming` (farmland/crop blocks, growth, seeds),
  `machinery` (processing block entities, menus, recipes), `nature` (seasonal dirt/grass), `player`
  (energy/wallet capability, sleep/exhaustion events), `shipping` (shipping bin + sell economy), `time`
  (season/weather/date `SavedData`, scheduled morning pass), `tools` (hoe/watering can/pickaxe + area patterns).
- `common/registry` / `common/registry/framework` — DeferredRegister holders + builder/datagen helpers.
- `common/api` — small shared contracts (block shape, quality, book/tooltip helpers).
- `core/config`, `core/network`, `core/util` — Forge config specs, `SimpleChannel` packet setup, shape math.
- `mixin/` — `TransformerEngine` (Mixin plugin, also installs BiggerStacks stack-size transformers),
  `stacksize.*` mixins (999 max-stack patches), `client.*` mixins (custom font/GUI), `common.ServerLevelTimeMixin`
  (time-freeze support).
- `datagen/` — `StardewItemModels` (item model provider), driven by builder-registered generators.

**Networking**: `PacketHandler` owns one `SimpleChannel` at `stardew:main`. S2C packets sync season/world/player
state and open result screens; the one C2S packet is time-freeze. Validate player/level/position server-side
in any new packet handler, per `AGENTS.md`.

**Client/server separation risk areas** (per `docs/architecture.md`): some `common.registry.framework` and
packet classes (`PacketPlayChestSound`, `S2COpenShippingResultScreen`) reference client renderer/`Minecraft`
classes from common-ish packages. Be careful not to add to this when touching those files, and don't assume
dedicated-server correctness has been verified there.

**Gameplay system status**: time/season/weather, farming/crops, tools with area patterns, player
energy/wallet/sleep, quality + custom tooltip/font rendering, chests/processing machines, and the shipping
economy are implemented (see `docs/architecture.md` §6 for exact classes and behavior). NPCs, relationships,
gifting, shops, fishing, cooking, animals/ranching, quests, festivals-as-gameplay, worldgen, JSON recipes, and
loot tables are not implemented yet — don't assume scaffolding exists for these without checking first.

## Working Style

Follow `AGENTS.md` for the full rules; the short version: inspect existing patterns before adding code, make
the smallest useful vertical slice (registry entry + behavior class + event hook + resource file, not a
framework), don't touch `asset-tools/` or `mc-forge-knowledge/` beyond reading them, and run `./gradlew build`
after changes when possible — report whether it actually passed rather than assuming.

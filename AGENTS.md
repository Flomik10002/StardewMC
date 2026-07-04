# AGENTS.md

## Project Overview

StardewMC is a Java Minecraft 1.20.1 mod using a NeoForge 1.20.1 / NeoForged Gradle toolchain under the mod id `stardew`.

The project goal is to systematically port Stardew Valley gameplay systems into Minecraft.

The project should treat Stardew Valley as the behavioral reference for system rules, progression structure, data, states, events, schedules, item logic, economy, NPC behavior, and gameplay loops.

The work is not about loosely imitating the vibe. The work is about decomposing Stardew Valley into concrete systems and rebuilding their Minecraft equivalents.

A “port” in this project means:

- identify the original Stardew Valley system;
- document its rules, data, states, events, timers, dependencies, and edge cases;
- design the closest Minecraft/NeoForge 1.20.1 equivalent;
- implement the system using Java and the project’s current NeoForge 1.20.1 patterns;
- adapt only where Minecraft’s engine, world model, UI model, block grid, networking, or multiplayer/server model requires it.

The priority is systemic fidelity.

## Core Direction

The mod should combine Minecraft survival/sandbox gameplay with systems inspired by farming-life simulators.

Likely systems include:

* farming progression
* crops and crop growth
* seasons
* watering
* soil mechanics
* fertilizers
* farming tools and upgrades
* cooking
* food processing
* fishing
* mining
* economy
* shops
* selling and buying
* villagers or custom NPCs
* relationships
* gifting
* schedules
* quests
* calendar events
* festivals
* cozy decorative blocks
* machines and processing stations
* animals and ranching
* long-term player progression

Every system should feel natural inside Minecraft instead of being a crude one-to-one copy of another game.

## Hard Technical Constraints

- Minecraft version: 1.20.1
- Loader/toolchain: NeoForge 1.20.1 / NeoForged Gradle
- NeoForge version: 47.1.106
- Language: Java
- Java version: 17
- Build system: Gradle
- Mappings: official

Strictly avoid:

- Fabric APIs
- Minecraft 1.21+ APIs
- modern NeoForge APIs unless verified compatible with NeoForge 1.20.1 / 47.1.106
- API assumptions from newer Minecraft versions
- dependency upgrades unless explicitly requested
- mod loader migration
- broad version changes

Important compatibility rule:

This is a 1.20.1-era NeoForge setup. Some APIs and packages may still look Forge-like because of the Forge-to-NeoForge transition period. When documentation, examples, and code disagree, the current project code and Gradle configuration are the source of truth.

## Development Philosophy

Make small, controlled, reviewable changes.

Before editing code:

1. Read this file.
2. Inspect the relevant project files.
3. Identify the existing project pattern.
4. Produce a short plan.
5. Make the smallest useful patch.
6. Run `./gradlew build` when possible.
7. Report changed files and build result.

Do not rewrite unrelated systems.
Do not perform large refactors unless explicitly requested.
Do not invent architecture when the project already has a pattern.
Do not silently change public behavior outside the requested task.
Do not rename packages, classes, registry IDs, resource paths, or mod IDs unless the task requires it.

Prefer correctness over cleverness.
Prefer stable Forge patterns over experimental abstractions.
Prefer explicit readable code over unnecessary magic.

## Important Local Folders

Expected project structure:

* `src/main/java/`
  Java source code for the mod.

* `src/main/resources/`
  Runtime mod resources: assets, data files, language files, recipes, loot tables, models, blockstates, textures, sounds, and metadata.

* `docs/`
  Human-written project documentation, architecture notes, design notes, implementation plans, and system decisions.

* `tools/`
  Clean development utilities for working with the project. This includes AI/dev workflow helpers such as context packing, search helpers, repository inspection scripts, and other stable project-support tools.

* `asset-tools/`
  Local manual asset-generation workspace. This folder may contain messy, experimental, temporary, or one-off scripts for generating PNGs, textures, sprites, icons, converters, and other asset-related files.

  Treat this folder as a workshop, not production code. Do not assume it is clean, stable, or part of the mod runtime.

* `mc-forge-knowledge/`
  Local ignored knowledge/reference base. This folder may contain Forge documentation, MinecraftForge sources, external mod references, Stardew Valley reference observations, examples, patterns, and local notes.

  This folder must not be committed.

## Local Knowledge Rules

The folder `mc-forge-knowledge/` may contain local reference material such as:

* Forge documentation for 1.20.1
* MinecraftForge source references for 1.20.1
* external mod source references
* Stardew Valley reference observations
* implementation examples
* extracted design patterns
* local research notes

Use this folder only for reference.

Do not copy external code directly.
Do not copy proprietary assets.
Do not copy game text or dialogue.
Do not copy exact item names, NPC names, locations, textures, sounds, branding, or copyrighted content from Stardew Valley or other games/mods.
Do not modify files inside `mc-forge-knowledge/` unless explicitly asked.

When using external references, extract the design principle or technical pattern and implement an original version that fits this mod.

## Asset Tools Rules

The folder `asset-tools/` is a local workshop for asset-related scripts.

It may contain:

* PNG generation scripts
* texture-processing scripts
* sprite experiments
* converters
* temporary scripts
* manual generation helpers
* messy experimental code

Rules:

* Do not assume `asset-tools/` is clean.
* Do not refactor `asset-tools/` unless explicitly asked.
* Do not run destructive scripts from `asset-tools/` unless explicitly instructed.
* If a task involves generated assets, inspect the relevant script first.
* Explain what an asset-generation script does before changing it.
* Do not treat files in `asset-tools/` as runtime mod code.
* Do not move generated files into `src/main/resources/` unless the task requires it.

## NeoForge 1.20.1 / Project API Patterns

Prefer these patterns where appropriate:

* `DeferredRegister`
* `RegistryObject`
* `ForgeRegistries`
- mod event bus for registration/setup events
- main gameplay/runtime event bus for runtime events
* `SimpleChannel` for networking
* `enqueueWork` when handling packets that interact with game state
* server-side validation for packet actions
* client-only setup for screens, renderers, keybinds, and visual-only code
* data generation when it reduces repetitive JSON maintenance
* stable lowercase resource locations
* stable registry names once introduced

Be careful with logical side separation:

* Common code must not reference client-only Minecraft classes directly.
* Screen, rendering, and keybind code belongs in client-only initialization.
* Server gameplay logic must not depend on client state.
* Network packets must validate player, world, position, and action before mutating game state.

## Expected Development Workflow

For any code change:

1. Find the relevant classes.
2. Read the existing implementation pattern.
3. Make a small plan.
4. Patch only the necessary files.
5. Build when possible.
6. Report the result.

For fixing errors:

1. Read the error or crashlog.
2. Identify the real root cause.
3. Inspect only relevant files.
4. Prefer a minimal fix.
5. Avoid unrelated refactors.
6. Run `./gradlew build` if possible.

For adding features:

1. Find existing registry and architecture patterns.
2. Follow existing naming and package style.
3. Add only required Java classes and resources.
4. Keep IDs lowercase and stable.
5. Add data/resource files only when needed.
6. Build after changes.

## Context Helper

The project may contain `tools/context.sh`.

Use it to collect relevant project context before making changes.

Example:

```bash
./tools/context.sh "DeferredRegister|RegistryObject|SimpleChannel|BlockEntity"
```

Prefer targeted searches over reading the whole repository.

If a context-packing helper exists, use it to prepare a focused context pack instead of dumping the entire project into an AI session.

## Porting Rules

When working on a feature, follow this model:

1. Identify the original Stardew Valley system.
2. Describe its actual behavior and rules.
3. Identify required data: items, prices, timers, seasons, NPCs, schedules, states, UI, progression, unlocks.
4. Map the system to Minecraft concepts:
  - tile/grid position -> BlockPos
  - farm tile -> block/state/block entity when needed
  - item -> Forge item
  - machine -> block entity + menu/screen + recipe/process timer
  - day/calendar -> persistent world calendar data
  - NPC schedule -> saved routine + AI/navigation/state
  - friendship -> persistent player/NPC relationship data
  - shop -> menu/screen + trade/economy data
  - festival -> calendar event + temporary world/NPC state
5. Decide what can be ported directly.
6. Decide what must be adapted because of Minecraft’s engine.
7. Implement the smallest vertical slice first.

## Build and Validation

Use these commands when relevant:

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

If build fails:

* Report the failure.
* Identify the likely cause.
* Fix only if the cause belongs to the current task.
* Do not hide or ignore build errors.

If a command cannot be run, say that it was not run.

## Forbidden Agent Behavior

Do not:

* rewrite the whole project
* switch the project to Fabric
- switch the project to Fabric
- migrate the project to a newer NeoForge/Minecraft version
- upgrade NeoForge version
* add large dependencies without explicit request
* generate huge abstract frameworks for simple features
* move files around without a specific reason
* copy external code directly
* copy proprietary Stardew Valley content
* touch unrelated files
* edit generated/reference folders unless explicitly asked
* refactor `asset-tools/` unless explicitly asked
* ignore compile errors
* claim success without checking build output when build was requested

## Response Style for Agents

When reporting work, be direct:

* What was changed
* Why it was changed
* What files were touched
* Whether `./gradlew build` passed
* What remains to be done
* Any known risks or uncertainty

Do not produce vague summaries.
Do not hide uncertainty.
Do not pretend an untested change is confirmed.

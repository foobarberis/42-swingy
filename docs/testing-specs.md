# Swingy — Test Suite Specification

This document is the authoritative specification of the automated test suite currently implemented in this repository.

Scope:
- Describes **what each test validates** and **what regressions it is designed to catch**.
- Describes the test harness utilities used to test controllers without real UI.
- Describes how tests are executed as part of the Maven build.

Non-goals:
- Full end-to-end scripted gameplay runs.
- Deep tests of console input threading (`ConsoleInput`) or full Swing UI automation.

Hard constraints:
- Tests must remain **high ROI** and **low maintenance** (school project).
- Tests must be deterministic.

---

## 0. How to run

Run all tests:

```bash
mvn test
```

Notes:
- Tests use **JUnit 5** (Jupiter) executed by **maven-surefire-plugin**.
- Production code remains free of test-only dependencies.

---

## 1. Test architecture and utilities

### 1.1 Determinism rules

Randomness is controlled via `com.swingy.util.RandomProvider`.

For tests that depend on procedural generation, these patterns are used:
- `DeterministicRandomProvider(seed)` for reproducible sequences.
- Small "sequence" RNGs for controller tests to force exact branches.

### 1.2 Controller testing without UI

Controllers depend on `com.swingy.view.View`. Tests avoid real `ConsoleView`/`SwingView` by using a fake implementation.

#### `src/test/java/com/swingy/support/FakeView.java`

Capabilities:
- Scripted input queue (`enqueue(...)`).
- Captured outputs (`outputs()` and `coloredOutputs()`).
- Supports `readLine()` and `readLine(timeout)` by consuming scripted inputs.
- Simulates quit attempts by enqueuing `FakeView.QUIT_ATTEMPT`, which causes `readLine*()` to return `null` and sets an internal flag consumed by `consumeQuitAttempt()`.

Limitations (intentional):
- `clearPendingInput()` is a no-op.
- `renderStatus()`/`renderMap()` only record markers (they do not validate rendering).

#### `src/test/java/com/swingy/support/InMemoryHeroRepository.java`

A minimal `HeroRepository` used to validate persistence side effects:
- Tracks `saveCalls`, `deleteCalls`, and `lastDeletedName`.
- Stores heroes in memory.

---

## 2. Unit tests — model and pure logic

### 2.1 Combat rules: `CombatResolverTest`

File:
- `src/test/java/com/swingy/model/combat/CombatResolverTest.java`

System under test:
- `com.swingy.model.combat.CombatResolver`

#### Test: `fullMatrixProducesExpectedOutcomes`
Validates the complete combat action matrix implementation.

Coverage:
- Iterates all `enemyAction` in `{ATTACK, DEFEND, SUNDER}`.
- Iterates all `playerAction` in `{ATTACK, DEFEND, SUNDER, IDLE}`.
- For matching non-IDLE actions, validates both:
  - QTE success (`qteSuccess=true`)
  - QTE failure (`qteSuccess=false`)

Assertions:
- `CombatOutcome` fields match the expected rules:
  - which side takes damage
  - which side heals
  - multipliers (1.0 / 1.5)
  - `IDLE` semantics
  - armor-broken flags (`applyArmorBrokenToHero/enemy`)

Regression risk addressed:
- Any future refactor of combat logic changing a single matrix cell.

#### Test: `idleInputNeverTriggersQteSemantics`
Validates that `playerAction=IDLE` behaves as idle even if `qteSuccess` is passed as `true`.

Regression risk addressed:
- Accidentally treating idle as a selectable action or allowing QTE branches.

#### Test: `armorBrokenAppliesDefenseReductionForDamageCalculation`
Validates `armorBroken` reduces effective DEF to `floor(def * 0.7)`.

Method:
- Compares damage taken with and without `hero.debuffState().applyArmorBrokenForNextRound()` + `beginRound()`.

Regression risk addressed:
- Incorrect debuff application or missing defense reduction.

#### Test: `sunderVsSunderSetsArmorBrokenFlags`
Validates Sunder-vs-Sunder QTE outcome flags:
- success → `applyArmorBrokenToEnemy=true`
- failure → `applyArmorBrokenToHero=true`

---

### 2.2 Hero math + formatting: `HeroTest`

File:
- `src/test/java/com/swingy/model/HeroTest.java`

System under test:
- `com.swingy.model.Hero`

#### Test: `xpThresholdMatchesFormula`
Validates `xpThreshold(level)` matches the required formula values for levels 1–4.

Regression risk addressed:
- Formula drift.

#### Test: `addXpLevelsUpAndHealsByTenPerLevel`
Validates:
- gaining exactly `xpThreshold(1)` levels the hero from 1 → 2
- XP remainder becomes 0
- heal on level-up is `+10 current HP` capped by effective max HP

Regression risk addressed:
- off-by-one in level-up loop and heal.

#### Test: `addXpSupportsMultipleLevelUps`
Validates multiple sequential level-ups in one XP gain.

Regression risk addressed:
- incorrect looping, threshold recalculation, or XP subtraction.

#### Test: `effectiveModBoundaries`
Validates effective mod conversion:
- `mod=-1` → 0
- `mod=0` → 1
- spot-check for `mod=10` based on `EFFECTIVE_MOD_K`.

Regression risk addressed:
- breaking equipment scaling behavior.

#### Test: `statusLineFormatIsStable`
Validates the exact `statusLine()` format at level 1 warrior baseline, including the hero name prefix.

Regression risk addressed:
- UX string drift.

---

### 2.3 Maze generation invariants: `MazeGeneratorTest`

File:
- `src/test/java/com/swingy/model/world/MazeGeneratorTest.java`

System under test:
- `com.swingy.model.world.MazeGenerator`

#### Test: `generatedMazeHasCorrectExitsAndReachability`
Validates that generated mazes are structurally correct and winnable.

Method:
- Runs multiple deterministic seeds.
- Generates a 21×21 maze.
- Asserts:
  - `maze.size()` equals requested size
  - `heroStart` is floor
  - 4 exits exist at the centered edges
  - adjacent interior tiles to exits are floor
  - BFS from `heroStart` reaches all exits

Regression risk addressed:
- accidentally generating unwinnable maps or misplaced exits.

---

### 2.4 Entity placement: `EntityPlacerTest`

File:
- `src/test/java/com/swingy/model/world/EntityPlacerTest.java`

System under test:
- `com.swingy.model.world.EntityPlacer`

#### Test: `placePotionRespectsBasicConstraints`
Validates potion placement constraints on a generated maze:
- potion position is non-null
- potion is placed on `TileType.FLOOR`
- potion is not on an exit
- potion respects minimum Manhattan distance `>= size/4` from start

Regression risk addressed:
- potion being unreachable due to invalid placement or being too close.

#### Test: `placeEnemiesRespectsCountAndPlacementConstraints`
Validates:
- enemy count does not exceed `floor(size^2/32)`
- each enemy:
  - is placed on floor
  - is not placed on hero start
  - is not placed on potion
  - does not overlap any other enemy

Regression risk addressed:
- collisions, illegal placement, count formula drift.

#### Test: `maybeMakeUniqueCreatesAtMostOneUniqueWithLevelPlusTwo`
Uses a forced RNG to ensure unique creation occurs.

Validates:
- unique count after conversion is exactly 1
- unique enemy level is `heroLevel + 2`

Regression risk addressed:
- unique conversion breaking constraints or not applying stat rules.

---

### 2.5 Fog-of-war rendering: `FogOfWarTest`

File:
- `src/test/java/com/swingy/model/world/FogOfWarTest.java`

System under test:
- `com.swingy.model.world.FogOfWar`

#### Test: `viewportIsAlways11x11`
Validates output dimensions are always 11×11.

Regression risk addressed:
- UI breakage caused by viewport size changes.

#### Test: `outOfBoundsIsRenderedAsSpace`
Validates out-of-bounds cells render as `' '`.

Regression risk addressed:
- incorrect padding leading to map rendering artifacts.

#### Test: `overlayPrecedenceIsPlayerThenEnemyThenPotionThenTerrain`
Constructs a small maze with:
- hero at center
- enemy adjacent
- potion adjacent
- wall two cells away

Asserts:
- center is `'@'`
- adjacent enemy renders as `'M'`
- adjacent potion renders as `'!'`
- wall renders as `'#'`

Regression risk addressed:
- precedence changes that desync CLI/GUI expectations.

---

## 3. Persistence tests

### 3.1 CSV parser strictness: `CsvHeroParserTest`

File:
- `src/test/java/com/swingy/persistence/CsvHeroParserTest.java`

Systems under test:
- `CsvHeroParser`
- `CsvHeroSerializer`

#### Test: `serializeThenParseRoundTripKeepsFields`
Validates round-trip stability for name/class/level/xp/currentHp/mods.

Regression risk addressed:
- serializer/parser disagreement causing silent save corruption.

#### Test: `malformedColumnCountIsRejected`
Validates strict parsing of 8 fields.

#### Test: `badClassTokenIsRejected`
Validates strict parsing of hero class.

#### Test: `nanNumericFieldIsRejected`
Validates numeric parsing fails on non-integers.

#### Test: `invalidRangesAreRejected`
Validates range rules:
- level must be >= 1
- xp must be >= 0
- currentHp must be >= 0
- mods must be >= -1

---

### 3.2 Repository strictness + atomic writes: `HeroCsvRepositoryTest`

File:
- `src/test/java/com/swingy/persistence/HeroCsvRepositoryTest.java`

System under test:
- `HeroCsvRepository`

#### Test: `listFailsOnBlankLineWithLineNumber`
Validates a blank line triggers `SaveFileCorruptedException` and reports correct line number.

#### Test: `listFailsOnDuplicateNameWithLineNumber`
Validates duplicate names trigger strict corruption and correct line number.

#### Test: `saveReplacesExistingHeroAndLeavesNoTmpFile`
Validates:
- saving the same hero name twice replaces in-place
- repository does not leave `heroes.csv.tmp` behind

Regression risk addressed:
- roster duplication and partial/failed writes.

---

## 4. Controller-level integration tests (no real UI)

### 4.1 Menu flow: `MenuControllerTest`

File:
- `src/test/java/com/swingy/controller/MenuControllerTest.java`

System under test:
- `MenuController`

#### Test: `firstHandleAutoListsHeroes`
Validates the auto-list behavior on first menu entry.

Assertion:
- prints `No heroes available.` for an empty roster.

#### Test: `createWithBadSyntaxPrintsUsage`
Validates wrong create argument count prints:
- `Usage: create warrior|rogue|mage <name>`

#### Test: `createDuplicateNamePrintsExpectedError`
Validates duplicate name check prints:
- `A character with the name already exists, pick another name.`

#### Test: `createOnCorruptedSavePrintsLineAwareError`
Validates strict corruption message formatting:
- `Save file heroes.csv is corrupted (line 1).`

#### Test: `loadFailurePrintsCouldNotLoadSave`
Validates load failure prints:
- `Could not load save.`

---

### 4.2 Mission flow: `GameControllerTest`

File:
- `src/test/java/com/swingy/controller/GameControllerTest.java`

System under test:
- `GameController`

Test approach:
- Uses a fixed maze generator (returns a prepared maze).
- Uses a no-op entity placer (to avoid randomness).
- Uses a stub combat controller for deterministic win/lose.
- Uses an in-memory repository to assert save/delete side effects.

#### Test: `victoryFlowSavesHeroAndReturnsToMenu`
Validates:
- stepping onto an exit prints victory message
- hero is saved via repository
- controller returns `MissionResult.RETURN_MENU`

#### Test: `deathFlowDeletesHeroAndReturnsToMenu`
Validates:
- losing combat prints death message
- hero is deleted via repository
- controller returns `MissionResult.RETURN_MENU`

#### Test: `potionPromptHealsAndGracefulExitSaves`
Validates:
- stepping on potion triggers prompt
- choosing `y` heals for `baseMaxHp/2`
- after input exhaustion (EOF), hero is saved and mission returns `EXIT_APP`

#### Test: `encounterBlocksQuitAttempt`
Validates:
- quit attempt during encounter prompt is rejected
- `You cannot quit now.` is printed

---

### 4.3 Combat controller behavior: `CombatControllerTest`

File:
- `src/test/java/com/swingy/controller/CombatControllerTest.java`

System under test:
- `CombatController`

Test approach:
- Uses a sequence RNG to force exact enemy actions and QTE letters.
- Uses `FakeView` for timed/untimed inputs.

#### Test: `timeoutResolvesAsIdleAndDoesNotTriggerQte`
Validates:
- no input available → timed read returns `null` → player action becomes `IDLE`
- combat resolves without printing any `QTE:` line

#### Test: `qteTriggersOnlyWhenActionsMatchAndNotIdle`
Validates:
- when enemy action equals a valid player action, controller prints `QTE: <letters>`
- scripted input matching the letters succeeds and combat ends deterministically

#### Test: `artifactEquipReplacesHelmAndCapsHp`
Validates:
- unique enemies drop artifacts (always)
- equipping a helm replaces old helm
- old helm discard message is printed
- hero HP is capped to new effective max HP

---

## 5. Process-level smoke

### `MainTest`

File:
- `src/test/java/com/swingy/app/MainTest.java`

System under test:
- `com.swingy.app.Main` executed in a subprocess.

#### Test: `invalidArgumentPrintsUsageAndExitsWithCodeOne`
Validates:
- invalid CLI args cause:
  - exit code `1`
  - usage string written to stderr: `Usage: java -jar swingy.jar console|gui`

Regression risk addressed:
- breaking required entrypoint behavior.

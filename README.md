# Swingy

Swingy is a Java 17 turn-based RPG with a terminal UI and a Swing GUI, selected at launch. The player creates or loads a hero, explores a generated square map, fights or escapes enemies, gains XP and equipment, and wins a mission by reaching a map border.

The application follows **strict passive MVC**. Models own game state and rules and do not depend on controllers, views, or persistence. Controllers translate user input into model and service calls, then select what to render. Views are passive: they render state supplied by controllers and return `ViewInput`; they never change game state or access persistence. CSV storage is an infrastructure adapter behind a repository interface.

## Requirements

- OpenJDK 17
- Maven 3.9 or later

Hibernate Validator and its EL implementation are the only non-JDK runtime libraries.

## Build and run

```bash
mvn clean test
mvn clean package

java -jar target/swingy.jar console
java -jar target/swingy.jar gui
```

Maven provides equivalent launch commands:

```bash
mvn clean package exec:exec@run
mvn -Pgui clean package exec:exec@run
```

With `mise` installed, use the project wrapper:

```bash
mise install
./run test
./run build
./run cli
./run gui
```

## Commands

The main menu accepts:

```text
list
create warrior|rogue|mage <name>
load <name>
quit
```

During a mission, move with `north`, `east`, `south`, or `west`. The aliases `n`, `e`, `s`, and `w` are also accepted. Encounters accept `fight`/`f` or `run`/`r`. `quit` exits from the menu, mission, encounter, or artifact prompt.

Hero names must contain 1–16 letters, digits, underscores, or hyphens. Names are unique and case-sensitive in the save file.

## Architecture and responsibilities

`Main` is the composition root. It validates the launch mode, constructs the selected view, validator, CSV repository, shared random source, services, and controllers, then starts `ApplicationController`.

```text
                         ┌─────────────────────┐
                         │ Main                │
                         │ composition root    │
                         └──────────┬──────────┘
                                    │
       ┌────────────────────────────┼───────────────────────────┐
       │                            │                           │
┌──────▼────────────────┐  ┌────────▼─────────┐      ┌──────────▼─────────┐
│ ApplicationController │  │ View             │      │ HeroRepository     │
│ menu and save policy  │◄─┤ ConsoleView      │      │ CsvStore           │
└──────────┬────────────┘  │ SwingView        │      └────────────────────┘
           │               └──────────────────┘
┌──────────▼────────────┐
│ MissionController     │
│ mission interaction   │
└───────┬────────┬──────┘
        │        │
┌───────▼───┐ ┌──▼─────────────────────┐
│ services  │ │ model                  │
│ room,     │ │ Mission, Hero, Room,   │
│ encounter │ │ Enemy, Artifact, ...   │
└───────────┘ └────────────────────────┘
```

### Application and controllers

- **`com.swingy.app.Main`** selects `console` or `gui`, creates all dependencies, and handles unrecoverable startup/runtime failures. Console and GUI are selected only at launch; runtime view switching is not implemented.
- **`ApplicationController`** owns the application loop. It renders the menu, parses `list`, `create`, `load`, and `quit`, validates new heroes, rejects duplicate names, calls the repository, starts missions, and applies the resulting save/delete/exit policy.
- **`MissionController`** owns the mission interaction loop, not mission state or rules. It translates movement and encounter input into domain calls, selects the messages to render, and returns a `MissionResult` to `ApplicationController`.
- **`MissionResult`** is a small immutable result object with three outcomes: `WON`, `HERO_DIED`, or `EXIT_APPLICATION`. It prevents the mission controller from deciding how heroes are persisted.

### Model

The model holds validated game state and behavior.

- **`Hero`** is the mutable player state: immutable name/class plus level, cumulative XP, current HP, and one modifier for each equipment slot. It creates/copies heroes, applies damage, levels from XP, calculates effective statistics, and equips artifacts.
- **`Mission`** owns all mutable state for one playthrough: the hero, generated room, current position, and previous position used when an escape succeeds. Its `move` operation returns a domain result instead of allowing controllers to manipulate coordinates directly.
- **`HeroClass`** is an enum containing the base HP, attack, and defense for `WARRIOR`, `ROGUE`, and `MAGE`.
- **`Enemy`** is a mutable combatant with immutable identity, statistics, and map position, plus mutable current HP.
- **`Artifact`** is an immutable record containing a fixed `Slot` (`WEAPON`, `ARMOR`, or `HELM`) and a modifier.
- **`Room`** represents a square mission map. It validates its odd size and center start, owns enemies, and answers bounds, border, interior, and occupancy queries.
- **`Position`** is an immutable `(x, y)` coordinate record. **`Direction`** is an enum that parses movement commands and produces the next position.

Bean Validation annotations protect persisted hero state. Constructors and methods also enforce immediate invariants such as valid levels, non-negative damage, unique enemy positions, and valid artifact modifiers.

### Game logic and persistence

- **`GameRules`** is part of the domain model and contains formulas and limits: map size, XP thresholds, artifact effects, and the supported level range of 1–100. `GameLogic` remains as a compatibility façade for its former API.
- **`RandomRoomFactory`** creates a room whose size and enemy statistics scale with the hero level. `RoomFactory` is its narrow interface, allowing tests or future map generators to substitute it.
- **`CombatService`** resolves a complete fight and returns its round-by-round history in a `CombatResult`. **`EncounterService`** owns run probability, combat resolution, XP rewards, artifact drops, defeated-enemy removal, and the resulting `EncounterResult`.
- **`HeroRepository`** defines `list`, `load`, `save`, and `delete` without exposing a storage format. **`CsvStore`** is the implementation backed by `heroes.csv`.

### View

The UI boundary is split in two. `InputPort` returns input as `ViewInput`; `GameView` exposes presentation methods for menus, maps, combat, errors, and exit reports. `View` combines both for concrete frontends and application wiring. Controllers depend on the two narrow roles separately.

- **`ConsoleView`** reads UTF-8 lines with a `BufferedReader` and writes to a `PrintStream`.
- **`SwingView`** renders a frame with status, log, and input fields. Swing event handlers enqueue submitted lines; the controller waits for them without running game logic on Swing's event-dispatch thread.
- **`SwingInputQueue`** uses a blocking queue and a close sentinel to safely pass GUI input or window closure to the controller.
- **`ViewFormatter`** is shared presentation logic. It converts model state and result records into hero status, map, combat, artifact, repository-error, and exit text.
- **`ViewInput`** distinguishes a normal line from end of console input, GUI closure, and input failure. This lets both interfaces request a clean exit through the same controller path.

## Data structures and design choices

| Structure | Where | Why it is used |
| --- | --- | --- |
| Immutable records | `Position`, `Artifact`, `CombatRound`, `CombatResult`, `MissionResult`, `ViewInput`, `ExitReport` | These are value-like messages or coordinates. Value equality makes positions usable in sets and avoids accidental mutation of result data. `CombatResult` copies its rounds list so callers cannot alter the transcript. |
| Enums | `HeroClass`, `Direction`, `Artifact.Slot`, result/input/action types | The valid alternatives are fixed. Enums make command/result branches exhaustive and avoid invalid string states inside the program. |
| `ArrayList<Enemy>` | `Room.enemies` | A room owns a mutable ordered collection: enemies are added during generation and removed after defeat. `enemyAt` scans it, which keeps the room representation simple. |
| `ArrayList<Position>` plus shuffle | `RandomRoomFactory` | The factory first collects every valid interior cell except the start, shuffles with the shared `Random`, then takes the required prefix. This samples without replacement, so enemy positions are guaranteed unique without retry loops. |
| `HashSet<Position>` | `ViewFormatter.map` | Map rendering converts the enemy list to a set once, then checks each cell efficiently while constructing the text map. |
| `ArrayList<CombatRound>` | `CombatService` | Combat appends one immutable snapshot per round. The view can render what happened after combat completes rather than mixing display code into combat rules. |
| Three integer equipment fields | `Hero.weaponMod`, `armorMod`, `helmMod` | The game has exactly three slots, so fixed fields are clearer and easier to persist than a general map. `-1` means an empty slot; a non-negative value reconstructs an `Artifact`. |
| `List<Hero>` and `HashSet<String>` | `CsvStore` | Rows are read into a list to support replacement or deletion before rewriting the complete file. The set detects duplicate hero names while parsing. |
| `LinkedBlockingQueue<Object>` | `SwingInputQueue` | It safely bridges asynchronous Swing events and the controller's blocking `readInput` call. A private sentinel represents window closure. |

The CSV parser splits each row into exactly eight columns. Names cannot contain commas because the hero-name validation forbids them, so quoting and CSV escaping are unnecessary for this format.

## Data flow

### User input to display

```text
player
  → ConsoleView / SwingView
  → ViewInput
  → ApplicationController or MissionController
  → model, service, or repository operation
  → changed model state or immutable result
  → ViewFormatter / View
  → console output or Swing widgets
```

The controller is the decision point. A view never modifies a hero or repository directly; it reports input and renders what the controller asks it to render.

### Hero creation and loading

```text
create command
  → ApplicationController parses class and name
  → Hero.createNew
  → Bean Validation
  → repository.list checks duplicate name
  → repository.save
  → MissionController.play

load command
  → CsvStore reads every row strictly
  → parse fields → Hero.Builder → Bean Validation
  → selected Hero
  → MissionController.play
```

A newly created hero is saved *before* its first mission starts. `CsvStore.save` copies the hero into its in-memory list, then serializes all heroes. `load` returns a reconstructed hero rather than a direct reference to stored state.

### Mission data flow

```text
Hero
  → RandomRoomFactory.create(hero)
  → Room: size, center start, generated enemies
  → Mission owns hero Position and previous Position
  → render Hero + Room
  → movement input
       ├─ outside map: reject move
       ├─ border: MissionResult.WON
       ├─ empty interior: next turn
       └─ enemy: encounter
                    ├─ successful run: Mission restores previous Position
                    ├─ failed run / fight: EncounterService → CombatService
                    │    → mutate Hero and Enemy HP
                    │    → CombatResult with CombatRound history
                    └─ victory: EncounterService grants XP, creates optional Artifact, and removes enemy
```

The room, enemy collection, and hero position are mission-local. They are discarded when `play` returns. Only the hero's persistent fields survive to the next program run.

### Persistence data flow

```text
Hero
  → validate
  → name,class,level,xp,currentHp,weaponMod,armorMod,helmMod
  → temporary CSV file
  → atomic replacement of heroes.csv when supported
```

`CsvStore` validates on both boundaries: before saving a hero and after rebuilding one from a row. It rejects blank rows, wrong column counts, invalid numbers/classes, duplicate names, and hero states that violate constraints. It writes a temporary file in the save file's directory, then replaces `heroes.csv` atomically when the filesystem supports it; otherwise it falls back to replacement. Temporary files are cleaned up after success or failure.

## Execution flow

### Startup and menu loop

1. `Main.main` calls `run(args)`. The only valid arguments are `console` and `gui`; another argument prints usage and returns a non-zero exit code.
2. `Main` creates the selected `View`. It then creates the Bean `Validator`, `CsvStore` for `heroes.csv`, one `Random`, `RandomRoomFactory`, `CombatService`, `MissionController`, and `ApplicationController`.
3. `ApplicationController.run` displays the welcome message. On the first menu entry, and again after every completed mission, it lists saved heroes.
4. The controller reads `ViewInput`. EOF, GUI closure, input failure, or `quit` at the menu produces an exit report and closes the view.
5. `list` reads and renders saved heroes. `create` validates and saves a hero before entering a mission. `load` reads a named hero and enters a mission. Repository failures are reported as failures and never shown as successful actions.

### Mission loop

1. `MissionController.play(hero)` asks the room factory for a new room and creates a `Mission`. The mission places the hero at the exact center.
2. Each iteration renders current hero statistics and the complete map, then waits for input.
3. A direction calls `Mission.move`. Invalid commands and outside-map moves leave the model position unchanged.
4. Reaching any border ends the mission immediately with victory. The border is always visible as `*`; the hero is `@`, enemies are `M`, and empty interior cells are `.`.
5. Entering an empty interior cell begins the next iteration. Entering an occupied cell opens the encounter loop.
6. An encounter accepts only fight or run. A successful escape restores the position before the attempted move; a failed escape continues immediately into combat.
7. Hero death ends the mission. Enemy victory removes that enemy from the room, grants XP, and may prompt for an artifact. The mission then continues from the enemy's former cell.

### Mission outcome and persistence

| Mission result | `ApplicationController` action |
| --- | --- |
| Reached a border (`WON`) | Save the living hero and return to the menu. Winning does not heal the hero. |
| Hero HP reached zero (`HERO_DIED`) | Delete the hero from the repository and return to the menu. |
| Quit, EOF, GUI closure, or input failure during a mission (`EXIT_APPLICATION`) | Attempt to save the living hero, show whether saving succeeded, then exit. |

`ApplicationController` closes the view in a `finally` block. The GUI disposes its frame without calling `System.exit`; the console process exits naturally unless startup returns a non-zero code.

## Gameplay rules

- The hero starts at the map center and wins by reaching any border.
- The player may fight or try to run from an enemy. A successful escape returns the hero to the cell occupied before the attempted move.
- The hero attacks first. A dead enemy cannot retaliate in the same round.
- Defeating an enemy grants XP and may drop an artifact. Replaced or refused artifacts are discarded.
- Winning a mission does not heal the hero.

## Formulas

All divisions below use integer division unless stated otherwise. `L` is a level, `XP` is cumulative experience, `S` is map size, and `m` is an artifact's stored modifier.

### Limits and starting statistics

Supported levels satisfy `1 ≤ L ≤ 100`. Artifact modifiers satisfy `0 ≤ m ≤ 1,000,000`; an unequipped hero slot stores `-1`.

| Class | Base HP | Base ATK | Base DEF |
| --- | ---: | ---: | ---: |
| Warrior | 125 | 10 | 20 |
| Rogue | 100 | 15 | 15 |
| Mage | 75 | 20 | 10 |

A new hero starts at level 1, with 0 XP, base HP as current and maximum HP, and no equipped artifacts.

### Map and enemy generation

For a hero of level `L`, the map size is:

```text
S = 5 × (L - 1) + 10 - (L mod 2)
```

`S` is always odd. The hero starts at `(S / 2, S / 2)`, using integer division, which is the unique center cell. The number of enemies is:

```text
enemy count = min(max(1, S² / 16), (S - 2)² - 1)
```

`(S - 2)² - 1` is the number of interior cells after excluding the center. The factory shuffles those cells and uses the first `enemy count` positions, so enemies cannot overlap, spawn on the hero, or occupy a border.

Movement adds a direction vector to the current position `(x, y)`:

```text
north = ( 0, -1)    east = (1, 0)
south = ( 0,  1)    west = (-1, 0)
next position = (x + dx, y + dy)
```

A move outside `0 ≤ x < S` or `0 ≤ y < S` is blocked. A position wins the mission when `x = 0`, `y = 0`, `x = S - 1`, or `y = S - 1`.

Each enemy first receives a random offset `r ∈ {-1, 0, 1}`:

```text
candidate level = hero level + r
enemy level = candidate level, if 1 ≤ candidate level ≤ 100
              hero level, otherwise
```

For enemy level `E`, its statistics are:

```text
enemy maximum HP = 60 + 10 × (E - 1)
enemy ATK        = 10 +  5 × (E - 1)
enemy DEF        = 10 +  5 × (E - 1)
```

### Hero statistics and artifacts

At level `L`, before equipment, a hero has:

```text
base ATK = class base ATK +  5 × (L - 1)
base DEF = class base DEF +  5 × (L - 1)
base HP  = class base HP  + 10 × (L - 1)
```

The game converts a stored artifact modifier `m` into an effective modifier `effective(m)`:

```text
effective(m) = 0,                         if m < 0
effective(m) = 1,                         if m = 0
effective(m) = floor(4.17 × ln(1 + m)),   if m > 0
```

The `m < 0` case represents an empty equipment slot. The logarithmic curve keeps large modifiers from increasing power linearly. Final hero statistics are:

```text
ATK        = base ATK + 3 × effective(weapon modifier)
DEF        = base DEF + 3 × effective(armor modifier)
maximum HP = base HP  + 5 × effective(helm modifier)
```

An artifact drops only after an enemy is defeated. It has a 35% drop chance; when it drops, each slot has a one-in-three chance. For an enemy of level `E`, the artifact modifier is:

```text
m = max(0, E - 1)
```

Thus a level-1 enemy drops a modifier of 0, which still gives +3 ATK or DEF, or +5 maximum HP because `effective(0) = 1`. Equipping a helm does not heal the hero: current HP becomes `min(current HP, new maximum HP)`.

### Combat

The hero attacks first in every round. With attacker ATK `A`, defender DEF `D`, and multiplier `K`, damage is:

```text
damage = max(1, A × K / (100 + D))
```

The hero uses `K = 200`; enemies use `K = 100`:

```text
hero damage  = max(1, hero ATK  × 200 / (100 + enemy DEF))
enemy damage = max(1, enemy ATK × 100 / (100 + hero DEF))
```

After the hero's attack, the enemy attacks only if its HP remains above zero. For either combatant, damage updates HP as:

```text
new current HP = max(0, previous current HP - damage)
```

A run succeeds when a random value is below `0.5`, giving it a 50% chance of success. A failed run starts combat immediately.

### Experience and leveling

The threshold to advance from level `L` is:

```text
threshold(L) = 1,000 × L + 450 × (L - 1)²
```

A valid hero at level `L` has:

```text
threshold(L - 1) ≤ XP < threshold(L)
```

For level 1, the lower bound is 0. The thresholds to leave levels 1, 2, 3, 4, and 5 are 1,000, 2,450, 4,800, 8,050, and 12,200 XP.

Defeating an enemy of level `E` grants:

```text
XP reward = threshold(E) / 10
```

The hero's XP is increased by that reward without resetting after a level-up. If the new total crosses one or more thresholds, the hero gains every crossed level. If `G` levels are gained, current HP becomes:

```text
current HP = min(new maximum HP, previous current HP + 10 × G)
```

This awards 10 HP per gained level without healing above the new maximum.

## Persistence and exit behavior

Only hero state is persisted: name, class, level, cumulative XP, current HP, and equipped weapon, armor, and helm modifiers. Maps, positions, enemies, and uncollected artifacts are not saved.

The file has no header. Each nonblank line contains:

```text
name,HERO_CLASS,level,xp,currentHp,weaponMod,armorMod,helmMod
```

Example:

```text
Alice,WARRIOR,3,2500,145,4,2,1
```

A living hero is saved after a mission victory and before an exit from an active mission. A dead hero is deleted. Save, load, list, and delete failures are displayed and are never presented as successful operations.

Database persistence and runtime view switching are not implemented.

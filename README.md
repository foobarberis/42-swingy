# Swingy

## Contents

- [1. Overview](#1-overview)
- [2. Build and run](#2-build-and-run)
- [3. MVC architecture](#3-mvc-architecture)
- [4. Gameplay and formulas](#4-gameplay-and-formulas)
- [5. Persistence](#5-persistence)

## 1. Overview

Swingy is a Java 17 turn-based RPG with terminal console and Swing GUI modes.

The core loop is:

```text
create or load a hero
  → explore a generated map
  → fight or run from enemies
  → reach any map border to win
  → persist the hero
```

A mission starts at the center of a new map. Maps and encounters are generated from the hero's current level.

## 2. Build and run

Requirements: Java 17, Maven, and `mise` when using `swingy.sh`.

### Maven

Run the tests and build the executable JAR:

```bash
mvn clean test
mvn clean package
```

Launch either interface from the project root:

```bash
java -jar target/swingy.jar console
java -jar target/swingy.jar gui
```

### Project wrapper

The wrapper runs Maven and Java through `mise`:

```bash
./swingy.sh clean
./swingy.sh test
./swingy.sh build
./swingy.sh cli
./swingy.sh gui
```

### Basic controls

| Context | Commands |
| --- | --- |
| Main menu | `list`, `create warrior\|rogue\|mage <name>`, `load <name>`, `quit` |
| Exploration | `north`, `east`, `south`, `west` or `n`, `e`, `s`, `w` |
| Encounter | `fight` / `f`, `run` / `r` |

## 3. MVC architecture

### What MVC means

**Model–View–Controller (MVC)** separates state and rules from presentation and input handling:

- **Model:** owns game state and rules. It does not read input, render output, or access persistence.
- **View:** displays information and returns user input without changing game state.
- **Controller:** interprets that input, invokes the required operations, and decides what the view renders next.

Swingy uses **passive MVC**: the view does not call the model directly. Controllers mediate every interaction.

```text
View (input) → Controller → Model / Services
View (output) ← Controller ← results and updated state
                       ↕
                  Repository
```

Services and the repository support MVC without being additional MVC layers. Services coordinate gameplay operations involving several model objects. The repository hides the persistence mechanism behind an interface.

### How Swingy implements it

1. `Main` selects the console or GUI view and constructs the controllers, services, and repository.
2. The selected view returns raw player input to a controller.
3. `ApplicationController` handles menu and persistence flow; `MissionController` handles exploration and encounters.
4. Controllers call model methods directly or delegate map generation and encounters to services.
5. Controllers pass the resulting state or message back to the view for rendering.

### Production source map

#### Application and controllers

| Class | Responsibility |
| --- | --- |
| `Main` | Composition root: selects the launch mode, constructs dependencies, and starts the application. |
| `ApplicationController` | Runs the main menu, creates or loads heroes, starts missions, and applies save or delete policy. |
| `MissionController` | Runs exploration, translates movement and encounter input into actions, and reports the mission outcome. |

#### Gameplay services

| Class | Responsibility |
| --- | --- |
| `RoomFactory` | Defines the interface for creating a room for a hero. |
| `RandomRoomFactory` | Generates the level-sized map and randomly places and creates its enemies. |
| `EncounterService` | Resolves escape attempts and combat, then applies XP, enemy removal, and artifact drops. |
| `EncounterResult` | Carries an encounter outcome and its escape, XP, level-up, failure, and artifact details. |

#### Model

| Class | Responsibility |
| --- | --- |
| `GameRules` | Defines the supported level range and the map-size and XP-threshold formulas. |
| `Hero` | Stores hero identity, progression, HP, and equipment and calculates effective statistics. |
| `HeroClass` | Defines the base HP, attack, and defense of Warrior, Rogue, and Mage heroes. |
| `Enemy` | Stores an enemy's type, level, calculated statistics, and current HP. |
| `EnemyType` | Defines the base statistics and display name of Goblin, Orc, and Troll enemies. |
| `Artifact` | Represents a weapon, armor, or helm modifier that a hero may equip. |
| `Mission` | Owns the active hero, room, current position, movement results, and retreat position. |
| `Room` | Represents the square map and manages enemy occupancy by position. |
| `Position` | Immutable `(x, y)` map coordinate. |
| `Direction` | Parses movement commands and calculates the next position. |

#### Persistence

| Class | Responsibility |
| --- | --- |
| `HeroRepository` | Defines the operations for listing, loading, saving, and deleting heroes. |
| `CsvStore` | Implements the repository using the `heroes.csv` file. |

#### Views

| Class | Responsibility |
| --- | --- |
| `View` | Defines the passive UI boundary: read input, display text, and close. |
| `ViewFormatter` | Formats hero status, maps, and artifact prompts for either interface. |
| `ConsoleView` | Implements the view with terminal input and output. |
| `SwingView` | Implements the view with Swing widgets and a thread-safe input queue. |

## 4. Gameplay and formulas

### Mission and map generation

A hero starts at the center of a square map and wins by reaching any border.

For hero level `L`, the map side length is:

```text
S = 5 × (L - 1) + 10 - (L mod 2)
```

For every interior cell except the center:

- there is a 10% chance that an enemy is placed there;
- enemies never spawn on the border or starting cell;
- if no enemies spawn, one is placed directly east of the center.

Each enemy type is selected randomly with equal probability. Its level is:

```text
enemy level = max(1, hero level - random(0 or 1))
```

### Hero statistics

For class base statistic `B`, level `L`, and the relevant equipment modifier:

```text
ATK = class base ATK × L + weapon modifier
DEF = class base DEF × L + armor modifier
HP  = class base HP  × L + helm modifier
```

| Hero class | Base HP | Base ATK | Base DEF |
| --- | ---: | ---: | ---: |
| Warrior | 18 | 6 | 4 |
| Rogue | 15 | 7 | 3 |
| Mage | 12 | 8 | 2 |

New heroes begin at level 1 with 0 XP, full base HP, and equipment modifiers of 0.

### Enemy statistics

Every enemy statistic scales directly with enemy level:

```text
stat = enemy type base stat × enemy level
```

| Enemy type | Base HP | Base ATK | Base DEF |
| --- | ---: | ---: | ---: |
| Goblin | 10 | 5 | 1 |
| Orc | 10 | 3 | 3 |
| Troll | 10 | 1 | 5 |

### Encounters

Combat damage is:

```text
damage = max(1, attacker ATK - defender DEF)
```

- The hero attacks first in every round.
- The enemy retaliates only if it survives the hero's attack.
- An escape attempt succeeds or fails with equal probability (50%).
- A successful escape returns the hero to the previous cell; failure starts combat.

### XP and artifacts

Defeating an enemy grants:

```text
XP reward = enemy level × 100
```

The cumulative XP threshold for level `L` is:

```text
threshold(L) = 1000 × L + 450 × (L - 1)²
```

The hero levels up whenever cumulative XP reaches the next threshold.

After a victory:

- an artifact has a 50% chance to drop;
- its slot is chosen randomly from weapon, armor, or helm;
- its modifier equals the defeated enemy's level;
- the player may equip it or discard it.

## 5. Persistence

`heroes.csv` is stored in the working directory. Each saved hero contains:

- identity (name) and class;
- level and cumulative XP;
- current HP;
- weapon, armor, and helm modifiers.

A new hero is saved before its first mission. A surviving hero is saved after winning or when quitting during a mission; a hero that dies is removed.

Only hero progression is persistent. Generated maps, enemy positions, enemy state, and active missions are not saved.

# Swingy — Game Design Document

## 1. High Concept
Swingy is a Java RPG where the player controls a hero in a procedurally generated **square maze** populated by enemies.

Objective per run: **reach an exit on the maze edge** before dying.

Front-ends:
- Console (CLI)
- Swing GUI

View is selected at launch: `console` or `gui`.

Runtime view switching is not implemented.

---

## 2. Core Loop
### 2.1 Starting Screen
On entering the starting screen, the game prints the list of available heroes automatically.

Actions:
- Create a hero
- Load an existing hero
- List heroes

### 2.2 Exploration
Exploration is turn-based: the world updates only after a player command.

Walkability (player):
- The player can move onto: floor `.`, potion `!` (without consuming it), and exit `X` (triggers victory).
- The player cannot move onto: wall `#` or outside maze bounds.

Invalid movement:
- Attempting to move into a wall/out of bounds does **not** consume a turn.
- Print: `"You cannot go there."`
- Enemy movement does not run.

**Player movement turn order**
1. Player attempts to move 1 tile (N/E/S/W).
2. If the move is invalid (wall/out of bounds): print `"You cannot go there."` and end input processing without advancing the world.
3. If the destination is an enemy tile, an encounter starts immediately.
   - If the hero dies: hero is removed and the run returns to menu.
   - If the enemy is defeated: remove that enemy from the maze.
   - If the hero escapes: hero returns to the previous tile (`turnStart`).
   - Encounter resolution **ends the turn**; enemy movement does not run this tick.
4. If the destination is not an enemy tile, enemies resolve movement **one at a time**:
   - Iterate the current enemy list in its stored order.
   - For each enemy:
     1. Roll a move chance. On success (**25%**), attempt to move.
     2. Compute the set of free neighboring tiles (N/E/S/W) that are walkable.
        - Enemies cannot move through walls.
        - An enemy cannot move onto a tile occupied by:
          - another enemy
          - the player
          - an exit
          - the potion
        - An enemy **can** move onto the tile the player just left.
        - The hero-start tile is just a floor tile; enemies can move onto it.
     3. If there is at least one valid tile, pick one uniformly at random and move.
        - If there are **no** valid neighboring tiles, the enemy stays in place.
        - If two enemies would move into the same tile, earlier enemies in the iteration order move first; later enemies see the tile as occupied and therefore cannot move there.

There is no dedicated map-inspection command; the fog of war viewport is rendered automatically before each in-mission input read.

### 2.3 Win/Lose
- **Win:** reaching any exit tile on the maze edge.
  - Show win message.
  - Save hero.
  - Hero HP carries over (saved current HP).
  - Return to starting screen.
  - Next time the hero is loaded, a **new maze** is generated.
- **Lose:** hero HP reaches 0.
  - Delete hero from persistence.
  - Return to starting screen.

---

## 3. Commands
Unknown command behavior:
- **Exploration:** unknown command does **not** consume a turn. Print:
  ```text
  Unknown command. Available commands: north (n), south (s), east (e), west (w).
  ```
- **Combat:** unknown command **does** consume the turn (treated as Idle). Print:
  ```text
  Unknown command. Available commands: attack (a), defend (d), sunder (s)
  ```

Input prompt:
- Console mode: every input read is prefixed with `> `.
- GUI mode: the text field is the prompt; no `> ` marker is printed into the log.
- Examples (console): `> attack`, `> north`.

### 3.1 Starting Screen
- `list`: list available heroes (also printed automatically on entering the starting screen).
- `load <name>`: load an existing hero by name.
  - If the save cannot be loaded for any reason (file missing/corrupt, invalid CSV format, unknown name, invalid class/name fields, negative numbers/out-of-range integers): print `"Could not load save."`
- `create <class> <name>`: create a new hero.
  - `<class>` must be exactly one of: `warrior`, `rogue`, `mage` (case-sensitive).
  - `<name>` must follow the persistence name constraints: `[A-Za-z0-9_-]{1,16}` and be unique.
  - If the name already exists: print `"A character with the name already exists, pick another name."`

### 3.2 In-Game
- Movement: `north|south|east|west` (aliases: `n|s|e|w`)
- Map: the fog of war viewport is rendered automatically before each in-mission input read (CLI prints ASCII; GUI draws tiles).
  - If the hero steps onto the potion tile, print the drink prompt (see Health).


Exiting:
- CLI:
  - Ctrl-D (EOF): graceful exit. Save hero state and print: `EOF received (Ctrl-D). Your progress has been saved. Goodbye!`
  - Ctrl-C: immediate abort. Print: `Ctrl-C detected, quitting now. Progress will not be saved.` (no save).
- GUI: closing the window exits (graceful exit).
- During combat and during the encounter prompt (fight/run):
  - Graceful quit attempts are blocked (Ctrl-D in CLI, window-close in GUI) and print: `You cannot quit now.`
  - Ctrl-C still aborts immediately (no save).
- Exiting mid-mission (outside combat) saves only the hero state (stats/xp/gear/current HP). On next load, a new maze is generated (see Persistence).

### 3.3 Combat
Accepted commands:
- `attack` (alias: `a`)
- `defend` (alias: `d`)
- `sunder` (alias: `s`)

Any other command: print the combat unknown-command message and consume the turn as Idle:
```text
Unknown command. Available commands: attack (a), defend (d), sunder (s)
```

---

## 4. Hero
### 4.1 Stats
Primary stats:
- **ATK**: increases damage dealt
- **DEF**: reduces damage taken
- **HP**: damage the hero can sustain

Level-up increases base stats:
- ATK +5
- DEF +5
- Max HP +10

### 4.2 Classes
All heroes start with no equipment.

Level 1 baselines (newly created heroes start at level 1):
- Warrior: HP 125 / ATK 10 / DEF 20
- Rogue:   HP 100 / ATK 15 / DEF 15
- Mage:    HP  75 / ATK 20 / DEF 10

---

## 5. Leveling and XP
Level thresholds follow the subject formula:
```text
XP_total(level) = level * 1000 + (level - 1)^2 * 450
```

- No level cap.

XP progression:
- Let `L` be the current hero level (starting at 1).
- XP required to level up from `L` to `L+1` is:
  ```text
  xpToNext = XP_total(L)
  ```
  Example: at level 1, `xpToNext = 1000`.

XP gain (based on enemy level; target: ~10 kills vs same-level enemies):
- Let `E` be the defeated enemy level.
- Each defeated enemy grants:
  ```text
  xpGain = XP_total(E) / 10
  ```
  (`/` is integer division; fractional XP is discarded.)

Notes:
- Uniques are `E = L + 2`, so they automatically grant more XP.
- Level-up heal: when gaining one or more levels, the hero immediately heals **+10 current HP per level gained**, capped at effective max HP.

---

## 6. World
### 6.1 Maze Size
The maze is a square grid.

Raw size formula:
```text
MapSizeRaw = (L - 1) * 5 + 10 - (L % 2)
```

Hard cap (safety):
- Prevent unbounded runtime/memory use if the level becomes unexpectedly large (corrupt save, manual edits, bugs).
```text
MAP_SIZE_CAP = 55
MapSize = min(MapSizeRaw, MAP_SIZE_CAP)
```

Hero starts at the center.

### 6.2 Tiles and Fog of War
Symbols (CLI):
- Player: `@`
- Floor: `.`
- Wall: `#`
- Exit: `X` (four exits; one per edge)
- Potion: `!`
- Enemies: `M` (regular mob), `U` (unique)
- Out-of-bounds padding: ` ` (space)

Rendering rules (overlay entities):
- The maze terrain is `#` (wall), `.` (floor), `X` (exit).
- Potions and enemies are **overlay entities** rendered on top of floor tiles (they never replace walls).
Overlay precedence (both CLI and GUI viewport rendering):
- Player `@` > enemy `M`/`U` > potion `!` > terrain (`.`/`#`/`X`).

Fog of war is mandatory: viewport-based rendering:
- The logical map view is a fixed-size **11x11** window centered on the hero.
- The viewport is rendered automatically before each in-mission input read (CLI prints ASCII; GUI draws tiles).
- If the window extends outside the maze bounds, out-of-bounds cells are spaces (` `).
- CLI output trims fully blank top/bottom rows and prints exactly one blank line above and below the visible map block.
- The window shows all entities/tiles inside it (walls, floors, exits, potion, enemies); there is no line-of-sight blocking by walls.

Example viewport output (11x11 window):
```text
 ####X####
 #.......#
 #.#####.#
 #.#.....#
 X.#..##.X
 #.#.@!#.#
 #.#####.#
 #.M.M...#
 ####X####
```

### 6.3 Maze Generation
Map size:
- Given hero level `L`, compute:
  ```text
  MapSizeRaw = (L - 1) * 5 + 10 - (L % 2)
  MapSize = min(MapSizeRaw, 55)
  ```
- Maze size is `MapSize × MapSize`.

Grid:
- Coordinates are `(x, y)` with `0 <= x,y < MapSize`.
- Initialize every tile as a **wall** `#`.

Carving model:
- Maze corridors are carved on a lattice of **cells** located at odd coordinates `(1..MapSize-2 step 2)`.
- A carved corridor tile is a **floor** `.`.

Recursive Backtracking (DFS) algorithm:
1. Compute the hero spawn position `start = (MapSize/2, MapSize/2)`.
2. Compute the carve start cell `carveStart` using an odd-center rule (as in the Python prototype):
   - `oddCenter(n) = c + (c % 2 == 0 ? 1 : 0)` where `c = n / 2` (integer division)
   - `carveX = oddCenter(MapSize)`
   - `carveY = oddCenter(MapSize)`
   - `carveStart = (carveX, carveY)`
   - This is in-bounds for the generated map sizes.
3. Mark `carveStart` as floor.
4. Maintain a stack of cells. Push `carveStart`.
5. While stack not empty:
   - Let `c` be the top cell.
   - Collect all unvisited neighbor cells at distance 2 in the four directions:
     - `n = (c.x + 2*dx, c.y + 2*dy)` where `(dx,dy)` ∈ {(1,0),(-1,0),(0,1),(0,-1)} and `n` stays inside the cell lattice.
   - If at least one neighbor exists:
     - Pick one uniformly at random.
     - Carve the wall between them: set `(c.x + dx, c.y + dy)` to floor.
     - Set `n` to floor and push `n`.
   - Else pop.

Hero spawn tile:
- Ensure the hero start tile `(MapSize/2, MapSize/2)` is a floor `.` (carve it if needed).

Exits:
- Create **four exits** `X`, centered on each edge:
  - North: `(MapSize/2, 0)`
  - South: `(MapSize/2, MapSize-1)`
  - West:  `(0, MapSize/2)`
  - East:  `(MapSize-1, MapSize/2)`
- For each exit, ensure the adjacent interior tile is floor (carve it if needed):
  - North adjacent: `(MapSize/2, 1)`
  - South adjacent: `(MapSize/2, MapSize-2)`
  - West adjacent: `(1, MapSize/2)`
  - East adjacent: `(MapSize-2, MapSize/2)`

Reachability guarantee:
- After generation, run a BFS/DFS from the hero start over walkable tiles (`.` and `X`).
- If any exit is unreachable, regenerate the maze.

### 6.4 Placement
Walkable tiles are floors `.` and exits `X`.

#### Potion placement
Potions are overlay entities (they do not replace the underlying terrain; they are rendered above floor tiles).

1. Compute dead ends:
   - A dead end is a floor tile `.` with exactly **one** walkable neighbor among its 4 cardinal neighbors.
2. Apply distance constraint:
   - Keep only dead ends with Manhattan distance from hero start `>= N`, where `N = floor(MapSize/4)`.
3. Choose a tile:
   - If the filtered dead-end list is non-empty, pick one uniformly at random.
   - Otherwise, pick any floor tile satisfying the same distance constraint.
4. Place the potion entity at that coordinate.

Potion constraints:
- Potion cannot be placed on an exit.

#### Enemy placement
Enemy count:
```text
NumEnemies = floor((MapSize * MapSize) / 32)
```

Placement constraints:
- Enemies spawn only on floor tiles `.`.
- Enemies cannot spawn on:
  - exits `X`
  - the potion `!`
  - the hero start tile
  - another enemy

Anti-spawnkill / spacing:
- Minimum Manhattan distance from hero start: `minStartDist = floor(MapSize/6)`.
- Minimum Manhattan distance between enemies: `minEnemyDist = floor(MapSize/8)`.

Algorithm:
1. Build a list of candidate floor tiles excluding hero start, exits, and potion.
2. Shuffle candidates.
3. Iterate candidates and place an enemy whenever constraints are satisfied until `NumEnemies` enemies are placed.
4. If you cannot place all enemies under constraints, relax `minEnemyDist` by 1 and retry.
   - Retry at most 3 times (total 4 placement attempts).
   - If still not enough enemies can be placed, keep fewer enemies (use whatever was placed in the final attempt) and proceed.

---

## 7. Enemies

Enemies are primarily **cosmetic**. All normal enemies share the same stat-generation rules; only the name/sprite changes.

### 7.1 Enemy roster
Default normal enemy name pool (15, from DCSS):
- Kobold
- Goblin
- Orc
- Orc wizard
- Skeleton
- Zombie
- Ogre
- Troll
- Centaur
- Yak
- Ice beast
- Jelly
- Killer bee
- Spiny frog
- Two-headed ogre

Unique name pool (10, from DCSS):
- Grinder
- Jessica
- Sigmund
- Crazy Yiuf
- Prince Ribbit
- Pikel
- Urug
- Harold
- Rupert
- Louise

Name selection:
- When spawning a normal enemy, pick a random name from the normal pool.
- When spawning a unique, pick a random name from the unique pool.

What is displayed:
- The map only shows `M` or `U` for enemies (no per-name symbols).
- The enemy name is shown in the encounter prompt and in the combat log/telegraphs.

Map symbols:
- Normal enemy: `M`
- Unique enemy: `U`

### 7.2 Stat generation
Enemy power is driven by enemy level.

Enemy level:
- **Regular mob:** `enemyLevel = heroLevel` (`L`).
- **Unique:** `enemyLevel = heroLevel + 2` (`L+2`).

Stats:
- For enemy level `E = enemyLevel`:
  ```text
  hp  = 100 + (E - 1) * 10
  atk =  15 + (E - 1) * 5
  def =  15 + (E - 1) * 5
  ```
- Enemies spawn at full HP.

### 7.3 Unique spawn rule
Each maze has a chance to contain **at most one** unique.

- Probability: `pMazeUnique = 25%`.
- If `NumEnemies == 0`, no unique spawns.
- If a unique spawns, it replaces one of the normal enemies (no placement changes):
  - After generating the list of placed enemies, pick one uniformly at random and convert it to a unique.
  - This conversion does not change coordinates; it only changes the enemy’s name/symbol/stats.
  - Set its symbol to `U`, name from the unique pool, and `enemyLevel = heroLevel + 2`.
  - The unique follows the same movement and encounter rules as normal enemies.

### 7.4 Encounters
Encounter starts when:
- Hero steps onto an enemy tile.

Enemy movement cannot start encounters.

Prompt:
- `"You have encountered <EnemyName>, do you want to fight [Y/n]?"`
- This prompt is **untimed** and quit-locked.
- During this prompt, EOF/window-close is blocked: print `"You cannot quit now."` and re-prompt.
- Accepted inputs:
  - `y` or empty input ⇒ fight
  - `n` ⇒ attempt to run
- Invalid input ⇒ print `"Please answer with y or n."` and re-prompt.

Run rule:
- If player chooses to run (including against uniques): 50% chance to return to previous tile; otherwise combat starts.

---

## 8. Combat
### 8.1 Instanced combat
Combat is instanced: only the hero and current enemy are updated until the fight resolves.

### 8.2 Timed combat input
- Exploration: no timer.
- Combat: player has a deadline per action (target: **3 seconds**).
- The timer starts **after** the enemy telegraph and the player prompt are printed.
- On timeout: player action becomes **Idle**.
- Invalid input consumes the turn (treated as **Idle**).
- Blocked graceful quit attempt (Ctrl-D/window-close while combat is quit-locked) also consumes the timed action window as **Idle**.

QTE timing:
- QTE uses its own fresh **3-second** input window.
- After the QTE is resolved, combat continues normally with the next telegraph/prompt.


### 8.3 Actions
Round structure:
1. Enemy chooses an action (uniform random).
2. Enemy telegraphs the action (color-coded).
3. Player enters an action within the deadline.
4. Resolve the exchange (including any QTE).
5. Next round.

Actions:
- Attack
- Defend
- Sunder

Enemy telegraphs its next action.

Enemy action selection:
- Each enemy action is chosen uniformly at random from {Attack, Defend, Sunder}.

Combat resolution matrix:

- Base damage:
  ```text
  BaseDamage = max(1, (AttackerATK * 200) / (100 + DefenderDEF))
  ```
- Final damage:
  ```text
  FinalDamage = BaseDamage * RpsMultiplier
  ```

| Enemy Action | Player Attack                                | Player Defend                          | Player Sunder                                                                         |
| :----------- | :------------------------------------------- | :------------------------------------- | :------------------------------------------------------------------------------------ |
| Attack       | QTE: Winner deals 1.5× dmg, loser deals 0    | Player blocks, Enemy takes 1.0x Dmg    | Player takes 1.5x Dmg                                                                 |
| Defend       | Enemy blocks, Player takes 1.0x Dmg          | QTE: Succ = Pl Heals / Fail = En heals | Enemy takes 1.0x Dmg                                                                  |
| Sunder       | Enemy takes 1.5x Dmg                         | Player takes 1.0x Dmg                  | QTE: Succ = Apply **Armor Broken** to enemy / Fail = Apply **Armor Broken** to player |

Notes:
- “Blocks” means the blocker takes **0 damage** for that exchange.
- Attack-vs-Attack (QTE): high risk.
  - QTE winner deals 1.5× damage.
  - QTE loser deals **0** damage.
- “Player blocks, Enemy takes 1.0× Dmg” (Enemy Attack vs Player Defend) is a **riposte**: the player chose the correct response and deals riposte damage.
- “Enemy blocks, Player takes 1.0× Dmg” (Enemy Defend vs Player Attack) is a **counterattack**: the player chose the wrong response and is punished.
- Riposte/counterattack damage uses the same base formula, with the riposter as the attacker:
  ```text
  RiposteBaseDamage = max(1, (RiposterATK * 200) / (100 + TargetDEF))
  RiposteFinalDamage = RiposteBaseDamage * 1.0
  ```
  - In these exchanges, only the riposte/counterattack damage is applied (the blocked side deals 0 damage).
- Idle action semantics (timeout/invalid/unknown input):
  - Idle is not a selectable action; it is a fallback only.
  - Idle does not trigger QTE.
  - If enemy action is Attack: player takes 1.0× enemy damage.
  - If enemy action is Sunder: player takes 1.0× enemy damage.
  - If enemy action is Defend: enemy heals (same amount as Defend-vs-Defend QTE heal; default 10% of base max HP).
- Defend-vs-Defend QTE healing amount is tunable; default: heal **10% of base max HP**.
  - On QTE success: **player** heals.
  - On QTE failure: **enemy** heals the same amount.
  - Healing is capped at max HP.
- **Armor Broken**: applies to the QTE loser for the **next turn** (regardless of chosen action).
  - For any damage calculation where the Armor-Broken target is the defender, treat their DEF as `floor(DEF * 0.7)`.
  - After that next turn is resolved, remove the debuff.

### 8.4 QTE
QTE triggers when both sides choose the same action.
- Print 3 random letters (e.g. `ayn`).
- The player must type them and press ENTER within the combat deadline.
- Timeout/incorrect input counts as QTE failure.
- Blocked graceful quit attempt during QTE input (Ctrl-D/window-close) also counts as QTE failure.

Enemy telegraphing:
- The enemy’s next action is announced before the player chooses.
- Color coding is used in **both** CLI and GUI:
  - Attack = Red
  - Defend = Blue
  - Sunder = Green

---

## 9. Health
Definitions:
- **Base max HP**: hero class + level HP without helm bonus.
- **Effective max HP**: base max HP + helm HP bonus.

- No passive regeneration.
- Healing:
  1. Potion: heals **50% of base max HP**.
  2. Defend-vs-Defend may allow a heal (via QTE outcome).

Potion interaction rules:
- If the hero steps onto the potion tile, immediately show the prompt:
  - `"You have found a health potion, do you want to drink it [Y/n]?"`
- If the player answers `Y`, the potion is consumed, heals **50% of base max HP** (capped at effective max HP), and the potion disappears.
- If the player answers `n`, nothing happens and the potion remains on the tile.
- The potion tile is passable; stepping onto it always triggers the prompt. The prompt is shown again the next time the hero steps onto the potion tile.

---

## 10. Artifacts / Equipment
No inventory. Equip-or-leave.

Slots:
- Weapon: +ATK
- Armor: +DEF
- Helm: +HP

### 10.1 Drops
Drop rates (defaults):
- Regular mob: **35%** chance to drop 1 artifact.
- Unique: **100%** chance to drop 1 artifact.

Dropped artifact modifier:
```text
mod = enemyLevel - 1
```

Item naming convention (derived from hero class and slot):
- Warrior: `Sword`, `Plate Armor`, `Steel Helm`
- Rogue: `Dagger`, `Leather Armor`, `Leather Helm`
- Mage: `Staff`, `Robe`, `Wizard Hat`

Displayed format:
- `<BaseName> +<mod>` (example: `Sword +7`)

### 10.2 Diminishing returns
Artifacts are labeled `+mod`.

Slot state:
- Empty slot is represented as `mod = -1` (not displayed as an item).

Convert artifact modifier to power:
```text
effectiveMod(mod) =
  0                           if mod < 0
  1                           if mod == 0
  floor(k * ln(1 + mod))      if mod >= 1
```
- `ln` is natural log; Java: `Math.log(1 + mod)`.
- Tune `k`. Calibration option: `effectiveMod(10) = 10` ⇒ `k ≈ 4.17`.
- Rationale: `+0` artifacts still give a small, non-zero bonus, while an empty slot gives no bonus.

Step constants (chosen):
- `atkStep = 3`
- `defStep = 3`
- `hpStep  = 5`

Bonus formulas:
```text
weaponAtkBonus = atkStep * effectiveMod(mod)
armorDefBonus  = defStep * effectiveMod(mod)
helmHpBonus    = hpStep  * effectiveMod(mod)
```

Final stat bonuses are linear in `effectiveMod` using these step constants.

Equip timing:
- Artifact prompts are **untimed** and are shown **after combat ends**, never during combat.

Prompt:
- `"You have found <BaseName> +<mod> (+<bonus> <stat>), do you want to equip it [Y/n]?"`
  - Example: `"You have found Sword +1 (+6 ATK), do you want to equip it [Y/n]?"`
- Accepted inputs:
  - `y` or empty input ⇒ equip
  - `n` ⇒ discard and print: `"<ItemName> has been discarded"`
    - `<ItemName>` is the displayed item name: `<BaseName> +<mod>`
- Invalid input ⇒ print `"Please answer with y or n."` and re-prompt.

Equip rules:
- Equipping always replaces the currently equipped item in that slot (if any).
  - If an old item was equipped, it is discarded and the game prints: `"<OldItemName> has been discarded"`.
  - If the slot was empty, no discard message is printed when equipping.
- If the player discards the found item (answers `n`), the game prints: `"<ItemName> has been discarded"`.
- If equipping a helm changes effective max HP, keep current HP as-is but cap it to the new effective max HP.

---

## 11. Persistence
Persistence uses a CSV text file.

File:
- Path: `heroes.csv`
- Encoding: UTF-8
- One hero per line.

Name constraints (to avoid CSV escaping):
- Hero names must match: `[A-Za-z0-9_-]{1,16}`
- Names are unique.

CSV columns (per line):
1. `name`
2. `class` (`WARRIOR|ROGUE|MAGE`)
3. `level` (int; newly created heroes start at 1)
4. `xp` (int)
5. `currentHp` (int)
6. `weaponMod` (int; `-1` means empty slot, `0+` means equipped weapon tier)
7. `armorMod` (int; `-1` means empty slot, `0+` means equipped armor tier)
8. `helmMod` (int; `-1` means empty slot, `0+` means equipped helm tier)

Example lines:
```text
# geared hero
Alice,WARRIOR,3,1520,128,4,2,1

# newly created hero (no equipment)
Bob,ROGUE,1,0,100,-1,-1,-1
```

Persistence update rules (explicit):
- On mission win (reaching an exit): save the hero.
- On mission loss (hero HP reaches 0): delete the hero from persistence.
- On mid-mission exit (Ctrl-D / window close, outside combat): save the hero.

Mid-mission exit saves only the **hero state** (stats/xp/gear/current HP). On next load, a **new maze** is generated.

Hero deletion on death:
- Death removes the hero from persistence immediately by deleting their CSV line (rewrite file without that hero).

Atomic save:
- Write to a temporary file then rename to `heroes.csv`.

Persisted (at save time):
- hero identity (name/class)
- level/xp
- current HP
- equipped artifact modifiers (weapon/armor/helm)

Not persisted:
- maze layout
- enemy positions
- potion position

On load, a **fresh maze** is generated for the hero’s level.

---

## 12. UI
A single prompt/status line format is used in **both** CLI and GUI.

Stat definitions (for the prompt):
- `baseAtk/baseDef/baseMaxHp`: hero class baseline + level-up increments only (no artifacts).
- `effectiveAtk/effectiveDef/effectiveMaxHp`: base stat plus equipped artifact bonuses.
- `currentHp` is capped at `effectiveMaxHp`.
- `xpNextThreshold`: XP required to reach the next level from current level (`XP_total(level)`).

Prompt/status line format:
```text
[Lv. <level> <classAbbrev> | <currentHp>/<effectiveMaxHp> HP <effectiveAtk>/<baseAtk> ATK <effectiveDef>/<baseDef> DEF | <xp>/<xpNextThreshold> XP]
```
Example:
```text
[Lv. 1 War. | 125/125 HP 10/10 ATK 20/20 DEF | 0/1000 XP]
```

### 12.1 CLI
- Before each in-mission input read, print the status line and the viewport map.
- Every input read prints a `> ` prefix as the input prompt.

### 12.2 GUI
- Layout (vertical stack, full width):
  1. Status line (hero stats) at the top
  2. World view (graphical tiles)
  3. Log panel (scrollable, full width; supports colored combat telegraphs)
  4. Text input field at the bottom (single-line)
- No ASCII map rendering in GUI mode.
- No `> ` prompt marker is printed in GUI mode.
- Combat deadline enforced.

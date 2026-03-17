# Swingy Evaluation Readiness Review

## 1. Summary

**Verdict: not fully evaluation-ready.**

The project builds successfully on Java 17, produces a runnable shaded JAR, passes all 34 tests, and implements the main gameplay requirements. Console launch and the CLI Maven profile were verified. The quality profile also passed Checkstyle and SpotBugs.

Primary blockers:

- MVC separation is weak: most game logic and presentation formatting live in the controller.
- No Builder pattern exists. `docs/swingy.md` does **not** explicitly require Builder, but the repository fails that criterion if it exists in the actual evaluation rubric.
- Persistence failures are silently ignored while the UI claims progress was saved.
- Persisted numeric values have no practical upper bounds and can cause overflow or excessive map allocation.
- GUI behavior is statically present but was not interactively verified or directly tested.

Against `docs/swingy.md` alone, the project is close, but the MVC and persistence weaknesses remain substantial evaluation risks.

## 2. Requirement compliance matrix

| Requirement | Status | Evidence | Required action |
|---|---|---|---|
| Java up to current LTS / Java 17 compatibility | `pass` | Java release is 17 in `pom.xml:12`; mise selects Java 17 in `mise.toml:1-3`. `javac 17.0.2`, `java 17.0.2`, and Maven 3.9.16 were verified. | None for compatibility. |
| Reproducible tooling through mise | `partial` | `mise.toml:1-3` uses floating selectors `17` and `3.9`; wrapper commands use mise in `run:4-14`. | Pin exact Java and Maven versions, or document why floating minor versions are intentional. |
| `javac`, `java`, and `mvn` available | `pass` | Required by `docs/swingy.md:14-17`; all three commands were verified on PATH. | None. |
| No default package; relevant package naming | `pass` | All 14 main Java files declare `com.swingy...` packages; examples include `src/main/java/com/swingy/app/Main.java:1`, `src/main/java/com/swingy/controller/GameLoopController.java:1`, and `src/main/java/com/swingy/persistence/CsvStore.java:1`. | None. |
| No committed `.class` files | `pass` | `.gitignore:1-3` excludes `target/`, `*.class`, and `*.csv`; no tracked `.class` or JAR files were found. | None. |
| `mvn clean package` produces runnable JAR | `pass` | Compiler release and final name are in `pom.xml:12,43`; shade manifest uses `Main` at `pom.xml:51-66`. Build produced `target/swingy.jar` with `Main-Class: com.swingy.app.Main`. | None. |
| External dependency restriction | `partial` | Runtime dependencies are Hibernate Validator and EL at `pom.xml:23-32`, matching the validation exception. JUnit is test-scoped at `pom.xml:34-39`; additional external Maven plugins appear at `pom.xml:46-116,197-245`. | Be ready to justify JUnit and quality plugins as test/build-only. Confirm the evaluator does not interpret the restriction as applying to test dependencies. |
| MVC architecture | `partial` | View abstraction and implementations are separated at `src/main/java/com/swingy/view/View.java:3-14`, `src/main/java/com/swingy/view/console/ConsoleView.java:10-64`, and `src/main/java/com/swingy/view/swing/SwingView.java:18-134`. Persistence is separate in `src/main/java/com/swingy/persistence/CsvStore.java:18-186`. However, map generation, combat, XP, artifacts, formatting, and enemy generation remain in `src/main/java/com/swingy/controller/GameLoopController.java:214-611`. | Move game mechanics into the logic/model layer and map/status presentation into a view-facing formatter or view implementation. Keep the controller as coordinator. |
| Console/GUI view selection | `pass` | Mode validation and view construction occur in `src/main/java/com/swingy/app/Main.java:19-29`; both classes implement `View`. | None. |
| Runtime view switching bonus | `fail` | View is selected once in `src/main/java/com/swingy/app/Main.java:24-29`; there is no runtime switch command. The feature is explicitly bonus-only at `docs/swingy.md:128-135`. | No action required for mandatory compliance; implement only if pursuing bonus. |
| Builder pattern | `fail` | No Builder exists. Hero restoration uses an eight-argument constructor at `src/main/java/com/swingy/model/Hero.java:38-56` and `src/main/java/com/swingy/persistence/CsvStore.java:174-184`; new heroes use a static factory at `src/main/java/com/swingy/model/Hero.java:58-60`. Builder is not required anywhere in `docs/swingy.md`. | If Builder is genuinely part of the evaluation rubric, add a validated `Hero.Builder` and use it for restoration/construction. Otherwise do not add it solely for `swingy.md`. |
| Multiple heroes and hero types | `pass` | Three classes and starting stats exist at `src/main/java/com/swingy/model/HeroClass.java:3-16`; CSV stores multiple records at `src/main/java/com/swingy/persistence/CsvStore.java:56-68,80-97`; create/load commands are at `src/main/java/com/swingy/controller/GameLoopController.java:156-211`. | None. |
| Create or select existing hero | `pass` | Menu dispatch is at `src/main/java/com/swingy/controller/GameLoopController.java:119-135`; creation at `156-188`; loading at `192-211`. | None. |
| Display all required hero statistics | `pass` | Name, class, level, XP, attack, defense, and HP are composed at `src/main/java/com/swingy/controller/GameLoopController.java:513-538` and rendered at `147,250`. | None. |
| Hero stats affected by level and artifacts | `pass` | Level-derived stats are at `src/main/java/com/swingy/controller/GameLoopController.java:539-553` and `src/main/java/com/swingy/logic/GameLogic.java:27-35`; artifact equipment is at `src/main/java/com/swingy/controller/GameLoopController.java:438-457`. | None. |
| Weapon, armor, and helm artifacts | `pass` | Slots are defined at `src/main/java/com/swingy/model/Artifact.java:4-8`; drop selection and equipping are at `src/main/java/com/swingy/controller/GameLoopController.java:392-403,438-457`; bonuses are at `579-583`. | Clarify the displayed `+0` artifact semantics noted below. |
| Exact map-size formula | `pass` | Formula is implemented at `src/main/java/com/swingy/logic/GameLogic.java:11-13`; tests cover levels 1, 7, and 12 at `src/test/java/com/swingy/logic/GameLogicTest.java:38-43`. | None. |
| Hero starts in map center | `pass` | Center is calculated at `src/main/java/com/swingy/controller/GameLoopController.java:220-222`. | None. |
| Border reach wins mission | `pass` | Border detection and victory are at `src/main/java/com/swingy/controller/GameLoopController.java:280-289`; tested at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:132-173`. | None. |
| Four one-cell movement directions | `pass` | North, south, east, and west produce one-cell destinations at `src/main/java/com/swingy/controller/GameLoopController.java:261-265`; bounds checking is at `272-275`. | None. |
| Random villains of varying power | `pass` | Interior positions are shuffled and populated at `src/main/java/com/swingy/controller/GameLoopController.java:223-242`; enemy level and stats vary at `490-497`. | Add direct generation tests; current tests inject rooms instead. |
| Fight or run, with 50% escape | `pass` | Probability is `0.5` at `src/main/java/com/swingy/controller/GameLoopController.java:44`; command validation and escape behavior are at `329-353`; both outcomes are tested at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:250-292`. | None. |
| Simulated combat and presented outcome | `pass` | Automatic round combat and damage reporting are at `src/main/java/com/swingy/controller/GameLoopController.java:355-383`; deterministic combat is tested at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:316-336`. | None. |
| Death loses mission | `pass` | Hero death ends the mission and deletes the save at `src/main/java/com/swingy/controller/GameLoopController.java:307-310`; test at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:211-229`. | Be ready to justify permanent deletion; the subject requires death but does not explicitly require deletion. |
| XP based on villain power | `pass` | XP uses enemy-level threshold at `src/main/java/com/swingy/controller/GameLoopController.java:386-388`; tests at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:338-357`. | None. |
| Level threshold formula | `partial` | Formula is exact at `src/main/java/com/swingy/logic/GameLogic.java:15-17` and tested at `src/test/java/com/swingy/logic/GameLogicTest.java:10-16`. XP is subtracted during level-up at `src/main/java/com/swingy/controller/GameLoopController.java:500-505`, making it per-level rather than cumulative. The subject wording is ambiguous. | Confirm expected XP semantics with the evaluator. Be prepared to explain why XP is reset to the remainder. |
| Artifact drop is optional and quality depends on villain strength | `pass` | Drop chance is 35% at `src/main/java/com/swingy/controller/GameLoopController.java:45,392`; quality uses enemy level at `398`; equip/discard prompt is at `407-435`. | Add equip, replacement, all-slot, and no-drop tests. |
| `java -jar ... console` entry point | `pass` | Argument handling is at `src/main/java/com/swingy/app/Main.java:19-29`; README command at `README.md:17`; direct JAR and `./run cli` launches were verified. | None. |
| `java -jar ... gui` entry point | `not verified` | Static wiring exists at `src/main/java/com/swingy/app/Main.java:19-29`; Swing implementation is at `src/main/java/com/swingy/view/swing/SwingView.java:18-134`; README command is at `README.md:18`. Interactive GUI execution was not completed. | Perform an evaluation-style GUI smoke test: create, load, move, fight/run, equip/discard, win, die, and close-window save. |
| CLI and GUI Maven profiles | `pass` | Profiles are at `pom.xml:121-181`; launch arguments are at `143,173`; wrapper commands at `run:10-14`; README commands at `README.md:20-22`. CLI profile was verified. | Smoke-test the GUI profile in the target evaluation environment. |
| Text-file persistence and startup loading | `partial` | Store path is created at `src/main/java/com/swingy/app/Main.java:26`; all hero fields are serialized at `src/main/java/com/swingy/persistence/CsvStore.java:160-184`; round-trip test at `src/test/java/com/swingy/persistence/CsvStoreTest.java:24-40`; EOF save test at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:406-429`. Save/delete errors are swallowed at `src/main/java/com/swingy/controller/GameLoopController.java:601-612`. | Surface save/delete failures and do not claim success unless persistence succeeds. |
| Annotation-based validation in model | `pass` | Hero annotations are at `src/main/java/com/swingy/model/Hero.java:8-30`; Hibernate Validator is configured at `pom.xml:23-32`; controller/store invoke it at `src/main/java/com/swingy/controller/GameLoopController.java:176,200` and `src/main/java/com/swingy/persistence/CsvStore.java:46-55,119-128`. | Strengthen constraints for null name, alive HP, and safe numeric ranges. |
| Abnormal input cannot disrupt behavior; failures highlighted | `partial` | Command shape/name validation is at `src/main/java/com/swingy/controller/GameLoopController.java:119-176`; corrupted CSV handling at `src/main/java/com/swingy/persistence/CsvStore.java:80-143`. Level and artifact values have no upper bound, and generic messages obscure causes. | Add range validation and tests for extreme persisted values, zero-HP heroes, malformed numeric fields, and write failures. |
| Relational database bonus | `fail` | Persistence is explicitly CSV in `src/main/java/com/swingy/app/Main.java:26` and `src/main/java/com/swingy/persistence/CsvStore.java:18-186`; bonus is optional at `docs/swingy.md:128-133`. | No action required unless pursuing bonus. |

## 3. Design-pattern review

### MVC

**Status: partial.**

Correct separation:

- Model types exist under `com.swingy.model`, including `Hero`, `Enemy`, `Artifact`, `Room`, and `Position`.
- Views depend on the small `View` interface at `src/main/java/com/swingy/view/View.java:3-14`.
- Persistence is isolated in `src/main/java/com/swingy/persistence/CsvStore.java:18-186`.
- `Main` composes dependencies without placing game flow in the views at `src/main/java/com/swingy/app/Main.java:24-29`.

Separation problems:

- `GameLoopController` performs map generation at `src/main/java/com/swingy/controller/GameLoopController.java:214-243`.
- It implements all combat mechanics at `src/main/java/com/swingy/controller/GameLoopController.java:329-404`.
- It mutates XP and levels at `src/main/java/com/swingy/controller/GameLoopController.java:499-510`.
- It calculates attack/defense at `src/main/java/com/swingy/controller/GameLoopController.java:539-553`.
- It assigns artifact names and bonuses at `src/main/java/com/swingy/controller/GameLoopController.java:555-591`.
- It constructs presentation strings and map glyphs at `src/main/java/com/swingy/controller/GameLoopController.java:460-487,513-538`.

The model is largely anemic and mutable: `src/main/java/com/swingy/model/Hero.java:66-123` exposes setters for every field. `src/main/java/com/swingy/logic/GameLogic.java:11-35` contains only a few formulas. An evaluator can reasonably argue that the controller is simultaneously controller, game service, and presenter.

**Required correction:** relocate existing mechanics rather than redesigning the application. Keep menu/turn orchestration in the controller; move combat, XP, effective stats, artifact application, enemy/map generation, and map/status formatting to appropriate logic or presentation components.

### Builder

**Status: absent.**

- `Hero.createNew` is a static factory, not Builder: `src/main/java/com/swingy/model/Hero.java:58-60`.
- Restoration uses an eight-argument constructor: `src/main/java/com/swingy/model/Hero.java:38-56`, `src/main/java/com/swingy/persistence/CsvStore.java:174-184`.
- `Artifact` is immutable but has only two fields, so a Builder would add little value: `src/main/java/com/swingy/model/Artifact.java:10-24`.
- `Hero` is mutable by necessity during gameplay, so Builder would improve construction safety but would not make the aggregate immutable.

A Hero Builder is defensible because the eight same-type numeric parameters are order-sensitive and persistence duplicates construction. It should enforce required name/class fields and validate restored state before returning a Hero. However, `docs/swingy.md` does not require Builder, so this is a **conditional blocker based on the external rubric**, not a verified subject violation.

## 4. Documentation review

- `README.md:3` states the project follows MVC without disclosing that combat, map generation, stat calculation, and formatting reside in the controller. This overstates separation.
- README does not mention Builder, which matches the code but may conflict with the presumed external evaluation requirement.
- README documents Java 17 accurately at `README.md:8`, matching `pom.xml:12` and `mise.toml:2`.
- The Java setup command at `README.md:33-37` is macOS-specific (`/usr/libexec/java_home`) despite the repository providing mise. On Linux it is inaccurate.
- `mise.toml` and `run` are not documented. The reproducible commands in `run:4-14` should appear in Quick Start.
- `README.md:8` says only Hibernate Validator and EL are external dependencies. This is accurate for direct runtime dependencies but should distinguish JUnit and build-analysis plugins at `pom.xml:34-39,73-116,193-250`.
- Maven profile commands at `README.md:20-22` align with `pom.xml:121-181`.
- Persistence behavior at `README.md:155-165` aligns with the intended code path, but the statement that progress “has been saved” is not guaranteed because IO failures are ignored at `src/main/java/com/swingy/controller/GameLoopController.java:601-605`.
- XP semantics are underspecified at `README.md:141-142`; it does not explain that threshold XP is subtracted on level-up.
- Death deletion is documented accurately at `README.md:134-137`.
- Architecture responsibilities, validation invariants, artifact-modifier semantics, and safe save-file ranges are not documented.
- The `tests` profile at `pom.xml:183-190` is unexplained and effectively redundant; `src/test/java/com/swingy/TestRunner.java:6-8` only tells the user to run `mvn test`.

## 5. Prioritized issues

### Critical

1. **Conditional: required Builder pattern is absent.**
   - Evidence: `src/main/java/com/swingy/model/Hero.java:38-60`, `src/main/java/com/swingy/persistence/CsvStore.java:174-184`; no Builder implementation exists.
   - Risk: immediate pattern-compliance failure if Builder belongs to the real rubric.
   - Fix: confirm the rubric. If required, add a validated Hero Builder and use it for new and persisted heroes.

### High

1. **MVC responsibilities are concentrated in the controller.**
   - Evidence: `src/main/java/com/swingy/controller/GameLoopController.java:214-243,329-404,460-591`.
   - Risk: evaluator can reject the MVC claim after code inspection.
   - Fix: move mechanics and formatting out while preserving current flow.

2. **Persistence failures are silently reported as success.**
   - Evidence: victory and EOF call save at `src/main/java/com/swingy/controller/GameLoopController.java:286-289,594-598`; IO failures are ignored at `601-612`.
   - Risk: progress or death deletion can be lost while the UI says otherwise.
   - Fix: return persistence success/failure and show an actionable error.

3. **Persisted numeric values can trigger overflow or resource exhaustion.**
   - Evidence: only minimum constraints exist at `src/main/java/com/swingy/model/Hero.java:14-30`; CSV parsing has no maximum at `src/main/java/com/swingy/persistence/CsvStore.java:112-128`; map generation allocates every interior position at `src/main/java/com/swingy/controller/GameLoopController.java:220-242`; formulas use `int` at `src/main/java/com/swingy/logic/GameLogic.java:11-17`.
   - Risk: a syntactically valid high-level save can allocate millions of positions, overflow XP formulas, or crash.
   - Fix: define safe ranges, use overflow-safe arithmetic, and validate before mission generation.

### Medium

1. **Strict dependency interpretation remains an evaluation risk.**
   - Evidence: subject restriction at `docs/swingy.md:7-8,117-126`; JUnit and external plugins at `pom.xml:34-39,73-116,193-250`.
   - Fix: document that JUnit/plugins are test/build-only and not included as unrelated runtime functionality.

2. **Validation does not encode all important invariants.**
   - Evidence: name has `@Pattern` but no `@NotNull` at `src/main/java/com/swingy/model/Hero.java:8-9`; stored HP permits zero at `src/main/java/com/swingy/model/Hero.java:20-21` and `src/main/java/com/swingy/persistence/CsvStore.java:114`; upper bounds are absent.
   - Fix: add model-level constraints and preserve cross-field checks at the persistence boundary.

3. **GUI behavior lacks direct verification.**
   - Evidence: GUI-related controller tests use `FakeView` at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:175-190,231-248`; no test exercises `src/main/java/com/swingy/view/swing/SwingView.java:18-134`.
   - Fix: complete a manual GUI checklist and add focused lifecycle/close tests where practical.

4. **Artifact tests cover only the discard prompt path.**
   - Evidence: `src/test/java/com/swingy/controller/GameLoopControllerTest.java:383-404`.
   - Fix: test equip, replacement, weapon/armor/helm effects, no drop, invalid answers, and EOF during the prompt.

5. **XP interpretation may be challenged orally.**
   - Evidence: XP is subtracted at `src/main/java/com/swingy/controller/GameLoopController.java:500-505`; test expects residual XP at `src/test/java/com/swingy/controller/GameLoopControllerTest.java:359-380`.
   - Fix: confirm whether thresholds are cumulative or per-level costs and document the decision.

6. **Artifact display is potentially misleading.**
   - Evidence: level-one enemies create modifier `0` at `src/main/java/com/swingy/controller/GameLoopController.java:398`; display calls it `+0` at `555-556`, while `effectiveMod(0)` returns one at `src/main/java/com/swingy/logic/GameLogic.java:19-24`, yielding an actual +3/+5 bonus at `src/main/java/com/swingy/controller/GameLoopController.java:579-583`.
   - Fix: make displayed rank and actual stat bonus unambiguous.

7. **README does not document mise or `run`.**
   - Evidence: reproducible commands exist at `run:4-14`, but Quick Start only lists Maven/JAR commands at `README.md:10-22`.
   - Fix: add mise setup and wrapper commands; replace or qualify the macOS-only Java command at `README.md:33-37`.

### Low

1. **Working tree is not clean.**
   - Evidence at review time: `src/main/java/com/swingy/view/swing/SwingView.java:48` differed from the tracked version by changing font size from 12 to 24.
   - Fix: ensure the intended version is committed before submission.

2. **Large maps are repeatedly appended to the GUI log.**
   - Evidence: each turn renders a full map at `src/main/java/com/swingy/controller/GameLoopController.java:250-251`; Swing appends rather than replaces at `src/main/java/com/swingy/view/swing/SwingView.java:82-85,102-105`.
   - Fix: replace the current map display instead of accumulating every map if evaluator testing shows usability problems.

3. **Quality build emits a Checkstyle configuration warning.**
   - Evidence: `pom.xml:99` configures an `encoding` parameter that the executed check goal reported as unknown.
   - Fix: remove or relocate the unsupported parameter after confirming plugin documentation.

## 6. Strengths

- Clean Java package structure with no default-package classes.
- Java 17 compilation and mise tooling are present: `pom.xml:12`, `mise.toml:1-3`.
- `mvn clean package`, `./run test`, `./run cli`, and `mvn -Pquality verify` succeeded.
- All 34 tests passed; SpotBugs reported zero findings and Checkstyle reported zero violations.
- Runnable shaded JAR has the correct entry point: `pom.xml:51-66`.
- Console input is defensive and repeatedly prompts on encounter errors: `src/main/java/com/swingy/controller/GameLoopController.java:329-353,407-435`.
- Map-size and XP formulas match the subject numerically: `src/main/java/com/swingy/logic/GameLogic.java:11-17`, `src/test/java/com/swingy/logic/GameLogicTest.java:10-16,38-43`.
- Escape behavior, combat, victory, death, leveling, persistence, corrupted saves, and EOF saving have deterministic tests.
- Persistence retains all hero-relevant fields and uses temporary-file replacement: `src/main/java/com/swingy/persistence/CsvStore.java:146-184`.
- CSV parsing rejects malformed records, duplicates, invalid annotations, excessive HP, and invalid XP: `src/main/java/com/swingy/persistence/CsvStore.java:80-143`.
- Views depend on a concise shared interface, making launch-time frontend replacement straightforward: `src/main/java/com/swingy/view/View.java:3-14`, `src/main/java/com/swingy/app/Main.java:24-29`.
- Optional database and runtime-switch bonuses are not falsely claimed.

## 7. Final evaluation checklist

- [ ] Confirm whether Builder is genuinely required; implement a validated Hero Builder if it is.
- [ ] Reduce `GameLoopController` to orchestration by moving mechanics and formatting out.
- [ ] Stop swallowing save/delete failures.
- [ ] Add safe upper-bound and overflow validation for persisted level, XP, HP, and modifiers.
- [ ] Decide and document cumulative versus per-level XP semantics.
- [ ] Manually verify the full GUI flow and GUI close/save behavior.
- [ ] Add high-risk tests: real view lifecycle, save failure, extreme save values, zero HP, artifact equip/replacement/all slots, no-drop, and EOF during artifact selection.
- [ ] Document mise and `./run`; fix the platform-specific Java setup instructions.
- [ ] Justify test/build-only dependencies against the library restriction.
- [ ] Resolve the uncommitted `src/main/java/com/swingy/view/swing/SwingView.java:48` change.
- [ ] Run final clean checks: `git status`, `./run test`, `./run build`, `mvn -Pquality verify`, and both launch modes.

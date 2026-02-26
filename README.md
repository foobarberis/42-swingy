# Swingy

Swingy is a Java RPG built with the Model–View–Controller (MVC) pattern.
Two frontends are supported and selected at launch:
- Console (CLI)
- Swing GUI

Hard constraints (project contract): Java 21 (LTS), Maven, only Hibernate Validator (+ EL) as external deps, persistence via `heroes.csv`.

## Quick start (build/run)

```bash
# build
mvn clean package

# run (choose one mode)
java -jar target/swingy.jar console
java -jar target/swingy.jar gui

# build + run via Maven profiles
mvn -Pcli clean package exec:exec@run-cli
mvn -Pgui clean package exec:exec@run-gui  # enables Swing font AA

# run tests (no external test framework)
mvn -Ptests test-compile exec:java
```

## Static analysis (SpotBugs)

SpotBugs is integrated via the SpotBugs Maven Plugin.
Run Maven itself on Java 21 (otherwise you may accidentally execute under another installed JDK).

```bash
# ensure Maven runs on Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

# run SpotBugs (report in target/spotbugsXml.xml)
mvn -DskipTests spotbugs:spotbugs

# fail the build if bugs are found
mvn -DskipTests spotbugs:check

# security-only scan (FindSecBugs; filters: spotbugs-security-include.xml/exclude.xml)
mvn -DskipTests -Pspotbugs-security spotbugs:spotbugs
```

Outputs:
- `target/spotbugsXml.xml` (main SpotBugs XML report)

## Documentation map

- `docs/requirements.md`
  - Purpose: the **subject requirements** and the **evaluation scale** (what is mandatory, what is checked, and under which constraints).

- `docs/gdd.md`
  - Purpose: the **Game Design Document**.
  - Contains gameplay rules, commands, world/combat/equipment behavior, UX strings, and formulas.
  - No implementation details (no class names, method names, or technology-specific UI/input mechanisms).

- `docs/specs.md`
  - Purpose: the **Technical Specification**.
  - Contains implementation-facing decisions: architecture boundaries, data flow, persistence format, build/run, and any concrete class/method/UI implementation details.

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
```

## Tests

```bash
mvn test
```

## Static analysis

Run Maven itself on Java 21 (otherwise you may accidentally execute under another installed JDK).

```bash
# ensure Maven runs on Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

# SpotBugs report (target/spotbugsXml.xml)
mvn -DskipTests spotbugs:spotbugs

# SpotBugs gate (fails build on findings)
mvn -DskipTests spotbugs:check

# SpotBugs security scan (FindSecBugs; filters: config/spotbugs/spotbugs-security-include.xml and config/spotbugs/spotbugs-security-exclude.xml)
mvn -DskipTests -Pspotbugs-security spotbugs:spotbugs

# Checkstyle report (target/site/checkstyle.html)
mvn checkstyle:checkstyle

# Checkstyle gate (fails build on violations)
mvn checkstyle:check

# Optional combined quality gate (verify phase)
mvn -Pquality verify
```

Outputs:
- `target/spotbugsXml.xml` (SpotBugs XML report)
- `target/site/checkstyle.html` (Checkstyle HTML report)

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

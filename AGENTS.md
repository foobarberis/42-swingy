# AGENTS.md (Repository operating rules for AI agents)

## Non‑negotiable quality gate
- After any change that affects code, tests, build config, or documentation, run:
  ```bash
  mvn -Pquality verify
  ```
- Do not stop until the command succeeds.
- Fix every failure (tests, Checkstyle, SpotBugs). Do not bypass with `-DskipTests`, lowered thresholds, or blanket suppressions.

## Build environment
- Java: 21 (`maven.compiler.release=21`).
- Build tool: Maven.
- Output artifact: `target/swingy.jar` (shade plugin).
- Main class: `com.swingy.app.Main`.

## Fast local workflow
- Build jar:
  ```bash
  mvn clean package
  ```
- Run:
  ```bash
  java -jar target/swingy.jar console
  java -jar target/swingy.jar gui
  ```
- Tests:
  ```bash
  mvn test
  ```

## Quality tooling (what `-Pquality verify` enforces)
- Checkstyle (rules in `config/checkstyle/checkstyle.xml`, suppressions in `config/checkstyle/suppressions.xml`).
- SpotBugs (build fails on findings).

Reports/outputs:
- SpotBugs XML: `target/spotbugsXml.xml`
- Checkstyle HTML: `target/site/checkstyle.html`

## Repository constraints
- Keep external dependencies minimal and aligned with the project contract (see `README.md`): only Hibernate Validator (+ EL) and JUnit for tests.
- Persistence is via `heroes.csv`. Do not introduce databases or additional persistence libraries.
- Maintain MVC separation as documented under `docs/`.

## Rules for changes
- Make the smallest change that satisfies the requirement.
- Keep public APIs stable unless explicitly required.
- Add/adjust JUnit tests when behavior changes.
- If a static-analysis issue requires a suppression, scope it narrowly and document the reason in the suppression entry (avoid global suppressions).
- Before finishing any task, `mvn -Pquality verify` must be green.

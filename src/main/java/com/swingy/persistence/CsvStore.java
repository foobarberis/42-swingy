package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CsvStore implements HeroRepository {
    private final Path path;
    private final Validator validator;

    public CsvStore(Path path, Validator validator) {
        this.path = Objects.requireNonNull(path, "Save path is required.")
            .toAbsolutePath()
            .normalize();
        if (this.path.getFileName() == null) {
            throw new IllegalArgumentException("Save path must identify a file.");
        }
        this.validator = Objects.requireNonNull(validator, "Validator is required.");
    }

    @Override
    public List<Hero> list() throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        return readAllStrict();
    }

    @Override
    public Hero load(String name) throws IOException {
        Objects.requireNonNull(name, "Hero name is required.");
        if (!Files.exists(path)) {
            throw new IOException("The save file does not exist.");
        }
        for (Hero hero : readAllStrict()) {
            if (hero.getName().equals(name)) {
                return hero;
            }
        }
        throw new IOException("No saved hero named '" + name + "' exists.");
    }

    @Override
    public void save(Hero hero) throws IOException {
        Objects.requireNonNull(hero, "Hero is required.");
        String validationFailure = validationFailure(hero);
        if (validationFailure != null) {
            throw new IOException("Hero validation failed: " + validationFailure);
        }

        List<Hero> heroes = Files.exists(path) ? readAllStrict() : new ArrayList<>();
        boolean replaced = false;
        for (int index = 0; index < heroes.size(); index++) {
            if (heroes.get(index).getName().equals(hero.getName())) {
                heroes.set(index, hero.copy());
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            heroes.add(hero.copy());
        }
        writeAll(heroes);
    }

    @Override
    public void delete(String name) throws IOException {
        Objects.requireNonNull(name, "Hero name is required.");
        if (!Files.exists(path)) {
            return;
        }
        List<Hero> heroes = readAllStrict();
        heroes.removeIf(hero -> hero.getName().equals(name));
        writeAll(heroes);
    }

    private List<Hero> readAllStrict() throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Hero> heroes = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            int lineNumber = index + 1;
            if (line.isBlank()) {
                throw corrupted(lineNumber, "blank line");
            }
            Hero hero = parseHero(line, lineNumber);
            if (!names.add(hero.getName())) {
                throw corrupted(lineNumber, "duplicate hero name '" + hero.getName() + "'");
            }
            heroes.add(hero);
        }
        return heroes;
    }

    private Hero parseHero(String line, int lineNumber) throws SaveFileCorruptedException {
        String[] parts = line.split(",", -1);
        if (parts.length != 8) {
            throw corrupted(lineNumber, "expected 8 columns but found " + parts.length);
        }

        HeroClass heroClass;
        try {
            heroClass = HeroClass.valueOf(parts[1]);
        } catch (IllegalArgumentException exception) {
            throw corrupted(lineNumber, "invalid hero class '" + parts[1] + "'", exception);
        }

        int level = parseInt(parts[2], "level", lineNumber);
        long xp = parseLong(parts[3], "experience", lineNumber);
        int currentHp = parseInt(parts[4], "hit points", lineNumber);
        int weaponMod = parseInt(parts[5], "weapon modifier", lineNumber);
        int armorMod = parseInt(parts[6], "armor modifier", lineNumber);
        int helmMod = parseInt(parts[7], "helm modifier", lineNumber);

        Hero hero = Hero.builder(parts[0], heroClass)
            .level(level)
            .xp(xp)
            .currentHp(currentHp)
            .weaponMod(weaponMod)
            .armorMod(armorMod)
            .helmMod(helmMod)
            .build();
        String validationFailure = validationFailure(hero);
        if (validationFailure != null) {
            throw corrupted(lineNumber, validationFailure);
        }
        return hero;
    }

    private int parseInt(String token, String field, int lineNumber)
        throws SaveFileCorruptedException {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException exception) {
            throw corrupted(lineNumber, "invalid " + field + " '" + token + "'", exception);
        }
    }

    private long parseLong(String token, String field, int lineNumber)
        throws SaveFileCorruptedException {
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException exception) {
            throw corrupted(lineNumber, "invalid " + field + " '" + token + "'", exception);
        }
    }

    private String validationFailure(Hero hero) {
        Set<ConstraintViolation<Hero>> violations = validator.validate(hero);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .collect(Collectors.joining("; "));
    }

    private void writeAll(List<Hero> heroes) throws IOException {
        Path parent = Objects.requireNonNull(path.getParent(), "Save path parent is required.");
        String fileName = Objects.requireNonNull(
            path.getFileName(),
            "Save path file name is required."
        ).toString();
        String prefix = fileName.length() >= 3 ? fileName : fileName + "___";
        Path tempPath = null;
        Throwable failure = null;
        try {
            tempPath = Files.createTempFile(parent, prefix + ".", ".tmp");
            List<String> lines = new ArrayList<>(heroes.size());
            for (Hero hero : heroes) {
                lines.add(serializeHero(hero));
            }
            Files.write(tempPath, lines, StandardCharsets.UTF_8);
            try {
                Files.move(
                    tempPath,
                    path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException exception) {
            failure = exception;
            throw exception;
        } finally {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException cleanupException) {
                    if (failure != null) {
                        failure.addSuppressed(cleanupException);
                    } else {
                        throw cleanupException;
                    }
                }
            }
        }
    }

    private String serializeHero(Hero hero) {
        return String.join(
            ",",
            hero.getName(),
            hero.getHeroClass().name(),
            Integer.toString(hero.getLevel()),
            Long.toString(hero.getXp()),
            Integer.toString(hero.getCurrentHp()),
            Integer.toString(hero.getWeaponMod()),
            Integer.toString(hero.getArmorMod()),
            Integer.toString(hero.getHelmMod())
        );
    }

    private SaveFileCorruptedException corrupted(int lineNumber, String reason) {
        return new SaveFileCorruptedException(path, lineNumber, reason);
    }

    private SaveFileCorruptedException corrupted(
        int lineNumber,
        String reason,
        Throwable cause
    ) {
        return new SaveFileCorruptedException(path, lineNumber, reason, cause);
    }
}

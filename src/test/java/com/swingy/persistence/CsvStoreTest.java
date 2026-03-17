package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvStoreTest {
    @TempDir
    Path tempDir;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void saveLoadAndListRoundTripEveryHeroField() throws Exception {
        CsvStore store = store(file());
        Hero hero = Hero.builder("Alice", HeroClass.WARRIOR)
            .level(3)
            .xp(2_500L)
            .currentHp(145)
            .weaponMod(4)
            .armorMod(2)
            .helmMod(1)
            .build();

        store.save(hero);
        Hero loaded = store.load("Alice");

        assertHeroEquals(hero, loaded);
        assertEquals(1, store.list().size());
    }

    @Test
    void missingFileListsNoHeroesButLoadReportsFailure() throws Exception {
        CsvStore store = store(file());

        assertTrue(store.list().isEmpty());
        assertThrows(IOException.class, () -> store.load("Alice"));
    }

    @Test
    void blankLineIncludesLineNumberAndCorruptionReason() throws Exception {
        Files.write(
            file(),
            List.of("Alice,WARRIOR,1,0,125,-1,-1,-1", ""),
            StandardCharsets.UTF_8
        );

        SaveFileCorruptedException exception = assertThrows(
            SaveFileCorruptedException.class,
            () -> store(file()).list()
        );

        assertEquals(2, exception.getLineNumber());
        assertEquals("blank line", exception.getReason());
        assertTrue(exception.getMessage().contains(": blank line."));
    }

    @Test
    void duplicateNamesAreRejected() throws Exception {
        Files.write(
            file(),
            List.of(
                "Alice,WARRIOR,1,0,125,-1,-1,-1",
                "Alice,ROGUE,1,0,100,-1,-1,-1"
            ),
            StandardCharsets.UTF_8
        );

        SaveFileCorruptedException exception = assertThrows(
            SaveFileCorruptedException.class,
            () -> store(file()).list()
        );

        assertEquals(2, exception.getLineNumber());
        assertTrue(exception.getReason().contains("duplicate"));
    }

    @Test
    void malformedColumnCountAndNumbersAreRejectedWithReasons() throws Exception {
        Files.writeString(
            file(),
            "Alice,WARRIOR,1,0,125,-1,-1",
            StandardCharsets.UTF_8
        );
        SaveFileCorruptedException columns = assertThrows(
            SaveFileCorruptedException.class,
            () -> store(file()).list()
        );
        assertTrue(columns.getReason().contains("8 columns"));

        Files.writeString(
            file(),
            "Alice,WARRIOR,not-a-number,0,125,-1,-1,-1",
            StandardCharsets.UTF_8
        );
        SaveFileCorruptedException number = assertThrows(
            SaveFileCorruptedException.class,
            () -> store(file()).list()
        );
        assertTrue(number.getReason().contains("invalid level"));
    }

    @Test
    void invalidNameHpXpAndLevelAreRejectedAtLoadBoundary() throws Exception {
        assertInvalid("bad name,WARRIOR,1,0,125,-1,-1,-1", "Hero name");
        assertInvalid("Alice,WARRIOR,1,0,0,-1,-1,-1", "hit point");
        assertInvalid("Alice,WARRIOR,1,0,126,-1,-1,-1", "hit points");
        assertInvalid("Alice,WARRIOR,1,1000,125,-1,-1,-1", "experience");
        assertInvalid("Alice,WARRIOR,101,4510450,1125,-1,-1,-1", "level");
        assertInvalid("Alice,WARRIOR,2147483647,0,125,-1,-1,-1", "level");
    }

    @Test
    void cumulativeXpStateIsAcceptedAndOldResetStyleStateIsRejected() throws Exception {
        Files.writeString(
            file(),
            "Alice,WARRIOR,2,1000,135,-1,-1,-1",
            StandardCharsets.UTF_8
        );
        assertEquals(1_000L, store(file()).load("Alice").getXp());

        Files.writeString(
            file(),
            "Alice,WARRIOR,2,50,135,-1,-1,-1",
            StandardCharsets.UTF_8
        );
        assertThrows(SaveFileCorruptedException.class, () -> store(file()).list());
    }

    @Test
    void saveRejectsInvalidReconstructedState() {
        Hero invalid = Hero.builder("Alice", HeroClass.WARRIOR)
            .xp(1_000L)
            .build();

        IOException exception = assertThrows(IOException.class, () -> store(file()).save(invalid));

        assertTrue(exception.getMessage().contains("experience"));
    }

    @Test
    void saveReplacesExistingHeroAndCleansTemporaryFiles() throws Exception {
        CsvStore store = store(file());
        Hero first = Hero.createNew("Alice", HeroClass.WARRIOR);
        Hero changed = Hero.builder("Alice", HeroClass.WARRIOR)
            .currentHp(115)
            .build();

        store.save(first);
        store.save(changed);

        assertEquals(
            List.of("Alice,WARRIOR,1,0,115,-1,-1,-1"),
            Files.readAllLines(file(), StandardCharsets.UTF_8)
        );
        try (Stream<Path> files = Files.list(tempDir)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    void failedWriteDoesNotLeaveTemporaryFiles() throws Exception {
        Path missingParentFile = tempDir.resolve("missing").resolve("heroes.csv");
        CsvStore store = store(missingParentFile);

        assertThrows(IOException.class, () -> store.save(Hero.createNew("Alice", HeroClass.MAGE)));
        try (Stream<Path> files = Files.list(tempDir)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    private void assertInvalid(String line, String expectedReason) throws Exception {
        Files.writeString(file(), line, StandardCharsets.UTF_8);
        SaveFileCorruptedException exception = assertThrows(
            SaveFileCorruptedException.class,
            () -> store(file()).list()
        );
        assertTrue(exception.getReason().toLowerCase().contains(expectedReason.toLowerCase()));
    }

    private void assertHeroEquals(Hero expected, Hero actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getHeroClass(), actual.getHeroClass());
        assertEquals(expected.getLevel(), actual.getLevel());
        assertEquals(expected.getXp(), actual.getXp());
        assertEquals(expected.getCurrentHp(), actual.getCurrentHp());
        assertEquals(expected.getWeaponMod(), actual.getWeaponMod());
        assertEquals(expected.getArmorMod(), actual.getArmorMod());
        assertEquals(expected.getHelmMod(), actual.getHelmMod());
    }

    private Path file() {
        return tempDir.resolve("heroes.csv");
    }

    private CsvStore store(Path path) {
        return new CsvStore(path, validator);
    }
}

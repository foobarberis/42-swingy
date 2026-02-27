package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.validation.Validation;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeroCsvRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void listFailsOnBlankLineWithLineNumber() throws Exception {
        Path file = tempDir.resolve("heroes.csv");
        Files.write(file, List.of(
                "Alice,WARRIOR,1,0,125,-1,-1,-1",
                "",
                "Bob,ROGUE,1,0,100,-1,-1,-1"
        ), StandardCharsets.UTF_8);

        HeroCsvRepository repo = repository(file);
        SaveFileCorruptedException ex = assertThrows(SaveFileCorruptedException.class, repo::list);
        assertEquals(2, ex.getLineNumber());
    }

    @Test
    void listFailsOnDuplicateNameWithLineNumber() throws Exception {
        Path file = tempDir.resolve("heroes.csv");
        Files.write(file, List.of(
                "Alice,WARRIOR,1,0,125,-1,-1,-1",
                "Alice,ROGUE,1,0,100,-1,-1,-1"
        ), StandardCharsets.UTF_8);

        HeroCsvRepository repo = repository(file);
        SaveFileCorruptedException ex = assertThrows(SaveFileCorruptedException.class, repo::list);
        assertEquals(2, ex.getLineNumber());
    }

    @Test
    void saveReplacesExistingHeroAndLeavesNoTmpFile() throws Exception {
        Path file = tempDir.resolve("heroes.csv");
        HeroCsvRepository repo = repository(file);

        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        repo.save(hero);

        hero.damage(10);
        repo.save(hero);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
        assertEquals("Alice,WARRIOR,1,0,115,-1,-1,-1", lines.get(0));
        assertFalse(Files.exists(tempDir.resolve("heroes.csv.tmp")));
    }

    private HeroCsvRepository repository(Path file) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return new HeroCsvRepository(file, new CsvHeroParser(), new CsvHeroSerializer(), validator);
    }
}

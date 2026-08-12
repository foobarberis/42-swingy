package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvStoreTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @TempDir
    Path directory;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void persistsAndRestoresZeroEquipment() throws Exception {
        Path path = directory.resolve("heroes.csv");
        CsvStore store = new CsvStore(path, validator);
        Hero hero = Hero.restore("Ada", HeroClass.MAGE, 2, 1_000, 7, 0, 0, 0);
        store.save(hero);

        Hero loaded = store.load("Ada");
        assertEquals(0, loaded.getWeaponModifier());
        assertEquals("Ada,MAGE,2,1000,7,0,0,0", Files.readString(path).trim());
    }

    @Test
    void malformedRowsThrowContextualIoException() throws Exception {
        Path path = directory.resolve("heroes.csv");
        Files.writeString(path, "Ada,MAGE,x,0,12,0,0,0\n");
        IOException failure = assertThrows(
            IOException.class,
            () -> new CsvStore(path, validator).list()
        );
        assertTrue(failure.getMessage().contains(
            "Save file heroes.csv is corrupted at line 1: invalid level 'x'."
        ));
    }

    @Test
    void rejectsDuplicateNamesAndInvalidRestoredState() throws Exception {
        Path path = directory.resolve("heroes.csv");
        Files.writeString(
            path,
            "Ada,MAGE,1,0,12,0,0,0\nAda,MAGE,1,0,12,0,0,0\n"
        );
        assertThrows(IOException.class, () -> new CsvStore(path, validator).list());
    }
}

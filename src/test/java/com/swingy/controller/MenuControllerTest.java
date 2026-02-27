package com.swingy.controller;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.persistence.CsvHeroParser;
import com.swingy.persistence.CsvHeroSerializer;
import com.swingy.persistence.HeroCsvRepository;
import com.swingy.support.FakeView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.validation.Validation;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuControllerTest {

    @TempDir
    Path tempDir;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void firstHandleAutoListsHeroes() {
        MenuController controller = new MenuController(repository(tempDir.resolve("heroes.csv")), validator);
        FakeView view = new FakeView().enqueue("list");

        controller.enterMenu();
        controller.handle(view);

        assertTrue(view.outputs().contains("No heroes available."));
    }

    @Test
    void createWithBadSyntaxPrintsUsage() {
        MenuController controller = new MenuController(repository(tempDir.resolve("heroes.csv")), validator);
        FakeView view = new FakeView().enqueue("create warrior");

        MenuController.MenuResult result = controller.handle(view);

        assertEquals(MenuController.MenuResult.Type.NONE, result.type());
        assertTrue(view.outputs().contains("Usage: create warrior|rogue|mage <name>"));
    }

    @Test
    void createDuplicateNamePrintsExpectedError() throws Exception {
        HeroCsvRepository repository = repository(tempDir.resolve("heroes.csv"));
        repository.save(Hero.createNew("Alice", HeroClass.WARRIOR));

        MenuController controller = new MenuController(repository, validator);
        FakeView view = new FakeView().enqueue("create warrior Alice");

        MenuController.MenuResult result = controller.handle(view);

        assertEquals(MenuController.MenuResult.Type.NONE, result.type());
        assertTrue(view.outputs().contains("A character with the name already exists, pick another name."));
    }

    @Test
    void createOnCorruptedSavePrintsLineAwareError() throws Exception {
        Path file = tempDir.resolve("heroes.csv");
        Files.writeString(file, "\n", StandardCharsets.UTF_8);

        MenuController controller = new MenuController(repository(file), validator);
        FakeView view = new FakeView().enqueue("create warrior Bob");

        MenuController.MenuResult result = controller.handle(view);

        assertEquals(MenuController.MenuResult.Type.NONE, result.type());
        assertTrue(view.outputs().stream().anyMatch(s -> s.equals("Save file heroes.csv is corrupted (line 1).")));
    }

    @Test
    void loadFailurePrintsCouldNotLoadSave() {
        MenuController controller = new MenuController(repository(tempDir.resolve("heroes.csv")), validator);
        FakeView view = new FakeView().enqueue("load Missing");

        MenuController.MenuResult result = controller.handle(view);

        assertEquals(MenuController.MenuResult.Type.NONE, result.type());
        assertTrue(view.outputs().contains("Could not load save."));
    }

    private HeroCsvRepository repository(Path file) {
        return new HeroCsvRepository(file, new CsvHeroParser(), new CsvHeroSerializer(), validator);
    }
}

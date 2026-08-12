package com.swingy.controller;

import com.swingy.logic.CombatService;
import com.swingy.logic.EncounterService;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import com.swingy.persistence.CsvStore;
import com.swingy.persistence.HeroRepository;
import com.swingy.support.FakeView;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationControllerTest {
    @TempDir
    Path tempDir;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void firstMenuEntryListsHeroes() {
        FakeView view = new FakeView();

        application(view, store(), openRoom(), repositoryRandom()).run();

        assertTrue(view.outputs().contains("No heroes available."));
    }

    @Test
    void invalidHeroNameShowsModelValidationMessage() {
        FakeView view = new FakeView().enqueue("create warrior bad!", "quit");

        application(view, store(), openRoom(), repositoryRandom()).run();

        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("1 to 16")));
    }

    @Test
    void duplicateNamesAreRejected() throws Exception {
        CsvStore repository = store();
        repository.save(Hero.createNew("Alice", HeroClass.WARRIOR));
        FakeView view = new FakeView().enqueue("create mage Alice", "quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("already exists")));
    }

    @Test
    void missionVictorySavesFullyHealedHeroAndReturnsToMenu() throws Exception {
        CsvStore repository = store();
        repository.save(
            Hero.builder("Alice", HeroClass.WARRIOR).currentHp(50).build()
        );
        FakeView view = new FakeView().enqueue("load Alice", "north", "north", "quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertEquals(125, repository.load("Alice").getCurrentHp());
        assertTrue(view.outputs().contains("Progress saved."));
        assertTrue(
            outputAfter(view.outputs(), "Victory! You reached the border.", "STATUS:Main menu")
        );
    }

    @Test
    void deadHeroIsDeletedAfterConfirmedDeath() throws Exception {
        CsvStore repository = store();
        repository.save(Hero.createNew("Alice", HeroClass.WARRIOR));
        FakeView view = new FakeView().enqueue("load Alice", "east", "fight", "quit");
        Room room = roomWithEnemy(
            new Enemy("Ogre", 1, 1_000, 0, 200, new Position(3, 2))
        );

        application(view, repository, room, repositoryRandom()).run();

        assertTrue(repository.list().isEmpty());
        assertTrue(view.outputs().contains("You died."));
        assertTrue(view.outputs().contains("Your hero has been removed."));
    }

    @Test
    void quitDuringMissionSavesBeforeClaimingSuccess() throws Exception {
        CsvStore repository = store();
        repository.save(
            Hero.builder("Alice", HeroClass.WARRIOR).currentHp(111).build()
        );
        FakeView view = new FakeView().enqueue("load Alice", "quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertEquals(111, repository.load("Alice").getCurrentHp());
        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("has been saved")));
    }

    @Test
    void listFailureIsReportedAndNotDisguisedAsAnEmptyList() {
        FailingRepository repository = new FailingRepository(Failure.LIST);
        FakeView view = new FakeView().enqueue("quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("Could not list")));
        assertFalse(view.outputs().contains("No heroes available."));
    }

    @Test
    void loadFailureNamesTheFailedOperation() {
        FailingRepository repository = new FailingRepository(Failure.LOAD);
        FakeView view = new FakeView().enqueue("load Alice", "quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("Could not load")));
    }

    @Test
    void createSaveFailureDoesNotStartMissionOrClaimSuccess() {
        FailingRepository repository = new FailingRepository(Failure.SAVE);
        FakeView view = new FakeView().enqueue("create warrior Alice", "quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("Could not save")));
        assertFalse(view.outputs().stream().anyMatch(line -> line.contains("has been saved")));
        assertFalse(view.outputs().stream().anyMatch(line -> line.startsWith("MAP:")));
    }

    @Test
    void failedDeleteNeverClaimsDeadHeroWasRemoved() {
        FailingRepository repository = new FailingRepository(Failure.DELETE);
        repository.heroes.add(Hero.createNew("Alice", HeroClass.WARRIOR));
        FakeView view = new FakeView().enqueue("load Alice", "east", "fight", "quit");
        Room room = roomWithEnemy(
            new Enemy("Ogre", 1, 1_000, 0, 200, new Position(3, 2))
        );

        application(view, repository, room, repositoryRandom()).run();

        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("Could not remove")));
        assertFalse(view.outputs().contains("Your hero has been removed."));
    }

    @Test
    void failedExitSaveIsVisibleAndNeverReportedAsSaved() {
        FailingRepository repository = new FailingRepository(Failure.SAVE);
        repository.heroes.add(Hero.createNew("Alice", HeroClass.WARRIOR));
        FakeView view = new FakeView().enqueue("load Alice", "quit");

        application(view, repository, openRoom(), repositoryRandom()).run();

        assertTrue(
            view.outputs().stream().anyMatch(line -> line.contains("could not be saved"))
        );
        assertFalse(view.outputs().stream().anyMatch(line -> line.contains("has been saved")));
    }

    private ApplicationController application(
        FakeView view,
        HeroRepository repository,
        Room room,
        SequenceRandom random
    ) {
        MissionController mission = new MissionController(
            view,
            view,
            ignored -> room,
            new EncounterService(new CombatService(), random)
        );
        return new ApplicationController(view, view, repository, validator, mission);
    }

    private CsvStore store() {
        return new CsvStore(tempDir.resolve("heroes.csv"), validator);
    }

    private Room openRoom() {
        return new Room(5, new Position(2, 2));
    }

    private Room roomWithEnemy(Enemy enemy) {
        Room room = openRoom();
        room.addEnemy(enemy);
        return room;
    }

    private SequenceRandom repositoryRandom() {
        return new SequenceRandom(List.of(), List.of());
    }

    private boolean outputAfter(List<String> outputs, String first, String second) {
        boolean foundFirst = false;
        for (String output : outputs) {
            if (!foundFirst && output.contains(first)) {
                foundFirst = true;
            } else if (foundFirst && output.contains(second)) {
                return true;
            }
        }
        return false;
    }

    private enum Failure {
        LIST,
        LOAD,
        SAVE,
        DELETE
    }

    private static final class FailingRepository implements HeroRepository {
        private final Failure failure;
        private final List<Hero> heroes = new ArrayList<>();

        private FailingRepository(Failure failure) {
            this.failure = failure;
        }

        @Override
        public List<Hero> list() throws IOException {
            fail(Failure.LIST);
            return heroes.stream().map(Hero::copy).toList();
        }

        @Override
        public Hero load(String name) throws IOException {
            fail(Failure.LOAD);
            return heroes.stream()
                .filter(hero -> hero.getName().equals(name))
                .findFirst()
                .map(Hero::copy)
                .orElseThrow(() -> new IOException("missing hero"));
        }

        @Override
        public void save(Hero hero) throws IOException {
            fail(Failure.SAVE);
            heroes.removeIf(existing -> existing.getName().equals(hero.getName()));
            heroes.add(hero.copy());
        }

        @Override
        public void delete(String name) throws IOException {
            fail(Failure.DELETE);
            heroes.removeIf(hero -> hero.getName().equals(name));
        }

        private void fail(Failure operation) throws IOException {
            if (failure == operation) {
                throw new IOException("deliberate " + operation.name().toLowerCase() + " failure");
            }
        }
    }
}

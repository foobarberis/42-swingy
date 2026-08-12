package com.swingy.controller;

import com.swingy.logic.EncounterService;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Room;
import com.swingy.persistence.HeroRepository;
import com.swingy.support.FakeView;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationControllerTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidation() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidation() {
        validatorFactory.close();
    }

    @Test
    void listIsExplicitAndNotAutomatic() {
        FakeView view = new FakeView("list", "quit");
        MemoryRepository repository = new MemoryRepository();
        repository.heroes.add(Hero.createNew("Ada", HeroClass.MAGE));
        controller(view, repository).run();

        assertEquals(1, repository.listCalls);
        assertTrue(view.renderedText().contains("Name: Ada"));
        assertTrue(view.isClosed());
    }

    @Test
    void eofDuringMissionStillSavesLivingHero() {
        FakeView view = new FakeView("load Ada");
        MemoryRepository repository = new MemoryRepository();
        repository.heroes.add(Hero.createNew("Ada", HeroClass.WARRIOR));
        controller(view, repository).run();

        assertEquals(1, repository.saveCalls);
        assertTrue(view.renderedText().contains("Your progress has been saved. Goodbye!"));
    }

    @Test
    void menuEofNeedsNoSave() {
        FakeView view = new FakeView();
        MemoryRepository repository = new MemoryRepository();
        controller(view, repository).run();
        assertEquals(0, repository.saveCalls);
        assertTrue(view.renderedText().contains("Goodbye!"));
    }

    private ApplicationController controller(FakeView view, MemoryRepository repository) {
        MissionController mission = new MissionController(
            view,
            ignored -> new Room(9),
            new EncounterService(new SequenceRandom(List.of(), List.of()))
        );
        return new ApplicationController(view, repository, validator, mission);
    }

    private static final class MemoryRepository implements HeroRepository {
        private final List<Hero> heroes = new ArrayList<>();
        private int listCalls;
        private int saveCalls;

        @Override
        public List<Hero> list() {
            listCalls++;
            return List.copyOf(heroes);
        }

        @Override
        public Hero load(String name) throws IOException {
            return heroes.stream()
                .filter(hero -> hero.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IOException("not found"));
        }

        @Override
        public void save(Hero hero) {
            saveCalls++;
            heroes.removeIf(saved -> saved.getName().equals(hero.getName()));
            heroes.add(hero);
        }

        @Override
        public void delete(String name) {
            heroes.removeIf(hero -> hero.getName().equals(name));
        }
    }
}

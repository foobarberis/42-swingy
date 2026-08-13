package com.swingy.controller;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.persistence.HeroRepository;
import com.swingy.view.View;
import com.swingy.view.ViewFormatter;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ApplicationController {
    private static final String MENU = "Available commands: list, create warrior|rogue|mage <name>, load <name>, quit.";

    private final View view;
    private final HeroRepository repository;
    private final Validator validator;
    private final MissionController mission;

    public ApplicationController(
        View view,
        HeroRepository repository,
        Validator validator,
        MissionController mission
    ) {
        this.view = Objects.requireNonNull(view, "View is required.");
        this.repository = Objects.requireNonNull(repository, "Hero repository is required.");
        this.validator = Objects.requireNonNull(validator, "Validator is required.");
        this.mission = Objects.requireNonNull(mission, "Mission controller is required.");
    }

    public void run() {
        try {
            view.show("Welcome to Swingy! " + MENU);
            while (true) {
                String input = view.readInput();
                if (input == null || "quit".equals(input.trim().toLowerCase(Locale.ROOT))) {
                    view.show("Goodbye!");
                    return;
                }
                String line = input.trim();
                if (!line.isEmpty() && handleMenuCommand(line)) {
                    return;
                }
            }
        } finally {
            view.close();
        }
    }

    private boolean handleMenuCommand(String line) {
        String[] parts = line.split("\\s+");
        return switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "list" -> {
                if (parts.length != 1) {
                    view.show("Usage: list");
                } else {
                    listHeroes();
                }
                yield false;
            }
            case "create" -> createHero(parts);
            case "load" -> loadHero(parts);
            default -> {
                view.show("Unknown command. " + MENU);
                yield false;
            }
        };
    }

    private void listHeroes() {
        try {
            List<Hero> heroes = repository.list();
            if (heroes.isEmpty()) {
                view.show("No heroes available.");
            } else {
                heroes.forEach(hero -> view.show(ViewFormatter.heroStatus(hero)));
            }
        } catch (IOException exception) {
            view.show("Could not list saved heroes: " + detail(exception));
        }
    }

    private boolean createHero(String[] parts) {
        if (parts.length != 3) {
            view.show("Usage: create warrior|rogue|mage <name>");
            return false;
        }

        HeroClass heroClass = HeroClass.fromCreateToken(parts[1].toLowerCase(Locale.ROOT));
        if (heroClass == null) {
            view.show("Unknown hero class. Choose warrior, rogue, or mage.");
            return false;
        }

        Hero hero = Hero.createNew(parts[2], heroClass);
        List<String> errors = validator.validate(hero).stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .toList();
        if (!errors.isEmpty()) {
            errors.forEach(error -> view.show("Validation failed: " + error));
            return false;
        }

        List<Hero> existing;
        try {
            existing = repository.list();
        } catch (IOException exception) {
            view.show("Could not list saved heroes: " + detail(exception));
            return false;
        }
        if (existing.stream().anyMatch(saved -> saved.getName().equals(hero.getName()))) {
            view.show("A character with that name already exists. Pick another name.");
            return false;
        }

        try {
            repository.save(hero);
        } catch (IOException exception) {
            view.show("Could not save hero '" + hero.getName() + "': " + detail(exception));
            return false;
        }
        return finishMission(hero, mission.play(hero));
    }

    private boolean loadHero(String[] parts) {
        if (parts.length != 2) {
            view.show("Usage: load <name>");
            return false;
        }

        Hero hero;
        try {
            hero = repository.load(parts[1]);
        } catch (IOException exception) {
            view.show("Could not load hero '" + parts[1] + "': " + detail(exception));
            return false;
        }
        return finishMission(hero, mission.play(hero));
    }

    private boolean finishMission(Hero hero, MissionController.Result result) {
        return switch (result) {
            case WON -> {
                hero.healToFull();
                view.show("Your health has been fully restored.");
                try {
                    repository.save(hero);
                    view.show("Progress saved.");
                } catch (IOException exception) {
                    view.show("Could not save hero '" + hero.getName() + "': " + detail(exception));
                }
                yield false;
            }
            case HERO_DIED -> {
                view.show("You died.");
                try {
                    repository.delete(hero.getName());
                    view.show("Your hero has been removed.");
                } catch (IOException exception) {
                    view.show("Could not remove hero '" + hero.getName() + "': " + detail(exception));
                }
                yield false;
            }
            case EXIT_APPLICATION -> {
                try {
                    repository.save(hero);
                    view.show("Your progress has been saved. Goodbye!");
                } catch (IOException exception) {
                    view.show("Your progress could not be saved: " + detail(exception));
                }
                yield true;
            }
        };
    }

    private String detail(IOException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

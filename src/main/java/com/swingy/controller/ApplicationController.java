package com.swingy.controller;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.persistence.HeroRepository;
import com.swingy.view.ExitReport;
import com.swingy.view.GameView;
import com.swingy.view.InputPort;
import com.swingy.view.RepositoryAction;
import com.swingy.view.ViewInput;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ApplicationController {
    private final InputPort input;
    private final GameView view;
    private final HeroRepository repository;
    private final Validator validator;
    private final MissionController mission;

    private boolean listOnMenuEntry = true;

    public ApplicationController(
        InputPort input,
        GameView view,
        HeroRepository repository,
        Validator validator,
        MissionController mission
    ) {
        this.input = Objects.requireNonNull(input, "Input is required.");
        this.view = Objects.requireNonNull(view, "View is required.");
        this.repository = Objects.requireNonNull(repository, "Hero repository is required.");
        this.validator = Objects.requireNonNull(validator, "Validator is required.");
        this.mission = Objects.requireNonNull(mission, "Mission controller is required.");
    }

    public void run() {
        try {
            view.showWelcome();
            boolean exit = false;
            while (!exit) {
                view.renderMenu();
                if (listOnMenuEntry) {
                    listHeroes();
                    listOnMenuEntry = false;
                }

                ViewInput nextInput = input.readInput();
                if (nextInput.type() != ViewInput.Type.LINE) {
                    view.showExit(exitReport(nextInput, ExitReport.SaveState.NOT_REQUIRED, null));
                    break;
                }
                String line = nextInput.line().trim();
                if (line.isEmpty()) {
                    continue;
                }
                if ("quit".equals(line.toLowerCase(Locale.ROOT))) {
                    view.showExit(exitReport(nextInput, ExitReport.SaveState.NOT_REQUIRED, null));
                    break;
                }
                exit = handleMenuCommand(line);
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
                    view.showListUsage();
                } else {
                    listHeroes();
                }
                yield false;
            }
            case "create" -> createHero(parts);
            case "load" -> loadHero(parts);
            default -> {
                view.showUnknownMenuCommand();
                yield false;
            }
        };
    }

    private void listHeroes() {
        try {
            List<Hero> heroes = repository.list();
            if (heroes.isEmpty()) {
                view.showNoHeroes();
                return;
            }
            for (Hero hero : heroes) {
                view.renderHeroSummary(hero);
            }
        } catch (IOException exception) {
            view.showRepositoryFailure(RepositoryAction.LIST, null, exception.getMessage());
        }
    }

    private boolean createHero(String[] parts) {
        if (parts.length != 3) {
            view.showCreateUsage();
            return false;
        }

        HeroClass heroClass = HeroClass.fromCreateToken(parts[1].toLowerCase(Locale.ROOT));
        if (heroClass == null) {
            view.showUnknownHeroClass();
            return false;
        }

        Hero hero;
        try {
            hero = Hero.createNew(parts[2], heroClass);
        } catch (IllegalArgumentException exception) {
            view.showValidationErrors(List.of(exception.getMessage()));
            return false;
        }
        List<String> validationErrors = validator.validate(hero).stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .toList();
        if (!validationErrors.isEmpty()) {
            view.showValidationErrors(validationErrors);
            return false;
        }

        List<Hero> existingHeroes;
        try {
            existingHeroes = repository.list();
        } catch (IOException exception) {
            view.showRepositoryFailure(RepositoryAction.LIST, null, exception.getMessage());
            return false;
        }
        for (Hero existing : existingHeroes) {
            if (existing.getName().equals(hero.getName())) {
                view.showDuplicateName();
                return false;
            }
        }
        try {
            repository.save(hero);
        } catch (IOException exception) {
            view.showRepositoryFailure(RepositoryAction.SAVE, hero.getName(), exception.getMessage());
            return false;
        }

        return finishMission(hero, mission.play(hero));
    }

    private boolean loadHero(String[] parts) {
        if (parts.length != 2) {
            view.showLoadUsage();
            return false;
        }

        Hero hero;
        try {
            hero = repository.load(parts[1]);
        } catch (IOException exception) {
            view.showRepositoryFailure(RepositoryAction.LOAD, parts[1], exception.getMessage());
            return false;
        }
        return finishMission(hero, mission.play(hero));
    }

    private boolean finishMission(Hero hero, MissionResult result) {
        listOnMenuEntry = true;
        return switch (result.type()) {
            case WON -> {
                try {
                    repository.save(hero);
                    view.showProgressSaved();
                } catch (IOException exception) {
                    view.showRepositoryFailure(
                        RepositoryAction.SAVE,
                        hero.getName(),
                        exception.getMessage()
                    );
                }
                yield false;
            }
            case HERO_DIED -> {
                view.showHeroDied();
                try {
                    repository.delete(hero.getName());
                    view.showHeroDeleted();
                } catch (IOException exception) {
                    view.showRepositoryFailure(
                        RepositoryAction.DELETE,
                        hero.getName(),
                        exception.getMessage()
                    );
                }
                yield false;
            }
            case EXIT_APPLICATION -> {
                ExitReport.SaveState saveState;
                String saveFailure = null;
                try {
                    repository.save(hero);
                    saveState = ExitReport.SaveState.SAVED;
                } catch (IOException exception) {
                    saveState = ExitReport.SaveState.FAILED;
                    saveFailure = exception.getMessage();
                }
                view.showExit(exitReport(result.exitInput(), saveState, saveFailure));
                yield true;
            }
        };
    }

    private ExitReport exitReport(
        ViewInput input,
        ExitReport.SaveState saveState,
        String saveFailure
    ) {
        ExitReport.Reason reason = switch (input.type()) {
            case LINE -> ExitReport.Reason.QUIT;
            case END_OF_INPUT -> ExitReport.Reason.END_OF_INPUT;
            case VIEW_CLOSED -> ExitReport.Reason.VIEW_CLOSED;
            case FAILURE -> ExitReport.Reason.INPUT_FAILURE;
        };
        String inputFailure = input.failure();
        return new ExitReport(reason, saveState, inputFailure, saveFailure);
    }
}

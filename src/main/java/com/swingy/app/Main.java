package com.swingy.app;

import com.swingy.controller.ApplicationController;
import com.swingy.controller.MissionController;
import com.swingy.logic.CombatService;
import com.swingy.logic.EncounterService;
import com.swingy.logic.RandomRoomFactory;
import com.swingy.persistence.CsvStore;
import com.swingy.persistence.HeroRepository;
import com.swingy.view.View;
import com.swingy.view.console.ConsoleView;
import com.swingy.view.swing.SwingView;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.nio.file.Path;
import java.util.Random;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args) {
        if (args.length != 1 || (!"console".equals(args[0]) && !"gui".equals(args[0]))) {
            System.err.println("Usage: java -jar swingy.jar console|gui");
            return 1;
        }

        View view;
        try {
            view = "console".equals(args[0]) ? new ConsoleView() : new SwingView();
        } catch (RuntimeException exception) {
            System.err.println("Could not start the " + args[0] + " view: " + exception.getMessage());
            return 1;
        }

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            HeroRepository repository = new CsvStore(Path.of("heroes.csv"), validator);
            Random random = new Random();
            MissionController mission = new MissionController(
                view,
                view,
                new RandomRoomFactory(random),
                new EncounterService(new CombatService(), random)
            );
            new ApplicationController(view, view, repository, validator, mission).run();
            return 0;
        } catch (RuntimeException exception) {
            view.close();
            System.err.println("Swingy stopped unexpectedly: " + exception.getMessage());
            return 1;
        }
    }
}

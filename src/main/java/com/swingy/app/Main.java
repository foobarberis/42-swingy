package com.swingy.app;

import com.swingy.controller.AppController;
import com.swingy.controller.CombatController;
import com.swingy.controller.GameController;
import com.swingy.controller.MenuController;
import com.swingy.model.combat.CombatResolver;
import com.swingy.model.world.EntityPlacer;
import com.swingy.model.world.MazeGenerator;
import com.swingy.persistence.CsvHeroParser;
import com.swingy.persistence.CsvHeroSerializer;
import com.swingy.persistence.HeroCsvRepository;
import com.swingy.persistence.HeroRepository;
import com.swingy.util.DefaultRandomProvider;
import com.swingy.util.RandomProvider;
import com.swingy.view.View;
import com.swingy.view.console.ConsoleView;
import com.swingy.view.swing.SwingView;

import javax.validation.Validation;
import javax.validation.Validator;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1 || (!"console".equals(args[0]) && !"gui".equals(args[0]))) {
            System.err.println("Usage: java -jar swingy.jar console|gui");
            System.exit(1);
        }

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        HeroRepository repository = new HeroCsvRepository(Path.of("heroes.csv"), new CsvHeroParser(), new CsvHeroSerializer(), validator);
        RandomProvider rng = new DefaultRandomProvider();
        View view = "console".equals(args[0]) ? new ConsoleView() : new SwingView();

        MenuController menuController = new MenuController(repository, validator);
        CombatController combatController = new CombatController(new CombatResolver(), rng);
        GameController gameController = new GameController(new MazeGenerator(rng), new EntityPlacer(rng), combatController, repository, rng);
        AppController appController = new AppController(view, menuController, gameController);
        appController.run();
    }
}

package com.swingy.controller;

import com.swingy.model.Hero;
import com.swingy.view.View;

public class AppController {
    private final View view;
    private final MenuController menuController;
    private final GameController gameController;

    public AppController(View view, MenuController menuController, GameController gameController) {
        this.view = view;
        this.menuController = menuController;
        this.gameController = gameController;
    }

    public void run() {
		view.println("\nWelcome to Swingy! Usage: `create <class> <name>` or `load <name>`.\n");
        while (!view.isClosed()) {
            MenuController.MenuResult menu = menuController.handle(view);
            if (menu.type() == MenuController.MenuResult.Type.EXIT) {
                break;
            }
            if (menu.type() == MenuController.MenuResult.Type.START) {
                Hero hero = menu.hero();
                MissionResult result = gameController.runMission(view, hero);
                if (result == MissionResult.EXIT_APP) {
                    break;
                }
            }
        }
        view.close();
    }
}

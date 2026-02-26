package com.swingy.controller;

import java.util.List;

import javax.validation.Validator;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.persistence.HeroRepository;
import com.swingy.persistence.SaveFileCorruptedException;
import com.swingy.view.View;

public class MenuController {
    private final HeroRepository repository;
    private final Validator validator;
    private boolean needsAutoList = true;

    public MenuController(HeroRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public void enterMenu() {
        needsAutoList = true;
    }

    public MenuResult handle(View view) {
        if (needsAutoList) {
            listHeroes(view);
            needsAutoList = false;
        }
        String line = view.readLine();
        if (line == null) {
            if (view.consumeQuitAttempt()) {
                view.println("You cannot quit now.");
                return MenuResult.none();
            }
            return MenuResult.exit();
        }
        if (view.isClosed()) {
            return MenuResult.exit();
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return MenuResult.none();
        }
        return switch (parts[0]) {
            case "list" -> {
                listHeroes(view);
                yield MenuResult.none();
            }
            case "create" -> create(view, parts);
            case "load" -> load(view, parts);
            default -> MenuResult.none();
        };
    }

    private MenuResult create(View view, String[] parts) {
        if (parts.length != 3) {
            view.println("Usage: create warrior|rogue|mage <name>");
            return MenuResult.none();
        }
        HeroClass cls = HeroClass.fromCreateToken(parts[1]);
        if (cls == null) {
            view.println("Usage: create warrior|rogue|mage <name>");
            return MenuResult.none();
        }
        String name = parts[2];
        if (!name.matches("[A-Za-z0-9_-]{1,16}")) {
            view.println("Usage: create warrior|rogue|mage <name>");
            return MenuResult.none();
        }
        try {
            List<Hero> heroes = repository.list();
            if (heroes.stream().anyMatch(h -> h.getName().equals(name))) {
                view.println("A character with the name already exists, pick another name.");
                return MenuResult.none();
            }
            Hero hero = Hero.createNew(name, cls);
            if (!validator.validate(hero).isEmpty()) return MenuResult.none();
            repository.save(hero);
            return MenuResult.start(hero);
        } catch (SaveFileCorruptedException e) {
            printCorruptedSaveMessage(view, e);
            return MenuResult.none();
        } catch (Exception ignored) {
            return MenuResult.none();
        }
    }

    private MenuResult load(View view, String[] parts) {
        if (parts.length != 2) {
            view.println("Could not load save.");
            return MenuResult.none();
        }
        try {
            Hero hero = repository.loadByName(parts[1]);
            if (!validator.validate(hero).isEmpty()) throw new IllegalStateException();
            return MenuResult.start(hero);
        } catch (Exception e) {
            view.println("Could not load save.");
            return MenuResult.none();
        }
    }

    private void listHeroes(View view) {
        try {
            List<Hero> heroes = repository.list();
            if (heroes.isEmpty()) {
                view.println("No heroes available.");
                return;
            }
            view.println("Available heroes:");
            for (Hero h : heroes) {
                view.println("  - " + h.getName() + " " + h.getHeroClass().name() + " Lv." + h.getLevel());
            }
        } catch (SaveFileCorruptedException e) {
            printCorruptedSaveMessage(view, e);
        } catch (Exception e) {
            view.println("No heroes available.");
        }
    }

    private void printCorruptedSaveMessage(View view, SaveFileCorruptedException e) {
        view.println("Save file " + e.getPath().getFileName() + " is corrupted (line " + e.getLineNumber() + ").");
    }

    public record MenuResult(Type type, Hero hero) {
        enum Type { NONE, START, EXIT }

        static MenuResult none() { return new MenuResult(Type.NONE, null); }
        static MenuResult start(Hero hero) { return new MenuResult(Type.START, hero); }
        static MenuResult exit() { return new MenuResult(Type.EXIT, null); }
    }
}

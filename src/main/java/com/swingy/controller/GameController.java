package com.swingy.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.swingy.model.Hero;
import com.swingy.model.world.EnemyInstance;
import com.swingy.model.world.EntityPlacer;
import com.swingy.model.world.FogOfWar;
import com.swingy.model.world.Maze;
import com.swingy.model.world.MazeGenerator;
import com.swingy.model.world.Position;
import com.swingy.model.world.TileType;
import com.swingy.persistence.HeroRepository;
import com.swingy.util.RandomProvider;
import com.swingy.view.View;

public class GameController {
    private final MazeGenerator mazeGenerator;
    private final EntityPlacer entityPlacer;
    private final CombatController combatController;
    private final HeroRepository repository;
    private final RandomProvider rng;
    private final FogOfWar fogOfWar = new FogOfWar();

    public GameController(MazeGenerator mazeGenerator, EntityPlacer entityPlacer, CombatController combatController, HeroRepository repository, RandomProvider rng) {
        this.mazeGenerator = mazeGenerator;
        this.entityPlacer = entityPlacer;
        this.combatController = combatController;
        this.repository = repository;
        this.rng = rng;
    }

    public MissionResult runMission(View view, Hero hero) {
        int mapSizeRaw = (hero.getLevel() - 1) * 5 + 10 - (hero.getLevel() % 2);
        int size = Math.min(mapSizeRaw, 55);

        Maze maze = mazeGenerator.generate(size);
        entityPlacer.placePotion(maze);
        entityPlacer.placeEnemies(maze, hero.getLevel());

        Position heroPos = maze.heroStart();
        view.println("\nAvailable commands: north (n), south (s), east (e), west (w).\nMap symbols: '@' (player), 'M' (monster), 'U' (unique monster), '!' (potion), 'X' (exit), '.' (floor), '#' (wall).\n");

        while (true) {
            if (view.isClosed()) {
                saveSafely(hero);
                return MissionResult.EXIT_APP;
            }

            view.renderStatus(hero.statusLine());
            view.renderMap(fogOfWar.viewport(maze, heroPos));
            String cmd = view.readLine();
            if (cmd == null) {
                if (view.consumeQuitAttempt()) {
                    view.println("You cannot quit now.");
                    continue;
                }
                saveSafely(hero);
                return MissionResult.EXIT_APP;
            }
            if (view.isClosed()) {
                saveSafely(hero);
                return MissionResult.EXIT_APP;
            }
            cmd = cmd.trim().toLowerCase();

            Position turnStart = heroPos;

            switch (cmd) {
                case "north", "n", "south", "s", "east", "e", "west", "w" -> {
                    Position dst = moveTarget(heroPos, cmd);
                    if (!maze.isInside(dst) || maze.terrainAt(dst) == TileType.WALL) {
                        view.println("You cannot go there.\n");
                        continue;
                    }

                    heroPos = dst;
                    if (maze.potionPos() != null && heroPos.equals(maze.potionPos())) {
                        drinkPotion(view, hero, maze);
                    }
                    EnemyInstance steppedEnemy = enemyAt(maze, heroPos);
                    if (maze.terrainAt(heroPos) == TileType.EXIT) {
                        view.println("Victory! You escaped the maze.\n");
                        saveSafely(hero);
                        return MissionResult.RETURN_MENU;
                    }
                    if (steppedEnemy != null) {
                        EncounterResult result = encounter(view, hero, steppedEnemy);
                        if (result == EncounterResult.HERO_DIED) {
                            view.println("You died. Your hero has been removed.\n");
                            deleteSafely(hero.getName());
                            return MissionResult.RETURN_MENU;
                        }
                        if (result == EncounterResult.ENEMY_DEFEATED) {
                            maze.enemies().remove(steppedEnemy);
                        }
                        if (result == EncounterResult.ESCAPED) {
                            heroPos = turnStart;
                        }
                        continue;
                    }
                }
                default -> {
                    view.println("Unknown command. Available commands: north (n), south (s), east (e), west (w).\n");
                    continue;
                }
            }

            for (EnemyInstance enemy : new ArrayList<>(maze.enemies())) {
                if (!rng.chance(0.25)) continue;
                List<Position> options = validEnemyMoves(maze, enemy, heroPos);
                if (options.isEmpty()) continue;
                Position newPos = options.get(rng.nextInt(options.size()));
                enemy.moveTo(newPos);
            }
        }
    }

    private void drinkPotion(View view, Hero hero, Maze maze) {
        while (true) {
            view.println("You have found a health potion, do you want to drink it [Y/n]?");
            String in = view.readLine();
            if (in == null) return;
            String a = in.trim().toLowerCase();
            if (a.isEmpty() || a.equals("y")) {
                hero.heal(hero.baseMaxHp() / 2);
                maze.setPotionPos(null);
                return;
            }
            if (a.equals("n")) return;
            view.println("Please answer with y or n.");
        }
    }

    private EncounterResult encounter(View view, Hero hero, EnemyInstance enemy) {
        view.setQuitLocked(true);
        try {
            while (true) {
                view.println("You have encountered " + enemy.enemy().getName() + ", do you want to fight [Y/n]?");
                String in = view.readLine();
                if (in == null) {
                    if (view.consumeQuitAttempt()) {
                        view.println("You cannot quit now.");
                        continue;
                    }
                    return EncounterResult.HERO_DIED;
                }
                String a = in.trim().toLowerCase();
                if (a.isEmpty() || a.equals("y")) {
                    return combatController.fight(view, hero, enemy.enemy()) ? EncounterResult.ENEMY_DEFEATED : EncounterResult.HERO_DIED;
                }
                if (a.equals("n")) {
                    if (rng.chance(0.5)) {
                        return EncounterResult.ESCAPED;
                    }
                    return combatController.fight(view, hero, enemy.enemy()) ? EncounterResult.ENEMY_DEFEATED : EncounterResult.HERO_DIED;
                }
                view.println("Please answer with y or n.");
            }
        } finally {
            view.setQuitLocked(false);
        }
    }

    private List<Position> validEnemyMoves(Maze maze, EnemyInstance enemy, Position heroPos) {
        List<Position> out = new ArrayList<>();
        for (Position n : enemy.pos().neighbors4()) {
            if (!maze.isInside(n)) continue;
            if (maze.terrainAt(n) == TileType.WALL || maze.terrainAt(n) == TileType.EXIT) continue;
            if (maze.potionPos() != null && maze.potionPos().equals(n)) continue;
            if (n.equals(heroPos)) continue;
            boolean occupied = false;
            for (EnemyInstance other : maze.enemies()) {
                if (other != enemy && other.pos().equals(n)) {
                    occupied = true;
                    break;
                }
            }
            if (occupied) continue;
            out.add(n);
        }
        return out;
    }

    private EnemyInstance enemyAt(Maze maze, Position p) {
        for (EnemyInstance e : maze.enemies()) {
            if (e.pos().equals(p)) return e;
        }
        return null;
    }

    private Position moveTarget(Position pos, String cmd) {
        return switch (cmd) {
            case "north", "n" -> new Position(pos.x(), pos.y() - 1);
            case "south", "s" -> new Position(pos.x(), pos.y() + 1);
            case "east", "e" -> new Position(pos.x() + 1, pos.y());
            case "west", "w" -> new Position(pos.x() - 1, pos.y());
            default -> pos;
        };
    }

    private void saveSafely(Hero hero) {
        try {
            repository.save(hero);
        } catch (IOException ignored) {
        }
    }

    private void deleteSafely(String name) {
        try {
            repository.deleteByName(name);
        } catch (IOException ignored) {
        }
    }

    private enum EncounterResult {
        ENEMY_DEFEATED,
        ESCAPED,
        HERO_DIED
    }

    public enum MissionResult {
        RETURN_MENU,
        EXIT_APP
    }
}

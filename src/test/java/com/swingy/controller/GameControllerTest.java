package com.swingy.controller;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.combat.CombatResolver;
import com.swingy.model.world.EnemyInstance;
import com.swingy.model.world.EntityPlacer;
import com.swingy.model.world.Maze;
import com.swingy.model.world.MazeGenerator;
import com.swingy.model.world.Position;
import com.swingy.model.world.TileType;
import com.swingy.support.FakeView;
import com.swingy.support.InMemoryHeroRepository;
import com.swingy.util.RandomProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    @Test
    void victoryFlowSavesHeroAndReturnsToMenu() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        InMemoryHeroRepository repository = new InMemoryHeroRepository();
        FakeView view = new FakeView().enqueue("east");

        Maze maze = emptyMaze(5);
        maze.setTerrain(maze.heroStart(), TileType.FLOOR);
        maze.setTerrain(new Position(3, 2), TileType.EXIT);

        GameController controller = new GameController(
                new FixedMazeGenerator(maze),
                new NoOpEntityPlacer(),
                new StubCombatController(true),
                repository,
                new NonMovingRandomProvider()
        );

        MissionResult result = controller.runMission(view, hero);

        assertEquals(MissionResult.RETURN_MENU, result);
        assertEquals(1, repository.saveCalls);
        assertTrue(view.outputs().contains("Victory! You escaped the maze.\n"));
    }

    @Test
    void deathFlowDeletesHeroAndReturnsToMenu() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        InMemoryHeroRepository repository = new InMemoryHeroRepository();
        FakeView view = new FakeView().enqueue("east", "y");

        Maze maze = emptyMaze(5);
        Enemy enemy = new Enemy("Goblin", false, 1, 15, 15, 100);
        maze.enemies().add(new EnemyInstance(enemy, new Position(3, 2)));

        GameController controller = new GameController(
                new FixedMazeGenerator(maze),
                new NoOpEntityPlacer(),
                new StubCombatController(false),
                repository,
                new NonMovingRandomProvider()
        );

        MissionResult result = controller.runMission(view, hero);

        assertEquals(MissionResult.RETURN_MENU, result);
        assertEquals(1, repository.deleteCalls);
        assertEquals("Alice", repository.lastDeletedName);
        assertTrue(view.outputs().contains("You died. Your hero has been removed.\n"));
    }

    @Test
    void potionPromptHealsAndGracefulExitSaves() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        hero.damage(80); // 125 -> 45

        InMemoryHeroRepository repository = new InMemoryHeroRepository();
        FakeView view = new FakeView().enqueue("east", "y");

        Maze maze = emptyMaze(5);
        maze.setPotionPos(new Position(3, 2));

        GameController controller = new GameController(
                new FixedMazeGenerator(maze),
                new NoOpEntityPlacer(),
                new StubCombatController(true),
                repository,
                new NonMovingRandomProvider()
        );

        MissionResult result = controller.runMission(view, hero);

        assertEquals(MissionResult.EXIT_APP, result);
        assertEquals(1, repository.saveCalls);
        assertEquals(107, hero.getCurrentHp()); // +62 heal from baseMaxHp/2
        assertTrue(view.outputs().contains("You have found a health potion, do you want to drink it [Y/n]?"));
    }

    @Test
    void encounterBlocksQuitAttempt() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        InMemoryHeroRepository repository = new InMemoryHeroRepository();
        FakeView view = new FakeView().enqueue("east", FakeView.QUIT_ATTEMPT, "y");

        Maze maze = emptyMaze(5);
        Enemy enemy = new Enemy("Goblin", false, 1, 15, 15, 100);
        maze.enemies().add(new EnemyInstance(enemy, new Position(3, 2)));

        GameController controller = new GameController(
                new FixedMazeGenerator(maze),
                new NoOpEntityPlacer(),
                new StubCombatController(true),
                repository,
                new NonMovingRandomProvider()
        );

        MissionResult result = controller.runMission(view, hero);

        assertEquals(MissionResult.EXIT_APP, result);
        assertTrue(view.outputs().contains("You cannot quit now."));
    }

    private Maze emptyMaze(int size) {
        TileType[][] terrain = new TileType[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                terrain[y][x] = TileType.FLOOR;
            }
        }
        Maze maze = new Maze(size, terrain);
        maze.setHeroStart(new Position(size / 2, size / 2));
        return maze;
    }

    private static class FixedMazeGenerator extends MazeGenerator {
        private final Maze maze;

        FixedMazeGenerator(Maze maze) {
            super(new NonMovingRandomProvider());
            this.maze = maze;
        }

        @Override
        public Maze generate(int size) {
            return maze;
        }
    }

    private static class NoOpEntityPlacer extends EntityPlacer {
        NoOpEntityPlacer() {
            super(new NonMovingRandomProvider());
        }

        @Override
        public void placePotion(Maze maze) {
        }

        @Override
        public void placeEnemies(Maze maze, int heroLevel) {
        }
    }

    private static class StubCombatController extends CombatController {
        private final boolean result;

        StubCombatController(boolean result) {
            super(new CombatResolver(), new NonMovingRandomProvider());
            this.result = result;
        }

        @Override
        public boolean fight(com.swingy.view.View view, Hero hero, Enemy enemy) {
            return result;
        }
    }

    private static class NonMovingRandomProvider implements RandomProvider {
        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public double nextDouble() {
            return 1.0;
        }

        @Override
        public <T> void shuffle(List<T> list) {
        }
    }
}

package com.swingy.model.world;

import com.swingy.model.Enemy;
import com.swingy.util.DeterministicRandomProvider;
import com.swingy.util.RandomProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityPlacerTest {

    @Test
    void placePotionRespectsBasicConstraints() {
        Maze maze = new MazeGenerator(new DeterministicRandomProvider(7L)).generate(21);
        EntityPlacer placer = new EntityPlacer(new DeterministicRandomProvider(11L));

        placer.placePotion(maze);

        Position potion = maze.potionPos();
        assertNotNull(potion);
        assertTrue(maze.terrainAt(potion) == TileType.FLOOR);
        assertTrue(!maze.exits().contains(potion));
        assertTrue(potion.manhattan(maze.heroStart()) >= (maze.size() / 4));
    }

    @Test
    void placeEnemiesRespectsCountAndPlacementConstraints() {
        Maze maze = new MazeGenerator(new DeterministicRandomProvider(13L)).generate(21);
        EntityPlacer placer = new EntityPlacer(new DeterministicRandomProvider(17L));
        placer.placePotion(maze);

        placer.placeEnemies(maze, 3);

        int desiredMax = (maze.size() * maze.size()) / 32;
        assertTrue(maze.enemies().size() <= desiredMax);

        Set<Position> seen = new HashSet<>();
        for (EnemyInstance enemy : maze.enemies()) {
            Position p = enemy.pos();
            assertTrue(maze.terrainAt(p) == TileType.FLOOR);
            assertNotSame(maze.heroStart(), p);
            assertTrue(!p.equals(maze.heroStart()));
            assertTrue(maze.potionPos() == null || !p.equals(maze.potionPos()));
            assertTrue(seen.add(p));
        }
    }

    @Test
    void maybeMakeUniqueCreatesAtMostOneUniqueWithLevelPlusTwo() {
        List<EnemyInstance> enemies = new ArrayList<>();
        enemies.add(new EnemyInstance(new Enemy("A", false, 5, 10, 10, 100), new Position(1, 1)));
        enemies.add(new EnemyInstance(new Enemy("B", false, 5, 10, 10, 100), new Position(2, 1)));
        enemies.add(new EnemyInstance(new Enemy("C", false, 5, 10, 10, 100), new Position(3, 1)));

        EntityPlacer placer = new EntityPlacer(new ForceUniqueRandomProvider());
        placer.maybeMakeUnique(enemies, 5);

        long uniqueCount = enemies.stream().filter(e -> e.enemy().isUnique()).count();
        assertEquals(1, uniqueCount);

        EnemyInstance unique = enemies.stream().filter(e -> e.enemy().isUnique()).findFirst().orElseThrow();
        assertEquals(7, unique.enemy().getLevel());
    }

    private static class ForceUniqueRandomProvider implements RandomProvider {
        private int call;

        @Override
        public int nextInt(int bound) {
            call++;
            if (call == 1) {
                return 1; // index to convert
            }
            return 0;
        }

        @Override
        public double nextDouble() {
            return 0.0; // always pass chance(0.25)
        }

        @Override
        public <T> void shuffle(List<T> list) {
        }
    }
}

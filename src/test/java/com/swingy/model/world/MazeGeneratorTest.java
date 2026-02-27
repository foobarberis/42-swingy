package com.swingy.model.world;

import com.swingy.util.DeterministicRandomProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MazeGeneratorTest {

    @Test
    void generatedMazeHasCorrectExitsAndReachability() {
        List<Long> seeds = List.of(1L, 2L, 7L, 11L, 42L);

        for (long seed : seeds) {
            int size = 21;
            MazeGenerator generator = new MazeGenerator(new DeterministicRandomProvider(seed));
            Maze maze = generator.generate(size);

            assertEquals(size, maze.size());
            assertEquals(TileType.FLOOR, maze.terrainAt(maze.heroStart()));

            Position north = new Position(size / 2, 0);
            Position south = new Position(size / 2, size - 1);
            Position west = new Position(0, size / 2);
            Position east = new Position(size - 1, size / 2);

            assertTrue(maze.exits().containsAll(List.of(north, south, west, east)));
            assertEquals(TileType.EXIT, maze.terrainAt(north));
            assertEquals(TileType.EXIT, maze.terrainAt(south));
            assertEquals(TileType.EXIT, maze.terrainAt(west));
            assertEquals(TileType.EXIT, maze.terrainAt(east));

            assertEquals(TileType.FLOOR, maze.terrainAt(new Position(size / 2, 1)));
            assertEquals(TileType.FLOOR, maze.terrainAt(new Position(size / 2, size - 2)));
            assertEquals(TileType.FLOOR, maze.terrainAt(new Position(1, size / 2)));
            assertEquals(TileType.FLOOR, maze.terrainAt(new Position(size - 2, size / 2)));

            Set<Position> visited = bfsWalkable(maze, maze.heroStart());
            assertTrue(visited.containsAll(maze.exits()));
        }
    }

    private Set<Position> bfsWalkable(Maze maze, Position start) {
        Set<Position> visited = new HashSet<>();
        ArrayDeque<Position> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Position p = queue.poll();
            for (Position n : p.neighbors4()) {
                if (!maze.isInside(n) || visited.contains(n) || !maze.isWalkable(n)) {
                    continue;
                }
                visited.add(n);
                queue.add(n);
            }
        }
        return visited;
    }
}

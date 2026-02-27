package com.swingy.model.world;

import com.swingy.util.RandomProvider;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MazeGenerator {
    private final RandomProvider rng;

    public MazeGenerator(RandomProvider rng) {
        this.rng = rng;
    }

    public Maze generate(int size) {
        Maze maze;
        int attempts = 0;
        do {
            maze = generateOnce(size);
            attempts++;
        } while (!allExitsReachable(maze) && attempts < 100);
        return maze;
    }

    private Maze generateOnce(int size) {
        TileType[][] grid = new TileType[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                grid[y][x] = TileType.WALL;
            }
        }
        Maze maze = new Maze(size, grid);
        Position heroStart = new Position(size / 2, size / 2);
        maze.setHeroStart(heroStart);

        int c = size / 2;
        int oddCenter = c + (c % 2 == 0 ? 1 : 0);
        Position carveStart = new Position(oddCenter, oddCenter);
        maze.setTerrain(carveStart, TileType.FLOOR);

        ArrayDeque<Position> stack = new ArrayDeque<>();
        stack.push(carveStart);
        while (!stack.isEmpty()) {
            Position cur = stack.peek();
            List<Position> unvisited = new ArrayList<>();
            List<Position> between = new ArrayList<>();
            int[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            for (int[] d : dirs) {
                Position n = new Position(cur.x() + d[0] * 2, cur.y() + d[1] * 2);
                if (n.x() > 0 && n.y() > 0 && n.x() < size - 1 && n.y() < size - 1 && maze.terrainAt(n) == TileType.WALL) {
                    unvisited.add(n);
                    between.add(new Position(cur.x() + d[0], cur.y() + d[1]));
                }
            }
            if (unvisited.isEmpty()) {
                stack.pop();
            } else {
                int i = rng.nextInt(unvisited.size());
                maze.setTerrain(between.get(i), TileType.FLOOR);
                maze.setTerrain(unvisited.get(i), TileType.FLOOR);
                stack.push(unvisited.get(i));
            }
        }

        maze.setTerrain(heroStart, TileType.FLOOR);
        List<Position> exits = List.of(
                new Position(size / 2, 0),
                new Position(size / 2, size - 1),
                new Position(0, size / 2),
                new Position(size - 1, size / 2)
        );
        List<Position> adj = List.of(
                new Position(size / 2, 1),
                new Position(size / 2, size - 2),
                new Position(1, size / 2),
                new Position(size - 2, size / 2)
        );
        for (int i = 0; i < exits.size(); i++) {
            maze.setTerrain(adj.get(i), TileType.FLOOR);
            maze.setTerrain(exits.get(i), TileType.EXIT);
            maze.addExit(exits.get(i));
        }
        return maze;
    }

    private boolean allExitsReachable(Maze maze) {
        Set<Position> visited = new HashSet<>();
        ArrayDeque<Position> q = new ArrayDeque<>();
        q.add(maze.heroStart());
        visited.add(maze.heroStart());
        while (!q.isEmpty()) {
            Position p = q.poll();
            for (Position n : p.neighbors4()) {
                if (!maze.isInside(n) || visited.contains(n)) continue;
                if (maze.isWalkable(n)) {
                    visited.add(n);
                    q.add(n);
                }
            }
        }
        return visited.containsAll(maze.exits());
    }
}

package com.swingy.model.world;

import com.swingy.model.Enemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FogOfWarTest {

    @Test
    void viewportIsAlways11x11() {
        Maze maze = floorMaze(5);
        Position heroPos = new Position(2, 2);

        char[][] window = new FogOfWar().viewport(maze, heroPos);

        assertEquals(11, window.length);
        assertEquals(11, window[0].length);
    }

    @Test
    void outOfBoundsIsRenderedAsSpace() {
        Maze maze = floorMaze(5);
        Position heroPos = new Position(0, 0);

        char[][] window = new FogOfWar().viewport(maze, heroPos);

        assertEquals(' ', window[0][0]);
        assertEquals(' ', window[0][1]);
        assertEquals(' ', window[1][0]);
    }

    @Test
    void overlayPrecedenceIsPlayerThenEnemyThenPotionThenTerrain() {
        Maze maze = floorMaze(5);
        Position heroPos = new Position(2, 2);

        maze.setPotionPos(new Position(2, 2)); // same as hero, hero must win
        maze.addEnemy(new EnemyInstance(new Enemy("Goblin", false, 1, 15, 15, 100), new Position(2, 2)));
        maze.addEnemy(new EnemyInstance(new Enemy("Orc", false, 1, 15, 15, 100), new Position(3, 2)));
        maze.setPotionPos(new Position(1, 2));
        maze.setTerrain(new Position(4, 2), TileType.WALL);

        char[][] window = new FogOfWar().viewport(maze, heroPos);

        int centerY = window.length / 2;
        int centerX = window[0].length / 2;

        assertEquals('@', window[centerY][centerX]);
        assertEquals('M', window[centerY][centerX + 1]);
        assertEquals('!', window[centerY][centerX - 1]);
        assertEquals('#', window[centerY][centerX + 2]);
    }

    private Maze floorMaze(int size) {
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
}

package com.swingy.model.world;

public class FogOfWar {
    public char[][] window5x5(Maze maze, Position heroPos) {
        char[][] out = new char[5][5];
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                Position p = new Position(heroPos.x() + dx, heroPos.y() + dy);
                char c;
                if (!maze.isInside(p)) {
                    c = ' ';
                } else if (p.equals(heroPos)) {
                    c = '@';
                } else {
                    EnemyInstance enemy = maze.enemies().stream().filter(e -> e.pos().equals(p)).findFirst().orElse(null);
                    if (enemy != null) {
                        c = enemy.enemy().isUnique() ? 'U' : 'M';
                    } else if (maze.potionPos() != null && maze.potionPos().equals(p)) {
                        c = '!';
                    } else {
                        c = switch (maze.terrainAt(p)) {
                            case WALL -> '#';
                            case FLOOR -> '.';
                            case EXIT -> 'X';
                        };
                    }
                }
                out[dy + 2][dx + 2] = c;
            }
        }
        return out;
    }
}

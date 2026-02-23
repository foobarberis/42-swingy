package com.swingy.model.world;

public class FogOfWar {
    public char[][] viewport(Maze maze, Position heroPos) {
        char[][] out = new char[11][11];
        int centerY = out.length / 2;
        int centerX = out[0].length / 2;

        for (int y = 0; y < out.length; y++) {
            int dy = y - centerY;
            for (int x = 0; x < out[y].length; x++) {
                int dx = x - centerX;
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
                out[y][x] = c;
            }
        }
        return out;
    }
}

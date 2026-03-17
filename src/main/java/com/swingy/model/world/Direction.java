package com.swingy.model.world;

import java.util.Locale;

public enum Direction {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Position move(Position position) {
        return new Position(
            Math.addExact(position.x(), dx),
            Math.addExact(position.y(), dy)
        );
    }

    public static Direction parse(String input) {
        if (input == null) {
            return null;
        }
        return switch (input.trim().toLowerCase(Locale.ROOT)) {
            case "north", "n" -> NORTH;
            case "east", "e" -> EAST;
            case "south", "s" -> SOUTH;
            case "west", "w" -> WEST;
            default -> null;
        };
    }
}

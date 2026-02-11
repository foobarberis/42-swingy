package com.swingy.model.world;

import java.util.List;

public record Position(int x, int y) {
    public List<Position> neighbors4() {
        return List.of(new Position(x, y - 1), new Position(x + 1, y), new Position(x, y + 1), new Position(x - 1, y));
    }

    public int manhattan(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
}

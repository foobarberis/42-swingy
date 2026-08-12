package com.swingy.model.world;

import com.swingy.model.Enemy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Room {
    private final int size;
    private final Position center;
    private final Map<Position, Enemy> enemies = new HashMap<>();

    public Room(int size) {
        if (size < 3 || size % 2 == 0) {
            throw new IllegalArgumentException("Room size must be an odd number of at least 3.");
        }
        this.size = size;
        center = new Position(size / 2, size / 2);
    }

    public int getSize() {
        return size;
    }

    public Position center() {
        return center;
    }

    public boolean isInside(Position position) {
        return position != null
            && position.x() >= 0
            && position.y() >= 0
            && position.x() < size
            && position.y() < size;
    }

    public boolean isBorder(Position position) {
        return isInside(position)
            && (position.x() == 0
                || position.y() == 0
                || position.x() == size - 1
                || position.y() == size - 1);
    }

    public boolean isInterior(Position position) {
        return isInside(position) && !isBorder(position);
    }

    public boolean hasEnemies() {
        return !enemies.isEmpty();
    }

    public Enemy enemyAt(Position position) {
        return enemies.get(position);
    }

    public void addEnemy(Position position, Enemy enemy) {
        Objects.requireNonNull(position, "Enemy position is required.");
        Objects.requireNonNull(enemy, "Enemy is required.");
        if (!isInterior(position)) {
            throw new IllegalArgumentException("Enemies must be inside the room interior.");
        }
        if (center.equals(position)) {
            throw new IllegalArgumentException("An enemy cannot occupy the room center.");
        }
        if (enemies.putIfAbsent(position, enemy) != null) {
            throw new IllegalArgumentException("Enemy positions must be unique.");
        }
    }

    public void removeEnemy(Position position) {
        Objects.requireNonNull(position, "Enemy position is required.");
        enemies.remove(position);
    }
}

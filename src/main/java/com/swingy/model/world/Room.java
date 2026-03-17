package com.swingy.model.world;

import com.swingy.model.Enemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Room {
    private final int size;
    private final List<Enemy> enemies = new ArrayList<>();
    private final Position heroStart;

    public Room(int size, Position heroStart) {
        if (size < 3 || size % 2 == 0) {
            throw new IllegalArgumentException("Room size must be an odd number of at least 3.");
        }
        this.size = size;
        this.heroStart = Objects.requireNonNull(heroStart, "Hero start is required.");
        Position center = new Position(size / 2, size / 2);
        if (!center.equals(heroStart)) {
            throw new IllegalArgumentException("Hero must start in the exact center of the room.");
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isInside(Position position) {
        if (position == null) {
            return false;
        }
        return position.x() >= 0
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

    public Position getHeroStart() {
        return heroStart;
    }

    public List<Enemy> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }

    public Enemy enemyAt(Position position) {
        for (Enemy enemy : enemies) {
            if (enemy.getPosition().equals(position)) {
                return enemy;
            }
        }
        return null;
    }

    public void addEnemy(Enemy enemy) {
        Objects.requireNonNull(enemy, "Enemy is required.");
        Position position = enemy.getPosition();
        if (!isInterior(position)) {
            throw new IllegalArgumentException("Enemies must be inside the room interior.");
        }
        if (heroStart.equals(position)) {
            throw new IllegalArgumentException("An enemy cannot occupy the hero start.");
        }
        if (enemyAt(position) != null) {
            throw new IllegalArgumentException("Enemy positions must be unique.");
        }
        enemies.add(enemy);
    }

    public void removeEnemy(Enemy enemy) {
        Objects.requireNonNull(enemy, "Enemy is required.");
        enemies.remove(enemy);
    }
}

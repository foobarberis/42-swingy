package com.swingy.logic;

import com.swingy.model.Enemy;
import com.swingy.model.EnemyType;
import com.swingy.model.GameRules;
import com.swingy.model.Hero;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;

import java.util.Objects;
import java.util.Random;

public final class RandomRoomFactory implements RoomFactory {
    private final Random random;

    public RandomRoomFactory(Random random) {
        this.random = Objects.requireNonNull(random, "Random source is required.");
    }

    @Override
    public Room create(Hero hero) {
        Objects.requireNonNull(hero, "Hero is required.");
        int size = GameRules.mapSizeForLevel(hero.getLevel());
        Room room = new Room(size);

        for (int y = 1; y < size - 1; y++) {
            for (int x = 1; x < size - 1; x++) {
                Position position = new Position(x, y);
                if (!position.equals(room.center()) && random.nextInt(10) == 0) {
                    room.addEnemy(position, createEnemy(hero.getLevel()));
                }
            }
        }

        if (!room.hasEnemies()) {
            Position eastOfCenter = new Position(room.center().x() + 1, room.center().y());
            room.addEnemy(eastOfCenter, createEnemy(hero.getLevel()));
        }
        return room;
    }

    private Enemy createEnemy(int heroLevel) {
        EnemyType type = switch (random.nextInt(3)) {
            case 0 -> EnemyType.GOBLIN;
            case 1 -> EnemyType.ORC;
            default -> EnemyType.TROLL;
        };
        int enemyLevel = Math.max(1, heroLevel - random.nextInt(2));
        return new Enemy(type, enemyLevel);
    }
}

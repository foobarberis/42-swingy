package com.swingy.logic;

import com.swingy.model.Enemy;
import com.swingy.model.GameRules;
import com.swingy.model.Hero;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class RandomRoomFactory implements RoomFactory {
    private static final List<String> ENEMY_NAMES = List.of(
        "Kobold",
        "Goblin",
        "Orc",
        "Skeleton",
        "Zombie",
        "Ogre",
        "Troll",
        "Centaur",
        "Jelly",
        "Harpy",
        "Wraith",
        "Bandit"
    );

    private final Random random;

    public RandomRoomFactory(Random random) {
        this.random = Objects.requireNonNull(random, "Random source is required.");
    }

    @Override
    public Room create(Hero hero) {
        Objects.requireNonNull(hero, "Hero is required.");
        int size = GameRules.mapSizeForLevel(hero.getLevel());
        Position start = new Position(size / 2, size / 2);
        Room room = new Room(size, start);

        List<Position> positions = new ArrayList<>();
        for (int y = 1; y < size - 1; y++) {
            for (int x = 1; x < size - 1; x++) {
                Position position = new Position(x, y);
                if (!position.equals(start)) {
                    positions.add(position);
                }
            }
        }
        Collections.shuffle(positions, random);

        int enemyCount = Math.max(1, Math.multiplyExact(size, size) / 16);
        enemyCount = Math.min(enemyCount, positions.size());
        for (int index = 0; index < enemyCount; index++) {
            Position position = positions.get(index);
            room.addEnemy(createEnemy(hero.getLevel(), position));
        }
        return room;
    }

    private Enemy createEnemy(int heroLevel, Position position) {
        int offset = random.nextInt(3) - 1;
        int candidateLevel = Math.addExact(heroLevel, offset);
        int enemyLevel = GameRules.isSupportedLevel(candidateLevel)
            ? candidateLevel
            : heroLevel;
        int hp = Math.addExact(60, Math.multiplyExact(enemyLevel - 1, 10));
        int atk = Math.addExact(10, Math.multiplyExact(enemyLevel - 1, 5));
        int def = Math.addExact(10, Math.multiplyExact(enemyLevel - 1, 5));
        String name = ENEMY_NAMES.get(random.nextInt(ENEMY_NAMES.size()));
        return new Enemy(name, enemyLevel, atk, def, hp, position);
    }
}

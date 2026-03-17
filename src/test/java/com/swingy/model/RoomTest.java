package com.swingy.model;

import com.swingy.logic.RandomRoomFactory;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {
    @Test
    void generatedRoomStartsHeroAtExactCenterAndPlacesValidEnemies() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = new RandomRoomFactory(
            new SequenceRandom(List.of(), List.of())
        ).create(hero);

        assertEquals(new Position(room.getSize() / 2, room.getSize() / 2), room.getHeroStart());
        Set<Position> positions = new HashSet<>();
        for (Enemy enemy : room.getEnemies()) {
            assertTrue(room.isInterior(enemy.getPosition()));
            assertFalse(room.getHeroStart().equals(enemy.getPosition()));
            assertTrue(positions.add(enemy.getPosition()));
        }
    }

    @Test
    void roomRejectsInvalidStartAndEnemyPositions() {
        assertThrows(NullPointerException.class, () -> new Room(5, null));
        assertThrows(IllegalArgumentException.class, () -> new Room(5, new Position(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> new Room(4, new Position(2, 2)));

        Room room = room(5);
        assertThrows(NullPointerException.class, () -> room.addEnemy(null));
        assertThrows(
            IllegalArgumentException.class,
            () -> room.addEnemy(enemyAt(new Position(0, 1)))
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> room.addEnemy(enemyAt(room.getHeroStart()))
        );

        room.addEnemy(enemyAt(new Position(1, 1)));
        assertThrows(
            IllegalArgumentException.class,
            () -> room.addEnemy(enemyAt(new Position(1, 1)))
        );
    }

    private Room room(int size) {
        return new Room(size, new Position(size / 2, size / 2));
    }

    private Enemy enemyAt(Position position) {
        return new Enemy("Goblin", 1, 10, 10, 60, position);
    }
}

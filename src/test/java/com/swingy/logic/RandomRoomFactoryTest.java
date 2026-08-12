package com.swingy.logic;

import com.swingy.model.Enemy;
import com.swingy.model.EnemyType;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RandomRoomFactoryTest {
    @Test
    void directTraversalUsesIndependentTypeAndLevelRolls() {
        SequenceRandom random = new SequenceRandom(List.of(0, 2, 1), List.of());
        Hero hero = Hero.restore("H", HeroClass.WARRIOR, 3, 2_450, 18, 0, 0, 0);
        Room room = new RandomRoomFactory(random).create(hero);

        Enemy first = room.enemyAt(new Position(1, 1));
        assertNotNull(first);
        assertEquals(EnemyType.TROLL, first.getType());
        assertEquals(2, first.getLevel());
        assertNull(room.enemyAt(room.center()));
        for (int coordinate = 0; coordinate < room.getSize(); coordinate++) {
            assertNull(room.enemyAt(new Position(coordinate, 0)));
            assertNull(room.enemyAt(new Position(0, coordinate)));
        }
    }

    @Test
    void fallbackCreatesExactlyOneEnemyEastOfCenter() {
        SequenceRandom random = new SequenceRandom(List.of(), List.of(), 1, false);
        Room room = new RandomRoomFactory(random).create(Hero.createNew("H", HeroClass.ROGUE));
        int occupied = 0;
        for (int y = 0; y < room.getSize(); y++) {
            for (int x = 0; x < room.getSize(); x++) {
                if (room.enemyAt(new Position(x, y)) != null) {
                    occupied++;
                }
            }
        }
        assertEquals(1, occupied);
        assertNotNull(room.enemyAt(new Position(room.center().x() + 1, room.center().y())));
        assertFalse(room.isBorder(new Position(room.center().x() + 1, room.center().y())));
    }
}

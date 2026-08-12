package com.swingy.model;

import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {
    @Test
    void roomDerivesCenterAndOwnsOccupancyByPosition() {
        Room room = new Room(9);
        Position position = new Position(2, 3);
        Enemy enemy = new Enemy(EnemyType.ORC, 1);

        assertEquals(new Position(4, 4), room.center());
        assertFalse(room.hasEnemies());
        room.addEnemy(position, enemy);
        assertTrue(room.hasEnemies());
        assertSame(enemy, room.enemyAt(position));
        assertThrows(IllegalArgumentException.class, () -> room.addEnemy(position, enemy));
        assertThrows(IllegalArgumentException.class, () -> room.addEnemy(room.center(), enemy));
        assertThrows(IllegalArgumentException.class, () -> room.addEnemy(new Position(0, 2), enemy));
        room.removeEnemy(position);
        assertNull(room.enemyAt(position));
    }
}

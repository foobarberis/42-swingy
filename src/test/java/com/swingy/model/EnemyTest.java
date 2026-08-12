package com.swingy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnemyTest {
    @Test
    void enemyProfilesAndLevelScalingAreDirect() {
        Enemy goblin = new Enemy(EnemyType.GOBLIN, 2);
        assertEquals("Goblin", goblin.getName());
        assertEquals(20, goblin.getMaxHp());
        assertEquals(10, goblin.getAttack());
        assertEquals(2, goblin.getDefense());

        Enemy troll = new Enemy(EnemyType.TROLL, 3);
        assertEquals(30, troll.getMaxHp());
        assertEquals(3, troll.getAttack());
        assertEquals(15, troll.getDefense());
    }
}

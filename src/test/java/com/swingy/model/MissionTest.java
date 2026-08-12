package com.swingy.model;

import com.swingy.model.world.Direction;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MissionTest {
    @Test
    void missionStartsAtCenterFindsEnemiesAndRetreats() {
        Room room = new Room(9);
        Enemy enemy = new Enemy(EnemyType.ORC, 1);
        room.addEnemy(new Position(5, 4), enemy);
        Mission mission = new Mission(Hero.createNew("H", HeroClass.WARRIOR), room);

        assertEquals(room.center(), mission.getHeroPosition());
        assertSame(enemy, mission.move(Direction.EAST).enemy());
        mission.retreat();
        assertEquals(room.center(), mission.getHeroPosition());
    }

    @Test
    void borderVictoryDoesNotHeal() {
        Hero hero = Hero.createNew("H", HeroClass.WARRIOR);
        hero.takeDamage(7);
        Mission mission = new Mission(hero, new Room(9));
        for (int step = 0; step < 3; step++) {
            assertEquals(Mission.MoveResult.Type.MOVED, mission.move(Direction.NORTH).type());
        }
        assertEquals(Mission.MoveResult.Type.WON, mission.move(Direction.NORTH).type());
        assertEquals(11, hero.getCurrentHp());
    }
}

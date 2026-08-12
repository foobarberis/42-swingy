package com.swingy.model;

import com.swingy.model.world.Direction;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MissionTest {
    @Test
    void missionOwnsMovementEncounterAndRetreatState() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = new Room(5, new Position(2, 2));
        Enemy enemy = new Enemy("Goblin", 1, 10, 10, 60, new Position(3, 2));
        room.addEnemy(enemy);
        Mission mission = new Mission(hero, room);

        Mission.MoveResult encounter = mission.move(Direction.EAST);

        assertEquals(Mission.MoveResult.Type.ENCOUNTER, encounter.type());
        assertEquals(enemy, encounter.enemy());
        assertEquals(new Position(3, 2), mission.getHeroPosition());

        mission.retreat();

        assertEquals(new Position(2, 2), mission.getHeroPosition());
    }

    @Test
    void missionReportsBlockedMovesAndHealsTheHeroOnVictory() {
        Hero hero = Hero.builder("Alice", HeroClass.WARRIOR)
            .currentHp(50)
            .build();
        Mission mission = new Mission(hero, new Room(5, new Position(2, 2)));

        assertEquals(Mission.MoveResult.Type.MOVED, mission.move(Direction.NORTH).type());
        assertEquals(Mission.MoveResult.Type.WON, mission.move(Direction.NORTH).type());
        assertEquals(hero.getMaxHp(), hero.getCurrentHp());
        assertEquals(Mission.MoveResult.Type.BLOCKED, mission.move(Direction.NORTH).type());
    }
}

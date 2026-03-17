package com.swingy.logic;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.Mission;
import com.swingy.model.world.Direction;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EncounterServiceTest {
    @Test
    void successfulRunRestoresThePositionThroughTheMissionModel() {
        Mission mission = missionWithEnemy(new Enemy("Goblin", 1, 10, 0, 45, new Position(3, 2)));
        Enemy enemy = mission.getRoom().enemyAt(new Position(3, 2));
        mission.move(Direction.EAST);
        EncounterService service = new EncounterService(
            new CombatService(),
            new SequenceRandom(List.of(), List.of(0.49))
        );

        EncounterResult result = service.resolve(mission, enemy, EncounterAction.RUN);

        assertEquals(EncounterResult.Type.ESCAPED, result.type());
        assertEquals(new Position(2, 2), mission.getHeroPosition());
        assertFalse(result.escapeFailed());
    }

    @Test
    void defeatedEnemyIsRemovedByTheEncounterService() {
        Mission mission = missionWithEnemy(new Enemy("Goblin", 1, 1, 0, 1, new Position(3, 2)));
        Enemy enemy = mission.getRoom().enemyAt(new Position(3, 2));
        mission.move(Direction.EAST);
        EncounterService service = new EncounterService(
            new CombatService(),
            new SequenceRandom(List.of(), List.of(1.0))
        );

        EncounterResult result = service.resolve(mission, enemy, EncounterAction.FIGHT);

        assertEquals(EncounterResult.Type.ENEMY_DEFEATED, result.type());
        assertEquals(null, mission.getRoom().enemyAt(new Position(3, 2)));
        assertEquals(100L, mission.getHero().getXp());
    }

    private Mission missionWithEnemy(Enemy enemy) {
        Room room = new Room(5, new Position(2, 2));
        room.addEnemy(enemy);
        return new Mission(Hero.createNew("Alice", HeroClass.WARRIOR), room);
    }
}

package com.swingy.logic;

import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.EnemyType;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.Mission;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EncounterServiceTest {
    @Test
    void fightUsesSubtractionHeroFirstAndAppliesDirectRewards() {
        Hero hero = Hero.restore("H", HeroClass.WARRIOR, 2, 1_000, 20, 0, 0, 0);
        Enemy enemy = new Enemy(EnemyType.GOBLIN, 2);
        Mission mission = missionWithEnemy(hero, enemy);
        EncounterService service = new EncounterService(
            new SequenceRandom(List.of(1), List.of(true))
        );

        EncounterResult result = service.fight(mission, enemy);

        assertEquals(EncounterResult.Type.ENEMY_DEFEATED, result.type());
        assertEquals(200, result.xpReward());
        assertEquals(18, hero.getCurrentHp());
        assertNull(mission.getRoom().enemyAt(mission.getHeroPosition()));
        assertEquals(new Artifact(Artifact.Slot.ARMOR, 2), result.artifact());
    }

    @Test
    void successfulRunUsesOneBooleanAndRestoresPosition() {
        Hero hero = Hero.createNew("H", HeroClass.WARRIOR);
        Enemy enemy = new Enemy(EnemyType.ORC, 1);
        Mission mission = missionWithEnemy(hero, enemy);
        EncounterResult result = new EncounterService(
            new SequenceRandom(List.of(), List.of(true))
        ).run(mission, enemy);

        assertEquals(EncounterResult.Type.ESCAPED, result.type());
        assertEquals(mission.getRoom().center(), mission.getHeroPosition());
        assertSame(enemy, mission.getRoom().enemyAt(new Position(5, 4)));
    }

    @Test
    void failedEscapeInvokesSameTerminatingCombatAndCanKillHero() {
        Hero hero = Hero.restore("H", HeroClass.MAGE, 1, 0, 1, 0, 0, 0);
        Enemy enemy = new Enemy(EnemyType.TROLL, 1);
        Mission mission = missionWithEnemy(hero, enemy);
        EncounterResult result = new EncounterService(
            new SequenceRandom(List.of(), List.of(false))
        ).run(mission, enemy);

        assertEquals(EncounterResult.Type.HERO_DIED, result.type());
        assertEquals(0, hero.getCurrentHp());
        assertEquals(true, result.escapeFailed());
    }

    private Mission missionWithEnemy(Hero hero, Enemy enemy) {
        Room room = new Room(9);
        Position position = new Position(5, 4);
        room.addEnemy(position, enemy);
        Mission mission = new Mission(hero, room);
        mission.move(com.swingy.model.world.Direction.EAST);
        return mission;
    }
}

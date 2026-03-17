package com.swingy.controller;

import com.swingy.logic.CombatService;
import com.swingy.logic.EncounterService;
import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import com.swingy.support.FakeView;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissionControllerTest {
    @Test
    void reachingAnyBorderWinsWithoutAutomaticallyHealingHero() {
        Hero hero = Hero.builder("Alice", HeroClass.WARRIOR)
            .currentHp(50)
            .build();
        FakeView view = new FakeView().enqueue("north", "north");

        MissionResult result = play(view, hero, openRoom(5), random());

        assertEquals(MissionResult.Type.WON, result.type());
        assertEquals(50, hero.getCurrentHp());
        assertTrue(view.outputs().contains("Victory! You reached the border."));
    }

    @Test
    void mapAndAllMandatoryStatisticsAreRendered() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        FakeView view = new FakeView();

        play(view, hero, openRoom(5), random());

        assertTrue(view.outputs().contains("MAP:*****|*...*|*.@.*|*...*|*****"));
        assertTrue(
            view.outputs().stream().anyMatch(
                line -> line.contains("Name: Alice")
                    && line.contains("Attack: 10")
                    && line.contains("Defense: 20")
                    && line.contains("Hit Points: 125/125")
            )
        );
    }

    @Test
    void movementRejectsCommandsOtherThanFourDirections() {
        FakeView view = new FakeView().enqueue("up", "north", "north");

        MissionResult result = play(
            view,
            Hero.createNew("Alice", HeroClass.WARRIOR),
            openRoom(5),
            random()
        );

        assertEquals(MissionResult.Type.WON, result.type());
        assertTrue(
            view.outputs().stream().anyMatch(line -> line.startsWith("Unknown command."))
        );
    }

    @Test
    void runBelowHalfSucceedsAndRestoresPreviousPosition() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = roomWithEnemy(new Enemy("Goblin", 1, 10, 0, 45, new Position(3, 2)));
        FakeView view = new FakeView().enqueue("east", "run", "north", "north");

        MissionResult result = play(
            view,
            hero,
            room,
            new SequenceRandom(List.of(), List.of(0.499_999))
        );

        assertEquals(MissionResult.Type.WON, result.type());
        assertEquals(0L, hero.getXp());
        assertTrue(view.outputs().contains("You escaped."));
    }

    @Test
    void runAtHalfFailsAndStartsCombat() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = roomWithEnemy(new Enemy("Goblin", 1, 1, 0, 1, new Position(3, 2)));
        FakeView view = new FakeView().enqueue("east", "run");

        play(view, hero, room, new SequenceRandom(List.of(), List.of(0.5, 1.0)));

        assertEquals(100L, hero.getXp());
        assertTrue(view.outputs().contains("You failed to escape."));
        assertTrue(view.outputs().contains("Combat starts against Goblin."));
    }

    @Test
    void losingCombatReturnsDeathResult() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = roomWithEnemy(
            new Enemy("Ogre", 1, 1_000, 0, 200, new Position(3, 2))
        );
        FakeView view = new FakeView().enqueue("east", "fight");

        MissionResult result = play(view, hero, room, random());

        assertEquals(MissionResult.Type.HERO_DIED, result.type());
        assertEquals(0, hero.getCurrentHp());
    }

    @Test
    void victoryXpStaysCumulativeWhenLeveling() {
        Hero hero = Hero.builder("Alice", HeroClass.WARRIOR)
            .xp(950L)
            .build();
        Room room = roomWithEnemy(new Enemy("Goblin", 1, 1, 0, 1, new Position(3, 2)));
        FakeView view = new FakeView().enqueue("east", "fight");

        play(view, hero, room, random());

        assertEquals(2, hero.getLevel());
        assertEquals(1_050L, hero.getXp());
        assertTrue(view.outputs().contains("Congratulations, you have reached level 2!"));
    }

    @Test
    void droppedArtifactCanBeEquipped() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = roomWithEnemy(new Enemy("Goblin", 1, 1, 0, 1, new Position(3, 2)));
        FakeView view = new FakeView().enqueue("east", "fight", "yes");
        SequenceRandom random = new SequenceRandom(List.of(0), List.of(0.1));

        play(view, hero, room, random);

        assertEquals(new Artifact(Artifact.Slot.WEAPON, 0), hero.getArtifact(Artifact.Slot.WEAPON));
        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("has been equipped")));
    }

    @Test
    void droppedArtifactCanBeDiscarded() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Room room = roomWithEnemy(new Enemy("Goblin", 1, 1, 0, 1, new Position(3, 2)));
        FakeView view = new FakeView().enqueue("east", "fight", "no");
        SequenceRandom random = new SequenceRandom(List.of(0), List.of(0.1));

        play(view, hero, room, random);

        assertEquals(null, hero.getArtifact(Artifact.Slot.WEAPON));
        assertTrue(view.outputs().stream().anyMatch(line -> line.contains("has been discarded")));
    }

    private MissionResult play(FakeView view, Hero hero, Room room, SequenceRandom random) {
        MissionController controller = new MissionController(
            view,
            view,
            ignored -> room,
            new EncounterService(new CombatService(), random)
        );
        return controller.play(hero);
    }

    private SequenceRandom random() {
        return new SequenceRandom(List.of(), List.of());
    }

    private Room openRoom(int size) {
        return new Room(size, new Position(size / 2, size / 2));
    }

    private Room roomWithEnemy(Enemy enemy) {
        Room room = openRoom(5);
        room.addEnemy(enemy);
        return room;
    }
}

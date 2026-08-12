package com.swingy.controller;

import com.swingy.logic.EncounterService;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Room;
import com.swingy.support.FakeView;
import com.swingy.support.SequenceRandom;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MissionControllerTest {
    @Test
    void reachesBorderWithoutHealingAndRendersNoCombatTranscript() {
        FakeView view = new FakeView("n", "n", "n", "n");
        Hero hero = Hero.createNew("H", HeroClass.WARRIOR);
        hero.takeDamage(5);
        MissionController controller = new MissionController(
            view,
            ignored -> new Room(9),
            new EncounterService(new SequenceRandom(List.of(), List.of()))
        );

        assertEquals(MissionController.Result.WON, controller.play(hero));
        assertEquals(13, hero.getCurrentHp());
        assertFalse(view.renderedText().contains("Round "));
    }

    @Test
    void nullInputExitsMission() {
        FakeView view = new FakeView();
        MissionController controller = new MissionController(
            view,
            ignored -> new Room(9),
            new EncounterService(new SequenceRandom(List.of(), List.of()))
        );
        assertEquals(
            MissionController.Result.EXIT_APPLICATION,
            controller.play(Hero.createNew("H", HeroClass.ROGUE))
        );
    }
}

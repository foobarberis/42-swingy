package com.swingy.controller;

import com.swingy.logic.EncounterResult;
import com.swingy.logic.EncounterService;
import com.swingy.logic.RoomFactory;
import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.Mission;
import com.swingy.model.world.Direction;
import com.swingy.view.View;
import com.swingy.view.ViewFormatter;

import java.util.Locale;
import java.util.Objects;

public final class MissionController {
    public enum Result {
        WON,
        HERO_DIED,
        EXIT_APPLICATION
    }

    private enum EncounterFlow {
        CONTINUE_MISSION,
        HERO_DIED,
        EXIT_APPLICATION
    }

    private final View view;
    private final RoomFactory roomFactory;
    private final EncounterService encounters;

    public MissionController(View view, RoomFactory roomFactory, EncounterService encounters) {
        this.view = Objects.requireNonNull(view, "View is required.");
        this.roomFactory = Objects.requireNonNull(roomFactory, "Room factory is required.");
        this.encounters = Objects.requireNonNull(encounters, "Encounter service is required.");
    }

    public Result play(Hero hero) {
        Objects.requireNonNull(hero, "Hero is required.");
        Mission mission = new Mission(hero, roomFactory.create(hero));

        while (true) {
            view.show(ViewFormatter.heroStatus(hero));
            view.show(ViewFormatter.map(mission.getRoom(), mission.getHeroPosition()));

            String input = view.readInput();
            if (input == null || isQuit(input)) {
                return Result.EXIT_APPLICATION;
            }

            Direction direction = Direction.parse(input);
            if (direction == null) {
                view.show(
                    "Unknown command. Available commands: north (n), south (s), "
                        + "east (e), west (w), quit."
                );
                continue;
            }

            Mission.MoveResult move = mission.move(direction);
            switch (move.type()) {
                case BLOCKED -> view.show("You cannot go there.");
                case MOVED -> {
                }
                case WON -> {
                    view.show("Victory! You reached the border.");
                    return Result.WON;
                }
                case ENCOUNTER -> {
                    EncounterFlow flow = encounter(mission, move.enemy());
                    if (flow == EncounterFlow.HERO_DIED) {
                        return Result.HERO_DIED;
                    }
                    if (flow == EncounterFlow.EXIT_APPLICATION) {
                        return Result.EXIT_APPLICATION;
                    }
                }
            }
        }
    }

    private EncounterFlow encounter(Mission mission, Enemy enemy) {
        while (true) {
            view.show("You encountered " + enemy.getName() + ". Fight or run [f/r]?");
            String input = view.readInput();
            if (input == null || isQuit(input)) {
                return EncounterFlow.EXIT_APPLICATION;
            }

            String answer = input.trim().toLowerCase(Locale.ROOT);
            EncounterResult result;
            if ("fight".equals(answer) || "f".equals(answer)) {
                view.show("Combat starts against " + enemy.getName() + ".");
                result = encounters.fight(mission, enemy);
            } else if ("run".equals(answer) || "r".equals(answer)) {
                result = encounters.run(mission, enemy);
            } else {
                view.show("Please answer with fight/f or run/r.");
                continue;
            }

            if (result.type() == EncounterResult.Type.ESCAPED) {
                view.show("You escaped.");
                return EncounterFlow.CONTINUE_MISSION;
            }
            if (result.escapeFailed()) {
                view.show("You failed to escape.");
                view.show("Combat starts against " + enemy.getName() + ".");
            }
            if (result.type() == EncounterResult.Type.HERO_DIED) {
                view.show("You lost the fight.");
                return EncounterFlow.HERO_DIED;
            }

            view.show("You defeated " + enemy.getName() + ".");
            if (result.xpFailure() == null) {
                view.show("You gained " + result.xpReward() + " XP.");
                if (result.levelsGained() > 0) {
                    view.show("Congratulations, you have reached level " + mission.getHero().getLevel() + "!");
                }
            } else {
                view.show("XP could not be applied: " + result.xpFailure());
            }

            if (result.artifact() != null
                && promptForArtifact(mission.getHero(), result.artifact())) {
                return EncounterFlow.EXIT_APPLICATION;
            }
            return EncounterFlow.CONTINUE_MISSION;
        }
    }

    private boolean promptForArtifact(Hero hero, Artifact artifact) {
        while (true) {
            view.show(ViewFormatter.artifactPrompt(hero, artifact));
            String input = view.readInput();
            if (input == null || isQuit(input)) {
                return true;
            }

            String answer = input.trim().toLowerCase(Locale.ROOT);
            if (answer.isEmpty() || "y".equals(answer) || "yes".equals(answer)) {
                Artifact replaced = hero.equip(artifact);
                if (replaced != null) {
                    view.show(ViewFormatter.slotName(replaced.slot()) + " has been discarded.");
                }
                view.show(ViewFormatter.slotName(artifact.slot()) + " has been equipped.");
                return false;
            }
            if ("n".equals(answer) || "no".equals(answer)) {
                view.show(ViewFormatter.slotName(artifact.slot()) + " has been discarded.");
                return false;
            }
            view.show("Please answer with y or n.");
        }
    }

    private boolean isQuit(String input) {
        return "quit".equals(input.trim().toLowerCase(Locale.ROOT));
    }
}

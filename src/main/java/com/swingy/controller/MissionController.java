package com.swingy.controller;

import com.swingy.logic.EncounterAction;
import com.swingy.logic.EncounterResult;
import com.swingy.logic.EncounterService;
import com.swingy.logic.RoomFactory;
import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.Mission;
import com.swingy.model.world.Direction;
import com.swingy.view.GameView;
import com.swingy.view.InputPort;
import com.swingy.view.ViewInput;

import java.util.Locale;
import java.util.Objects;

public final class MissionController {
    private final InputPort input;
    private final GameView view;
    private final RoomFactory roomFactory;
    private final EncounterService encounters;

    public MissionController(
        InputPort input,
        GameView view,
        RoomFactory roomFactory,
        EncounterService encounters
    ) {
        this.input = Objects.requireNonNull(input, "Input is required.");
        this.view = Objects.requireNonNull(view, "View is required.");
        this.roomFactory = Objects.requireNonNull(roomFactory, "Room factory is required.");
        this.encounters = Objects.requireNonNull(encounters, "Encounter service is required.");
    }

    public MissionResult play(Hero hero) {
        Objects.requireNonNull(hero, "Hero is required.");
        Mission mission = new Mission(hero, roomFactory.create(hero));

        while (true) {
            view.renderHero(mission.getHero());
            view.renderRoom(mission.getRoom(), mission.getHeroPosition());

            ViewInput nextInput = input.readInput();
            if (nextInput.type() != ViewInput.Type.LINE) {
                return MissionResult.exit(nextInput);
            }
            if (isQuit(nextInput.line())) {
                return MissionResult.exit(nextInput);
            }

            Direction direction = Direction.parse(nextInput.line());
            if (direction == null) {
                view.showUnknownMove();
                continue;
            }

            Mission.MoveResult move = mission.move(direction);
            switch (move.type()) {
                case BLOCKED -> view.showBlockedMove();
                case MOVED -> {
                }
                case WON -> {
                    view.showVictory();
                    return MissionResult.won();
                }
                case ENCOUNTER -> {
                    EncounterFlow flow = encounter(mission, move.enemy());
                    if (flow.type() == EncounterFlow.Type.HERO_DIED) {
                        return MissionResult.heroDied();
                    }
                    if (flow.type() == EncounterFlow.Type.EXIT_APPLICATION) {
                        return MissionResult.exit(flow.exitInput());
                    }
                }
            }
        }
    }

    private EncounterFlow encounter(Mission mission, Enemy enemy) {
        while (true) {
            view.showEncounter(enemy);
            ViewInput nextInput = input.readInput();
            if (nextInput.type() != ViewInput.Type.LINE || isQuit(nextInput.line())) {
                return EncounterFlow.exit(nextInput);
            }

            EncounterAction action = actionFor(nextInput.line());
            if (action == null) {
                view.showFightOrRunRequired();
                continue;
            }

            EncounterResult result = encounters.resolve(mission, enemy, action);
            if (result.type() == EncounterResult.Type.ESCAPED) {
                view.showEscaped();
                return EncounterFlow.continueMission();
            }
            if (result.escapeFailed()) {
                view.showEscapeFailed();
            }

            view.showCombatStarted(enemy);
            view.renderCombat(mission.getHero(), enemy, result.combat());
            if (result.type() == EncounterResult.Type.HERO_DIED) {
                view.showFightLost();
                return EncounterFlow.heroDied();
            }

            view.showEnemyDefeated(enemy);
            if (result.xpFailure() == null) {
                view.showExperienceGain(
                    result.xpReward(),
                    result.levelsGained(),
                    mission.getHero()
                );
            } else {
                view.showExperienceFailure(result.xpFailure());
            }

            if (result.artifact() != null) {
                ViewInput exitInput = promptForArtifact(mission.getHero(), result.artifact());
                if (exitInput != null) {
                    return EncounterFlow.exit(exitInput);
                }
            }
            return EncounterFlow.continueMission();
        }
    }

    private EncounterAction actionFor(String line) {
        String answer = line.trim().toLowerCase(Locale.ROOT);
        if ("fight".equals(answer) || "f".equals(answer)) {
            return EncounterAction.FIGHT;
        }
        if ("run".equals(answer) || "r".equals(answer)) {
            return EncounterAction.RUN;
        }
        return null;
    }

    private ViewInput promptForArtifact(Hero hero, Artifact artifact) {
        while (true) {
            view.promptForArtifact(hero, artifact);
            ViewInput nextInput = input.readInput();
            if (nextInput.type() != ViewInput.Type.LINE || isQuit(nextInput.line())) {
                return nextInput;
            }

            String answer = nextInput.line().trim().toLowerCase(Locale.ROOT);
            if (answer.isEmpty() || "y".equals(answer) || "yes".equals(answer)) {
                Artifact replaced = hero.equip(artifact);
                if (replaced != null) {
                    view.showArtifactDiscarded(hero, replaced);
                }
                view.showArtifactEquipped(hero, artifact);
                return null;
            }
            if ("n".equals(answer) || "no".equals(answer)) {
                view.showArtifactDiscarded(hero, artifact);
                return null;
            }
            view.showYesNoRequired();
        }
    }

    private boolean isQuit(String inputLine) {
        return "quit".equals(inputLine.trim().toLowerCase(Locale.ROOT));
    }

    private record EncounterFlow(Type type, ViewInput exitInput) {
        private enum Type {
            CONTINUE_MISSION,
            HERO_DIED,
            EXIT_APPLICATION
        }

        private static EncounterFlow continueMission() {
            return new EncounterFlow(Type.CONTINUE_MISSION, null);
        }

        private static EncounterFlow heroDied() {
            return new EncounterFlow(Type.HERO_DIED, null);
        }

        private static EncounterFlow exit(ViewInput input) {
            return new EncounterFlow(Type.EXIT_APPLICATION, input);
        }
    }
}

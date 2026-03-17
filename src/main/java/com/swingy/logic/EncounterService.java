package com.swingy.logic;

import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.GameRules;
import com.swingy.model.Mission;

import java.util.Objects;
import java.util.Random;

public final class EncounterService {
    public static final double RUN_SUCCESS_CHANCE = 0.5;
    public static final double ARTIFACT_DROP_CHANCE = 0.35;

    private final CombatService combat;
    private final Random random;

    public EncounterService(CombatService combat, Random random) {
        this.combat = Objects.requireNonNull(combat, "Combat service is required.");
        this.random = Objects.requireNonNull(random, "Random source is required.");
    }

    public EncounterResult resolve(Mission mission, Enemy enemy, EncounterAction action) {
        Objects.requireNonNull(mission, "Mission is required.");
        Objects.requireNonNull(enemy, "Enemy is required.");
        Objects.requireNonNull(action, "Encounter action is required.");

        boolean escapeFailed = false;
        if (action == EncounterAction.RUN) {
            if (random.nextDouble() < RUN_SUCCESS_CHANCE) {
                mission.retreat();
                return EncounterResult.escaped();
            }
            escapeFailed = true;
        }

        CombatResult combatResult = combat.fight(mission.getHero(), enemy);
        if (!combatResult.heroWon()) {
            return EncounterResult.heroDied(escapeFailed, combatResult);
        }

        mission.getRoom().removeEnemy(enemy);
        long xpReward = GameRules.xpReward(enemy.getLevel());
        int levelsGained = 0;
        String xpFailure = null;
        try {
            levelsGained = mission.getHero().gainExperience(xpReward);
        } catch (ArithmeticException | IllegalStateException exception) {
            xpFailure = exception.getMessage();
        }

        Artifact artifact = random.nextDouble() < ARTIFACT_DROP_CHANCE
            ? createArtifact(enemy)
            : null;
        return EncounterResult.enemyDefeated(
            escapeFailed,
            combatResult,
            xpReward,
            levelsGained,
            xpFailure,
            artifact
        );
    }

    private Artifact createArtifact(Enemy enemy) {
        Artifact.Slot slot = switch (random.nextInt(3)) {
            case 0 -> Artifact.Slot.WEAPON;
            case 1 -> Artifact.Slot.ARMOR;
            default -> Artifact.Slot.HELM;
        };
        return new Artifact(slot, Math.max(0, enemy.getLevel() - 1));
    }
}

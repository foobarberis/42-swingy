package com.swingy.logic;

import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.Mission;

import java.util.Objects;
import java.util.Random;

public final class EncounterService {
    private final Random random;

    public EncounterService(Random random) {
        this.random = Objects.requireNonNull(random, "Random source is required.");
    }

    public EncounterResult fight(Mission mission, Enemy enemy) {
        requireEncounter(mission, enemy);
        return combat(mission, enemy, false);
    }

    public EncounterResult run(Mission mission, Enemy enemy) {
        requireEncounter(mission, enemy);
        if (random.nextBoolean()) {
            mission.retreat();
            return EncounterResult.escaped();
        }
        return combat(mission, enemy, true);
    }

    private EncounterResult combat(Mission mission, Enemy enemy, boolean escapeFailed) {
        Hero hero = mission.getHero();
        while (hero.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            enemy.takeDamage(Math.max(1, hero.getAttack() - enemy.getDefense()));
            if (enemy.getCurrentHp() > 0) {
                hero.takeDamage(Math.max(1, enemy.getAttack() - hero.getDefense()));
            }
        }

        if (hero.getCurrentHp() == 0) {
            return EncounterResult.heroDied(escapeFailed);
        }

        mission.getRoom().removeEnemy(mission.getHeroPosition());
        long xpReward = enemy.getLevel() * 100L;
        int levelsGained = 0;
        String xpFailure = null;
        try {
            levelsGained = hero.gainExperience(xpReward);
        } catch (IllegalStateException exception) {
            xpFailure = exception.getMessage();
        }

        Artifact artifact = random.nextBoolean() ? createArtifact(enemy) : null;
        return EncounterResult.enemyDefeated(
            escapeFailed,
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
        return new Artifact(slot, enemy.getLevel());
    }

    private void requireEncounter(Mission mission, Enemy enemy) {
        Objects.requireNonNull(mission, "Mission is required.");
        Objects.requireNonNull(enemy, "Enemy is required.");
    }
}

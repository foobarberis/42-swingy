package com.swingy.logic;

import com.swingy.model.Artifact;

import java.util.Objects;

public record EncounterResult(
    Type type,
    boolean escapeFailed,
    CombatResult combat,
    long xpReward,
    int levelsGained,
    String xpFailure,
    Artifact artifact
) {
    public enum Type {
        ESCAPED,
        HERO_DIED,
        ENEMY_DEFEATED
    }

    public EncounterResult {
        Objects.requireNonNull(type, "Encounter result type is required.");
        if (type != Type.ESCAPED) {
            Objects.requireNonNull(combat, "Combat result is required.");
        }
    }

    public static EncounterResult escaped() {
        return new EncounterResult(Type.ESCAPED, false, null, 0L, 0, null, null);
    }

    public static EncounterResult heroDied(boolean escapeFailed, CombatResult combat) {
        return new EncounterResult(Type.HERO_DIED, escapeFailed, combat, 0L, 0, null, null);
    }

    public static EncounterResult enemyDefeated(
        boolean escapeFailed,
        CombatResult combat,
        long xpReward,
        int levelsGained,
        String xpFailure,
        Artifact artifact
    ) {
        return new EncounterResult(
            Type.ENEMY_DEFEATED,
            escapeFailed,
            combat,
            xpReward,
            levelsGained,
            xpFailure,
            artifact
        );
    }
}

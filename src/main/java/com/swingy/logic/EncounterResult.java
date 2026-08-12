package com.swingy.logic;

import com.swingy.model.Artifact;

import java.util.Objects;

public record EncounterResult(
    Type type,
    boolean escapeFailed,
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
    }

    public static EncounterResult escaped() {
        return new EncounterResult(Type.ESCAPED, false, 0L, 0, null, null);
    }

    public static EncounterResult heroDied(boolean escapeFailed) {
        return new EncounterResult(Type.HERO_DIED, escapeFailed, 0L, 0, null, null);
    }

    public static EncounterResult enemyDefeated(
        boolean escapeFailed,
        long xpReward,
        int levelsGained,
        String xpFailure,
        Artifact artifact
    ) {
        return new EncounterResult(
            Type.ENEMY_DEFEATED,
            escapeFailed,
            xpReward,
            levelsGained,
            xpFailure,
            artifact
        );
    }
}

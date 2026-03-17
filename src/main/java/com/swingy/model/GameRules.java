package com.swingy.model;

public final class GameRules {
    private static final double EFFECTIVE_MOD_K = 4.17;

    public static final int MAX_LEVEL = 100;
    public static final int MAX_ARTIFACT_MOD = 1_000_000;

    private GameRules() {
    }

    public static int mapSizeForLevel(int level) {
        requirePositiveLevel(level);
        long size = Math.addExact(Math.multiplyExact((long) level - 1L, 5L), 10L);
        size = Math.subtractExact(size, level % 2L);
        return Math.toIntExact(size);
    }

    public static long xpThreshold(int level) {
        requirePositiveLevel(level);
        long adjustedLevel = (long) level - 1L;
        long base = Math.multiplyExact((long) level, 1_000L);
        long square = Math.multiplyExact(adjustedLevel, adjustedLevel);
        return Math.addExact(base, Math.multiplyExact(square, 450L));
    }

    public static long minimumXpForLevel(int level) {
        requirePositiveLevel(level);
        return level == 1 ? 0L : xpThreshold(level - 1);
    }

    public static boolean isExperienceValid(int level, long xp) {
        if (!isSupportedLevel(level) || xp < 0L) {
            return false;
        }
        return xp >= minimumXpForLevel(level) && xp < xpThreshold(level);
    }

    public static long xpReward(int enemyLevel) {
        return xpThreshold(enemyLevel) / 10L;
    }

    public static int effectiveMod(int mod) {
        if (mod < 0) {
            return 0;
        }
        if (mod > MAX_ARTIFACT_MOD) {
            throw new IllegalArgumentException("Artifact modifier is too large.");
        }
        if (mod == 0) {
            return 1;
        }
        return (int) Math.floor(EFFECTIVE_MOD_K * Math.log1p(mod));
    }

    public static int artifactBonus(Artifact artifact) {
        int multiplier = artifact.slot() == Artifact.Slot.HELM ? 5 : 3;
        return Math.multiplyExact(effectiveMod(artifact.mod()), multiplier);
    }

    public static boolean isSupportedLevel(int level) {
        return level >= 1 && level <= MAX_LEVEL;
    }

    public static void requireSupportedLevel(int level) {
        if (!isSupportedLevel(level)) {
            throw new IllegalArgumentException(
                "Level must be between 1 and " + MAX_LEVEL + "."
            );
        }
    }

    private static void requirePositiveLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be positive.");
        }
    }
}

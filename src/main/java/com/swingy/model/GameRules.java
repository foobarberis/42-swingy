package com.swingy.model;

public final class GameRules {
    public static final int MAX_LEVEL = 100;

    private GameRules() {
    }

    public static int mapSizeForLevel(int level) {
        requireSupportedLevel(level);
        return 5 * (level - 1) + 10 - level % 2;
    }

    public static long xpThreshold(int level) {
        requireSupportedLevel(level);
        long previousLevels = level - 1L;
        return 1_000L * level + 450L * previousLevels * previousLevels;
    }

    public static long minimumXpForLevel(int level) {
        requireSupportedLevel(level);
        return level == 1 ? 0L : xpThreshold(level - 1);
    }

    public static boolean isExperienceValid(int level, long xp) {
        return isSupportedLevel(level)
            && xp >= minimumXpForLevel(level)
            && xp < xpThreshold(level);
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
}

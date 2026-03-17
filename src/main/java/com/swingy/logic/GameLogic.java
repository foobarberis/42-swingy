package com.swingy.logic;

import com.swingy.model.Artifact;
import com.swingy.model.GameRules;
import com.swingy.model.Hero;

public final class GameLogic {
    public static final int MAX_LEVEL = GameRules.MAX_LEVEL;
    public static final int MAX_ARTIFACT_MOD = GameRules.MAX_ARTIFACT_MOD;

    private GameLogic() {
    }

    public static int mapSizeForLevel(int level) {
        return GameRules.mapSizeForLevel(level);
    }

    public static long xpThreshold(int level) {
        return GameRules.xpThreshold(level);
    }

    public static long minimumXpForLevel(int level) {
        return GameRules.minimumXpForLevel(level);
    }

    public static boolean isExperienceValid(int level, long xp) {
        return GameRules.isExperienceValid(level, xp);
    }

    public static long xpReward(int enemyLevel) {
        return GameRules.xpReward(enemyLevel);
    }

    public static int effectiveMod(int mod) {
        return GameRules.effectiveMod(mod);
    }

    public static int baseMaxHp(Hero hero) {
        return hero.getHeroClass().baseHp() + (hero.getLevel() - 1) * 10;
    }

    public static int effectiveMaxHp(Hero hero) {
        return hero.getMaxHp();
    }

    public static int artifactBonus(Artifact artifact) {
        return GameRules.artifactBonus(artifact);
    }

    public static boolean isSupportedLevel(int level) {
        return GameRules.isSupportedLevel(level);
    }

    public static void requireSupportedLevel(int level) {
        GameRules.requireSupportedLevel(level);
    }
}

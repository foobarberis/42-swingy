package com.swingy.logic;

import com.swingy.model.Artifact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameLogicTest {
    @Test
    void cumulativeThresholdsMatchSubjectFormula() {
        assertEquals(1_000L, GameLogic.xpThreshold(1));
        assertEquals(2_450L, GameLogic.xpThreshold(2));
        assertEquals(4_800L, GameLogic.xpThreshold(3));
        assertEquals(8_050L, GameLogic.xpThreshold(4));
        assertEquals(12_200L, GameLogic.xpThreshold(5));
    }

    @Test
    void mapSizeMatchesSubjectFormula() {
        assertEquals(9, GameLogic.mapSizeForLevel(1));
        assertEquals(39, GameLogic.mapSizeForLevel(7));
        assertEquals(65, GameLogic.mapSizeForLevel(12));
    }

    @Test
    void levelSupportHasAPracticalUpperBound() {
        assertTrue(GameLogic.isSupportedLevel(GameLogic.MAX_LEVEL));
        assertFalse(GameLogic.isSupportedLevel(GameLogic.MAX_LEVEL + 1));
    }

    @Test
    void invalidOrUnrepresentableLevelsAreRejectedSafely() {
        assertThrows(IllegalArgumentException.class, () -> GameLogic.mapSizeForLevel(0));
        assertThrows(ArithmeticException.class, () -> GameLogic.mapSizeForLevel(Integer.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> GameLogic.xpThreshold(Integer.MAX_VALUE));
        assertFalse(GameLogic.isSupportedLevel(Integer.MAX_VALUE));
    }

    @Test
    void artifactEffectsMatchTheirSlots() {
        assertEquals(0, GameLogic.effectiveMod(-1));
        assertEquals(1, GameLogic.effectiveMod(0));
        assertEquals(3, GameLogic.artifactBonus(new Artifact(Artifact.Slot.WEAPON, 0)));
        assertEquals(3, GameLogic.artifactBonus(new Artifact(Artifact.Slot.ARMOR, 0)));
        assertEquals(5, GameLogic.artifactBonus(new Artifact(Artifact.Slot.HELM, 0)));
    }
}

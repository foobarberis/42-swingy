package com.swingy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameRulesTest {
    @Test
    void mandatoryMapFormulaIsPreserved() {
        assertEquals(9, GameRules.mapSizeForLevel(1));
        assertEquals(15, GameRules.mapSizeForLevel(2));
        assertEquals(39, GameRules.mapSizeForLevel(7));
    }

    @Test
    void mandatoryExperienceFormulaAndCumulativeRangesArePreserved() {
        assertEquals(1_000L, GameRules.xpThreshold(1));
        assertEquals(2_450L, GameRules.xpThreshold(2));
        assertEquals(4_800L, GameRules.xpThreshold(3));
        assertTrue(GameRules.isExperienceValid(2, 1_000));
        assertFalse(GameRules.isExperienceValid(2, 999));
    }

    @Test
    void formulasRejectUnsupportedLevels() {
        assertThrows(IllegalArgumentException.class, () -> GameRules.mapSizeForLevel(0));
        assertThrows(IllegalArgumentException.class, () -> GameRules.xpThreshold(101));
    }
}

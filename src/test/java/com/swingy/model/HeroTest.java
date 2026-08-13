package com.swingy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeroTest {
    @Test
    void classesUseConfiguredBasesAndScaleMultiplicatively() {
        Hero warrior = Hero.createNew("W", HeroClass.WARRIOR);
        assertEquals(18, warrior.getMaxHp());
        assertEquals(6, warrior.getAttack());
        assertEquals(4, warrior.getDefense());

        Hero mage = Hero.restore("M", HeroClass.MAGE, 3, 2_450, 7, 2, 3, 4);
        assertEquals(40, mage.getMaxHp());
        assertEquals(26, mage.getAttack());
        assertEquals(9, mage.getDefense());
    }

    @Test
    void zeroMeansEmptyAndArtifactsAreDirectBonuses() {
        Hero hero = Hero.createNew("R", HeroClass.ROGUE);
        assertNull(hero.getArtifact(Artifact.Slot.WEAPON));
        hero.equip(new Artifact(Artifact.Slot.WEAPON, 4));
        assertEquals(11, hero.getAttack());
        assertThrows(IllegalArgumentException.class, () -> new Artifact(Artifact.Slot.ARMOR, 0));
    }

    @Test
    void levelingPreservesHpAndWeakerHelmCapsIt() {
        Hero hero = Hero.restore("W", HeroClass.WARRIOR, 1, 900, 10, 0, 0, 8);
        assertEquals(1, hero.gainExperience(100));
        assertEquals(10, hero.getCurrentHp());

        Hero healthy = Hero.restore("R", HeroClass.ROGUE, 1, 0, 20, 0, 0, 10);
        healthy.equip(new Artifact(Artifact.Slot.HELM, 1));
        assertEquals(16, healthy.getCurrentHp());
    }

    @Test
    void healingToFullUsesEffectiveMaximumHp() {
        Hero hero = Hero.restore("W", HeroClass.WARRIOR, 2, 1_000, 3, 0, 0, 5);
        hero.healToFull();
        assertEquals(41, hero.getCurrentHp());
    }
}

package com.swingy.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeroTest {

    @Test
    void xpThresholdMatchesFormula() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        assertEquals(1000, hero.xpThreshold(1));
        assertEquals(2450, hero.xpThreshold(2));
        assertEquals(4800, hero.xpThreshold(3));
        assertEquals(8050, hero.xpThreshold(4));
    }

    @Test
    void addXpLevelsUpAndHealsByTenPerLevel() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        hero.damage(50); // 125 -> 75

        hero.addXp(1000);

        assertEquals(2, hero.getLevel());
        assertEquals(0, hero.getXp());
        assertEquals(85, hero.getCurrentHp());
        assertEquals(135, hero.effectiveMaxHp());
    }

    @Test
    void addXpSupportsMultipleLevelUps() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);

        hero.addXp(3450); // 1000 + 2450

        assertEquals(3, hero.getLevel());
        assertEquals(0, hero.getXp());
        assertEquals(145, hero.getCurrentHp());
    }

    @Test
    void effectiveModBoundaries() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);

        assertEquals(0, hero.effectiveMod(-1));
        assertEquals(1, hero.effectiveMod(0));
        assertEquals((int) Math.floor(Hero.EFFECTIVE_MOD_K * Math.log(11.0)), hero.effectiveMod(10));
    }

    @Test
    void statusLineFormatIsStable() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        assertEquals("[Alice | Lv. 1 War. | 125/125 HP 10/10 ATK 20/20 DEF | 0/1000 XP]", hero.statusLine());
    }
}

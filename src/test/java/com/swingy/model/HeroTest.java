package com.swingy.model;

import com.swingy.logic.GameLogic;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeroTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void allHeroClassesStartWithTheirConfiguredStatistics() {
        Hero warrior = Hero.createNew("Warrior", HeroClass.WARRIOR);
        Hero rogue = Hero.createNew("Rogue", HeroClass.ROGUE);
        Hero mage = Hero.createNew("Mage", HeroClass.MAGE);

        assertEquals(125, warrior.getMaxHp());
        assertEquals(10, warrior.getAttack());
        assertEquals(20, warrior.getDefense());
        assertEquals(100, rogue.getMaxHp());
        assertEquals(15, rogue.getAttack());
        assertEquals(15, rogue.getDefense());
        assertEquals(75, mage.getMaxHp());
        assertEquals(20, mage.getAttack());
        assertEquals(10, mage.getDefense());
    }

    @Test
    void experienceRemainsCumulativeAcrossOneAndSeveralLevels() {
        Hero hero = Hero.builder("Alice", HeroClass.WARRIOR)
            .xp(950L)
            .build();

        assertEquals(1, hero.gainExperience(100L));
        assertEquals(2, hero.getLevel());
        assertEquals(1_050L, hero.getXp());

        Hero multiLevel = Hero.createNew("Bob", HeroClass.ROGUE);
        assertEquals(4, multiLevel.gainExperience(9_000L));
        assertEquals(5, multiLevel.getLevel());
        assertEquals(9_000L, multiLevel.getXp());
    }

    @Test
    void progressionPastTheMaximumLevelDoesNotMutateHero() {
        long currentXp = GameLogic.minimumXpForLevel(GameLogic.MAX_LEVEL);
        Hero hero = Hero.builder("Veteran", HeroClass.WARRIOR)
            .level(GameLogic.MAX_LEVEL)
            .xp(currentXp)
            .currentHp(1_115)
            .build();

        long gain = GameLogic.xpThreshold(GameLogic.MAX_LEVEL) - currentXp;
        assertThrows(IllegalStateException.class, () -> hero.gainExperience(gain));
        assertEquals(GameLogic.MAX_LEVEL, hero.getLevel());
        assertEquals(currentXp, hero.getXp());
        assertEquals(1_115, hero.getCurrentHp());
    }

    @Test
    void unrepresentableProgressionDoesNotPartiallyMutateHero() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);

        assertThrows(IllegalStateException.class, () -> hero.gainExperience(Long.MAX_VALUE));
        assertEquals(1, hero.getLevel());
        assertEquals(0L, hero.getXp());
        assertEquals(125, hero.getCurrentHp());
    }

    @Test
    void damageNeverTakesHitPointsBelowZero() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);

        hero.takeDamage(Integer.MAX_VALUE);

        assertEquals(0, hero.getCurrentHp());
        assertThrows(IllegalArgumentException.class, () -> hero.takeDamage(-1));
    }

    @Test
    void everyArtifactSlotChangesItsRequiredStatistic() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);

        assertNull(hero.equip(new Artifact(Artifact.Slot.WEAPON, 0)));
        assertEquals(13, hero.getAttack());
        assertNotNull(hero.getArtifact(Artifact.Slot.WEAPON));

        assertNull(hero.equip(new Artifact(Artifact.Slot.ARMOR, 0)));
        assertEquals(23, hero.getDefense());

        assertNull(hero.equip(new Artifact(Artifact.Slot.HELM, 0)));
        assertEquals(130, hero.getMaxHp());
    }

    @Test
    void replacingHelmCapsCurrentHitPointsAtNewMaximum() {
        Hero hero = Hero.builder("Alice", HeroClass.WARRIOR)
            .currentHp(155)
            .helmMod(4)
            .build();

        Artifact replaced = hero.equip(new Artifact(Artifact.Slot.HELM, 0));

        assertEquals(new Artifact(Artifact.Slot.HELM, 4), replaced);
        assertEquals(130, hero.getMaxHp());
        assertEquals(130, hero.getCurrentHp());
    }

    @Test
    void annotationsRejectInvalidNameAndCrossFieldState() {
        Hero hero = Hero.builder("bad name", HeroClass.MAGE)
            .level(2)
            .xp(10L)
            .currentHp(500)
            .build();

        Set<String> messages = validator.validate(hero).stream()
            .map(violation -> violation.getMessage())
            .collect(Collectors.toSet());

        assertTrue(messages.contains(
            "Hero name must contain 1 to 16 letters, digits, underscores, or hyphens."
        ));
        assertTrue(messages.contains("Hero experience does not match the hero level."));
        assertTrue(messages.contains("Hero hit points exceed the effective maximum."));
    }

    @Test
    void newHeroFactoryReportsUsefulNameErrors() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Hero.createNew("bad name", HeroClass.MAGE)
        );

        assertTrue(exception.getMessage().contains("1 to 16"));
    }
}

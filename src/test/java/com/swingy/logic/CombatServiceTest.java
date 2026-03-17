package com.swingy.logic;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatServiceTest {
    private final CombatService combat = new CombatService();

    @Test
    void combatUsesEffectiveHeroStatisticsAndReturnsRounds() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Enemy enemy = new Enemy("Goblin", 1, 10, 0, 45, new Position(1, 1));

        CombatResult result = combat.fight(hero, enemy);

        assertTrue(result.heroWon());
        assertEquals(0, enemy.getCurrentHp());
        assertEquals(109, hero.getCurrentHp());
        assertEquals(3, result.rounds().size());
    }

    @Test
    void aStrongEnemyCanKillHero() {
        Hero hero = Hero.createNew("Alice", HeroClass.MAGE);
        Enemy enemy = new Enemy("Ogre", 1, 1_000, 1_000, 200, new Position(1, 1));

        CombatResult result = combat.fight(hero, enemy);

        assertFalse(result.heroWon());
        assertEquals(0, hero.getCurrentHp());
    }
}

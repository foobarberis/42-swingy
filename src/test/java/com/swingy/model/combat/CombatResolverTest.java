package com.swingy.model.combat;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatResolverTest {

    private final CombatResolver resolver = new CombatResolver();

    @Test
    void fullMatrixProducesExpectedOutcomes() {
        for (CombatAction enemyAction : CombatAction.values()) {
            if (enemyAction == CombatAction.IDLE) continue;
            for (CombatAction playerAction : CombatAction.values()) {
                Boolean qte = null;
                if (enemyAction == playerAction && playerAction != CombatAction.IDLE) {
                    qte = Boolean.TRUE;
                }

                Hero hero = Hero.createNew("H", HeroClass.WARRIOR);
                Enemy enemy = new Enemy("E", false, 1, 15, 15, 100);
                CombatOutcome out = resolver.resolve(hero, enemy, enemyAction, playerAction, qte);

                assertExpected(hero, enemy, enemyAction, playerAction, qte, out);

                if (enemyAction == playerAction && playerAction != CombatAction.IDLE) {
                    CombatOutcome outFail = resolver.resolve(hero, enemy, enemyAction, playerAction, Boolean.FALSE);
                    assertExpected(hero, enemy, enemyAction, playerAction, Boolean.FALSE, outFail);
                }
            }
        }
    }

    @Test
    void idleInputNeverTriggersQteSemantics() {
        Hero hero = Hero.createNew("H", HeroClass.WARRIOR);
        Enemy enemy = new Enemy("E", false, 1, 15, 15, 100);

        CombatOutcome out = resolver.resolve(hero, enemy, CombatAction.ATTACK, CombatAction.IDLE, Boolean.TRUE);
        assertTrue(out.heroDamage > 0);
        assertEquals(0, out.enemyDamage);
    }

    @Test
    void armorBrokenAppliesDefenseReductionForDamageCalculation() {
        Hero normal = Hero.createNew("H1", HeroClass.WARRIOR);
        Hero broken = Hero.createNew("H2", HeroClass.WARRIOR);
        Enemy enemy = new Enemy("E", false, 1, 15, 15, 100);

        broken.debuffState().applyArmorBrokenForNextRound();
        broken.debuffState().beginRound();

        CombatOutcome normalOut = resolver.resolve(normal, enemy, CombatAction.ATTACK, CombatAction.IDLE, null);
        CombatOutcome brokenOut = resolver.resolve(broken, enemy, CombatAction.ATTACK, CombatAction.IDLE, null);

        assertTrue(brokenOut.heroDamage > normalOut.heroDamage);
    }

    @Test
    void sunderVsSunderSetsArmorBrokenFlags() {
        Hero hero = Hero.createNew("H", HeroClass.WARRIOR);
        Enemy enemy = new Enemy("E", false, 1, 15, 15, 100);

        CombatOutcome qteSuccess = resolver.resolve(hero, enemy, CombatAction.SUNDER, CombatAction.SUNDER, Boolean.TRUE);
        assertTrue(qteSuccess.applyArmorBrokenToEnemy);
        assertFalse(qteSuccess.applyArmorBrokenToHero);

        CombatOutcome qteFailure = resolver.resolve(hero, enemy, CombatAction.SUNDER, CombatAction.SUNDER, Boolean.FALSE);
        assertTrue(qteFailure.applyArmorBrokenToHero);
        assertFalse(qteFailure.applyArmorBrokenToEnemy);
    }

    private void assertExpected(Hero hero,
                                Enemy enemy,
                                CombatAction enemyAction,
                                CombatAction playerAction,
                                Boolean qte,
                                CombatOutcome out) {
        int heroHeal = Math.max(1, (int) Math.floor(hero.baseMaxHp() * 0.10));
        int enemyHeal = Math.max(1, (int) Math.floor(enemy.getMaxHp() * 0.10));

        int expectedHeroDamage = 0;
        int expectedEnemyDamage = 0;
        int expectedHeroHeal = 0;
        int expectedEnemyHeal = 0;
        boolean expectedBrokenHero = false;
        boolean expectedBrokenEnemy = false;

        if (playerAction == CombatAction.IDLE) {
            if (enemyAction == CombatAction.ATTACK || enemyAction == CombatAction.SUNDER) {
                expectedHeroDamage = damage(enemy.getAtk(), hero.effectiveDef(), false, 1.0);
            } else if (enemyAction == CombatAction.DEFEND) {
                expectedEnemyHeal = enemyHeal;
            }
        } else {
            switch (enemyAction) {
                case ATTACK -> {
                    switch (playerAction) {
                        case ATTACK -> {
                            if (Boolean.TRUE.equals(qte)) {
                                expectedEnemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), false, 1.5);
                            } else {
                                expectedHeroDamage = damage(enemy.getAtk(), hero.effectiveDef(), false, 1.5);
                            }
                        }
                        case DEFEND -> expectedEnemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), false, 1.0);
                        case SUNDER -> expectedHeroDamage = damage(enemy.getAtk(), hero.effectiveDef(), false, 1.5);
                        default -> {
                        }
                    }
                }
                case DEFEND -> {
                    switch (playerAction) {
                        case ATTACK -> expectedHeroDamage = damage(enemy.getAtk(), hero.effectiveDef(), false, 1.0);
                        case DEFEND -> {
                            if (Boolean.TRUE.equals(qte)) {
                                expectedHeroHeal = heroHeal;
                            } else {
                                expectedEnemyHeal = enemyHeal;
                            }
                        }
                        case SUNDER -> expectedEnemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), false, 1.0);
                        default -> {
                        }
                    }
                }
                case SUNDER -> {
                    switch (playerAction) {
                        case ATTACK -> expectedEnemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), false, 1.5);
                        case DEFEND -> expectedHeroDamage = damage(enemy.getAtk(), hero.effectiveDef(), false, 1.0);
                        case SUNDER -> {
                            if (Boolean.TRUE.equals(qte)) {
                                expectedBrokenEnemy = true;
                            } else {
                                expectedBrokenHero = true;
                            }
                        }
                        default -> {
                        }
                    }
                }
                default -> {
                }
            }
        }

        assertEquals(expectedHeroDamage, out.heroDamage);
        assertEquals(expectedEnemyDamage, out.enemyDamage);
        assertEquals(expectedHeroHeal, out.heroHeal);
        assertEquals(expectedEnemyHeal, out.enemyHeal);
        assertEquals(expectedBrokenHero, out.applyArmorBrokenToHero);
        assertEquals(expectedBrokenEnemy, out.applyArmorBrokenToEnemy);
    }

    private int damage(int atk, int def, boolean armorBroken, double multiplier) {
        int effectiveDef = armorBroken ? (int) Math.floor(def * 0.7) : def;
        int base = Math.max(1, (atk * 200) / (100 + effectiveDef));
        return (int) Math.floor(base * multiplier);
    }
}

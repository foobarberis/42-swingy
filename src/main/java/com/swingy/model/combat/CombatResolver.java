package com.swingy.model.combat;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;

public class CombatResolver {
    public CombatOutcome resolve(Hero hero, Enemy enemy, CombatAction enemyAction, CombatAction playerAction, Boolean qteSuccess) {
        CombatOutcome out = new CombatOutcome();
        int healAmountHero = Math.max(1, (int) Math.floor(hero.baseMaxHp() * 0.10));
        int healAmountEnemy = Math.max(1, (int) Math.floor(enemy.getMaxHp() * 0.10));

        if (playerAction == CombatAction.IDLE) {
            switch (enemyAction) {
                case ATTACK, SUNDER -> out.heroDamage = damage(enemy.getAtk(), hero.effectiveDef(), hero.debuffState().isArmorBroken(), 1.0);
                case DEFEND -> out.enemyHeal = healAmountEnemy;
                default -> {
                }
            }
            return out;
        }

        switch (enemyAction) {
            case ATTACK -> {
                switch (playerAction) {
                    case ATTACK -> {
                        if (Boolean.TRUE.equals(qteSuccess)) {
                            out.enemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), enemy.debuffState().isArmorBroken(), 1.5);
                        } else {
                            out.heroDamage = damage(enemy.getAtk(), hero.effectiveDef(), hero.debuffState().isArmorBroken(), 1.5);
                        }
                    }
                    case DEFEND -> out.enemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), enemy.debuffState().isArmorBroken(), 1.0);
                    case SUNDER -> out.heroDamage = damage(enemy.getAtk(), hero.effectiveDef(), hero.debuffState().isArmorBroken(), 1.5);
                    default -> {
                    }
                }
            }
            case DEFEND -> {
                switch (playerAction) {
                    case ATTACK -> out.heroDamage = damage(enemy.getAtk(), hero.effectiveDef(), hero.debuffState().isArmorBroken(), 1.0);
                    case DEFEND -> {
                        if (Boolean.TRUE.equals(qteSuccess)) {
                            out.heroHeal = healAmountHero;
                        } else {
                            out.enemyHeal = healAmountEnemy;
                        }
                    }
                    case SUNDER -> out.enemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), enemy.debuffState().isArmorBroken(), 1.0);
                    default -> {
                    }
                }
            }
            case SUNDER -> {
                switch (playerAction) {
                    case ATTACK -> out.enemyDamage = damage(hero.effectiveAtk(), enemy.getDef(), enemy.debuffState().isArmorBroken(), 1.5);
                    case DEFEND -> out.heroDamage = damage(enemy.getAtk(), hero.effectiveDef(), hero.debuffState().isArmorBroken(), 1.0);
                    case SUNDER -> {
                        if (Boolean.TRUE.equals(qteSuccess)) {
                            out.applyArmorBrokenToEnemy = true;
                        } else {
                            out.applyArmorBrokenToHero = true;
                        }
                    }
                    default -> {
                    }
                }
            }
            default -> {
            }
        }
        return out;
    }

    private int damage(int atk, int def, boolean armorBroken, double multiplier) {
        int effectiveDef = armorBroken ? (int) Math.floor(def * 0.7) : def;
        int base = Math.max(1, (atk * 200) / (100 + effectiveDef));
        return (int) Math.floor(base * multiplier);
    }
}

package com.swingy.logic;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CombatService {
    public CombatResult fight(Hero hero, Enemy enemy) {
        Objects.requireNonNull(hero, "Hero is required.");
        Objects.requireNonNull(enemy, "Enemy is required.");

        List<CombatRound> rounds = new ArrayList<>();
        int roundNumber = 1;
        while (hero.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
            int heroDamage = damage(hero.getAttack(), 200, enemy.getDef());
            enemy.takeDamage(heroDamage);

            int enemyDamage = 0;
            if (enemy.getCurrentHp() > 0) {
                enemyDamage = damage(enemy.getAtk(), 100, hero.getDefense());
                hero.takeDamage(enemyDamage);
            }

            rounds.add(
                new CombatRound(
                    roundNumber,
                    heroDamage,
                    enemyDamage,
                    hero.getCurrentHp(),
                    enemy.getCurrentHp()
                )
            );
            roundNumber++;
        }
        return new CombatResult(hero.getCurrentHp() > 0, rounds);
    }

    private int damage(int attack, int multiplier, int defense) {
        long numerator = Math.multiplyExact((long) attack, multiplier);
        long denominator = Math.addExact(100L, defense);
        return Math.toIntExact(Math.max(1L, numerator / denominator));
    }
}

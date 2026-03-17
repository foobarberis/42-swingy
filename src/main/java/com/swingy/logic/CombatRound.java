package com.swingy.logic;

public record CombatRound(
    int number,
    int heroDamage,
    int enemyDamage,
    int heroHp,
    int enemyHp
) {
}

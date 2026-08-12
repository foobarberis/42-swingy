package com.swingy.model;

import java.util.Objects;

public final class Enemy {
    private final EnemyType type;
    private final int level;
    private int currentHp;

    public Enemy(EnemyType type, int level) {
        this.type = Objects.requireNonNull(type, "Enemy type is required.");
        GameRules.requireSupportedLevel(level);
        this.level = level;
        currentHp = getMaxHp();
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        currentHp = Math.max(0, currentHp - damage);
    }

    public EnemyType getType() {
        return type;
    }

    public String getName() {
        return type.displayName();
    }

    public int getLevel() {
        return level;
    }

    public int getMaxHp() {
        return type.baseHp() * level;
    }

    public int getAttack() {
        return type.baseAttack() * level;
    }

    public int getDefense() {
        return type.baseDefense() * level;
    }

    public int getCurrentHp() {
        return currentHp;
    }
}

package com.swingy.model;

public enum EnemyType {
    GOBLIN(10, 5, 1),
    ORC(10, 3, 3),
    TROLL(10, 1, 5);

    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;

    EnemyType(int baseHp, int baseAttack, int baseDefense) {
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
    }

    public int baseHp() {
        return baseHp;
    }

    public int baseAttack() {
        return baseAttack;
    }

    public int baseDefense() {
        return baseDefense;
    }

    public String displayName() {
        return switch (this) {
            case GOBLIN -> "Goblin";
            case ORC -> "Orc";
            case TROLL -> "Troll";
        };
    }
}

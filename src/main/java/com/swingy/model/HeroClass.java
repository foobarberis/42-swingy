package com.swingy.model;

public enum HeroClass {
    WARRIOR(125, 10, 20),
    ROGUE(100, 15, 15),
    MAGE(75, 20, 10);

    private final int hp;
    private final int atk;
    private final int def;

    HeroClass(int hp, int atk, int def) {
        this.hp = hp;
        this.atk = atk;
        this.def = def;
    }

    public int baseHp() {
        return hp;
    }

    public int baseAtk() {
        return atk;
    }

    public int baseDef() {
        return def;
    }

    public static HeroClass fromCreateToken(String token) {
        return switch (token) {
            case "warrior" -> WARRIOR;
            case "rogue" -> ROGUE;
            case "mage" -> MAGE;
            default -> null;
        };
    }
}

package com.swingy.model;

public class Weapon extends Artifact {
    public Weapon(int mod) {
        super(mod);
    }

    @Override
    public String baseName(HeroClass heroClass) {
        return switch (heroClass) {
            case WARRIOR -> "Sword";
            case ROGUE -> "Dagger";
            case MAGE -> "Staff";
        };
    }
}

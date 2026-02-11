package com.swingy.model;

public class Armor extends Artifact {
    public Armor(int mod) {
        super(mod);
    }

    @Override
    public String baseName(HeroClass heroClass) {
        return switch (heroClass) {
            case WARRIOR -> "Plate Armor";
            case ROGUE -> "Leather Armor";
            case MAGE -> "Robe";
        };
    }
}

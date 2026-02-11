package com.swingy.model;

public class Helm extends Artifact {
    public Helm(int mod) {
        super(mod);
    }

    @Override
    public String baseName(HeroClass heroClass) {
        return switch (heroClass) {
            case WARRIOR -> "Steel Helm";
            case ROGUE -> "Leather Helm";
            case MAGE -> "Wizard Hat";
        };
    }
}

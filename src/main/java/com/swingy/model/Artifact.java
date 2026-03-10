package com.swingy.model;

public abstract class Artifact {
    protected final int mod;

    protected Artifact(int mod) {
        this.mod = mod;
    }

    public int mod() {
        return mod;
    }

    public abstract String baseName(HeroClass heroClass);

    public String displayName(HeroClass heroClass) {
        return baseName(heroClass) + " +" + mod;
    }

    public static final class Weapon extends Artifact {
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

    public static final class Armor extends Artifact {
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

    public static final class Helm extends Artifact {
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
}

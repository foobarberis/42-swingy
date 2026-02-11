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
}

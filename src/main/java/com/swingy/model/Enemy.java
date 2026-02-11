package com.swingy.model;

import com.swingy.model.combat.DebuffState;

public class Enemy {
    private final String name;
    private final boolean unique;
    private final int level;
    private final int atk;
    private final int def;
    private final int maxHp;
    private int currentHp;
    private final DebuffState debuffState = new DebuffState();

    public Enemy(String name, boolean unique, int level, int atk, int def, int maxHp) {
        this.name = name;
        this.unique = unique;
        this.level = level;
        this.atk = atk;
        this.def = def;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    public void damage(int amount) { currentHp = Math.max(0, currentHp - amount); }
    public void heal(int amount) { currentHp = Math.min(maxHp, currentHp + amount); }

    public String getName() { return name; }
    public boolean isUnique() { return unique; }
    public int getLevel() { return level; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public DebuffState debuffState() { return debuffState; }
}

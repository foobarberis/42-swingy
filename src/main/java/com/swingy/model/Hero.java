package com.swingy.model;

import com.swingy.model.combat.DebuffState;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class Hero {
    public static final double EFFECTIVE_MOD_K = 4.17;

    @Pattern(regexp = "[A-Za-z0-9_-]{1,16}")
    private String name;

    @NotNull
    private HeroClass heroClass;

    @Min(0)
    private int level;

    @Min(0)
    private int xp;

    @Min(0)
    private int currentHp;

    @Min(0)
    private int weaponMod;

    @Min(0)
    private int armorMod;

    @Min(0)
    private int helmMod;

    private final DebuffState debuffState = new DebuffState();

    public Hero() {
    }

    public Hero(String name, HeroClass heroClass, int level, int xp, int currentHp, int weaponMod, int armorMod, int helmMod) {
        this.name = name;
        this.heroClass = heroClass;
        this.level = level;
        this.xp = xp;
        this.currentHp = currentHp;
        this.weaponMod = weaponMod;
        this.armorMod = armorMod;
        this.helmMod = helmMod;
    }

    public static Hero createNew(String name, HeroClass heroClass) {
        Hero hero = new Hero(name, heroClass, 0, 0, heroClass.baseHp(), 0, 0, 0);
        hero.currentHp = hero.effectiveMaxHp();
        return hero;
    }

    public int baseAtk() { return heroClass.baseAtk() + level * 5; }
    public int baseDef() { return heroClass.baseDef() + level * 5; }
    public int baseMaxHp() { return heroClass.baseHp() + level * 10; }

    public int effectiveAtk() { return baseAtk() + 3 * effectiveMod(weaponMod); }
    public int effectiveDef() { return baseDef() + 3 * effectiveMod(armorMod); }
    public int effectiveMaxHp() { return baseMaxHp() + 5 * effectiveMod(helmMod); }

    public int effectiveMod(int mod) {
        return (int) Math.floor(EFFECTIVE_MOD_K * Math.log(1.0 + mod));
    }

    public int xpThreshold(int targetLevel) {
        return targetLevel * 1000 + (targetLevel - 1) * (targetLevel - 1) * 450;
    }

    public void addXp(int gain) {
        xp += gain;
        int levelsGained = 0;
        while (xp >= xpThreshold(level + 1)) {
            level++;
            levelsGained++;
        }
        if (levelsGained > 0) {
            currentHp = Math.min(effectiveMaxHp(), currentHp + levelsGained * 10);
        }
    }

    public void heal(int amount) {
        currentHp = Math.min(effectiveMaxHp(), currentHp + amount);
    }

    public void damage(int amount) {
        currentHp = Math.max(0, currentHp - amount);
    }

    public void capHp() {
        currentHp = Math.min(currentHp, effectiveMaxHp());
    }

    public String statusLine() {
        return "[Lv. " + level + " " + heroClass.abbr() + " | " + currentHp + "/" + baseMaxHp() + " HP " +
                effectiveAtk() + "/" + baseAtk() + " ATK " + effectiveDef() + "/" + baseDef() + " DEF | " + xp + "/" +
                xpThreshold(level + 1) + " XP]";
    }

    public DebuffState debuffState() { return debuffState; }
    public String getName() { return name; }
    public HeroClass getHeroClass() { return heroClass; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getCurrentHp() { return currentHp; }
    public int getWeaponMod() { return weaponMod; }
    public int getArmorMod() { return armorMod; }
    public int getHelmMod() { return helmMod; }

    public void setName(String name) { this.name = name; }
    public void setHeroClass(HeroClass heroClass) { this.heroClass = heroClass; }
    public void setLevel(int level) { this.level = level; }
    public void setXp(int xp) { this.xp = xp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public void setWeaponMod(int weaponMod) { this.weaponMod = weaponMod; }
    public void setArmorMod(int armorMod) { this.armorMod = armorMod; }
    public void setHelmMod(int helmMod) { this.helmMod = helmMod; }
}

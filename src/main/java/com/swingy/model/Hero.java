package com.swingy.model;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

public final class Hero {
    private static final String NAME_PATTERN = "[A-Za-z0-9_-]{1,16}";

    @NotNull(message = "Hero name is required.")
    @Pattern(
        regexp = NAME_PATTERN,
        message = "Hero name must contain 1 to 16 letters, digits, underscores, or hyphens."
    )
    private final String name;

    @NotNull(message = "Hero class is required.")
    private final HeroClass heroClass;

    @Min(value = 1, message = "Hero level must be at least 1.")
    @Max(value = GameRules.MAX_LEVEL, message = "Hero level is too large.")
    private int level;

    @Min(value = 0, message = "Hero experience cannot be negative.")
    private long xp;

    @Min(value = 1, message = "A saved hero must have at least 1 hit point.")
    private int currentHp;

    @Min(value = 0, message = "Weapon modifier cannot be negative.")
    @Max(value = GameRules.MAX_LEVEL, message = "Weapon modifier is too large.")
    private int weaponModifier;

    @Min(value = 0, message = "Armor modifier cannot be negative.")
    @Max(value = GameRules.MAX_LEVEL, message = "Armor modifier is too large.")
    private int armorModifier;

    @Min(value = 0, message = "Helm modifier cannot be negative.")
    @Max(value = GameRules.MAX_LEVEL, message = "Helm modifier is too large.")
    private int helmModifier;

    private Hero(
        String name,
        HeroClass heroClass,
        int level,
        long xp,
        int currentHp,
        int weaponModifier,
        int armorModifier,
        int helmModifier
    ) {
        this.name = Objects.requireNonNull(name, "Hero name is required.");
        this.heroClass = Objects.requireNonNull(heroClass, "Hero class is required.");
        this.level = level;
        this.xp = xp;
        this.currentHp = currentHp;
        this.weaponModifier = weaponModifier;
        this.armorModifier = armorModifier;
        this.helmModifier = helmModifier;
    }

    public static Hero createNew(String name, HeroClass heroClass) {
        Objects.requireNonNull(name, "Hero name is required.");
        Objects.requireNonNull(heroClass, "Hero class is required.");
        return new Hero(name, heroClass, 1, 0L, heroClass.baseHp(), 0, 0, 0);
    }

    public static Hero restore(
        String name,
        HeroClass heroClass,
        int level,
        long xp,
        int currentHp,
        int weaponModifier,
        int armorModifier,
        int helmModifier
    ) {
        return new Hero(
            name,
            heroClass,
            level,
            xp,
            currentHp,
            weaponModifier,
            armorModifier,
            helmModifier
        );
    }

    @AssertTrue(message = "Hero experience does not match the hero level.")
    public boolean isExperienceConsistent() {
        if (!GameRules.isSupportedLevel(level) || xp < 0L) {
            return true;
        }
        return GameRules.isExperienceValid(level, xp);
    }

    @AssertTrue(message = "Hero hit points exceed the effective maximum.")
    public boolean isHitPointTotalValid() {
        if (heroClass == null
            || !GameRules.isSupportedLevel(level)
            || helmModifier < 0
            || helmModifier > GameRules.MAX_LEVEL
            || currentHp < 0) {
            return true;
        }
        return currentHp <= getMaxHp();
    }

    public int gainExperience(long amount) {
        if (amount < 0L) {
            throw new IllegalArgumentException("Experience gain cannot be negative.");
        }
        if (!GameRules.isExperienceValid(level, xp)) {
            throw new IllegalStateException("Current hero experience is invalid.");
        }

        long nextXp = xp + amount;
        int nextLevel = level;
        while (nextXp >= GameRules.xpThreshold(nextLevel)) {
            if (nextLevel == GameRules.MAX_LEVEL) {
                throw new IllegalStateException("The maximum supported level has been reached.");
            }
            nextLevel++;
        }

        int levelsGained = nextLevel - level;
        level = nextLevel;
        xp = nextXp;
        return levelsGained;
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        currentHp = Math.max(0, currentHp - damage);
    }

    public Artifact equip(Artifact artifact) {
        Objects.requireNonNull(artifact, "Artifact is required.");
        Artifact replaced = getArtifact(artifact.slot());
        switch (artifact.slot()) {
            case WEAPON -> weaponModifier = artifact.modifier();
            case ARMOR -> armorModifier = artifact.modifier();
            case HELM -> helmModifier = artifact.modifier();
        }
        currentHp = Math.min(currentHp, getMaxHp());
        return replaced;
    }

    public Artifact getArtifact(Artifact.Slot slot) {
        Objects.requireNonNull(slot, "Artifact slot is required.");
        int modifier = switch (slot) {
            case WEAPON -> weaponModifier;
            case ARMOR -> armorModifier;
            case HELM -> helmModifier;
        };
        return modifier == 0 ? null : new Artifact(slot, modifier);
    }

    public int getAttack() {
        return heroClass.baseAttack() * level + weaponModifier;
    }

    public int getDefense() {
        return heroClass.baseDefense() * level + armorModifier;
    }

    public int getMaxHp() {
        return heroClass.baseHp() * level + helmModifier;
    }

    public String getName() {
        return name;
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public int getLevel() {
        return level;
    }

    public long getXp() {
        return xp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getWeaponModifier() {
        return weaponModifier;
    }

    public int getArmorModifier() {
        return armorModifier;
    }

    public int getHelmModifier() {
        return helmModifier;
    }
}

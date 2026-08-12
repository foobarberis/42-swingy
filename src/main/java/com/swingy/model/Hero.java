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

    @Min(value = -1, message = "Weapon modifier cannot be below -1.")
    @Max(value = GameRules.MAX_ARTIFACT_MOD, message = "Weapon modifier is too large.")
    private int weaponMod;

    @Min(value = -1, message = "Armor modifier cannot be below -1.")
    @Max(value = GameRules.MAX_ARTIFACT_MOD, message = "Armor modifier is too large.")
    private int armorMod;

    @Min(value = -1, message = "Helm modifier cannot be below -1.")
    @Max(value = GameRules.MAX_ARTIFACT_MOD, message = "Helm modifier is too large.")
    private int helmMod;

    private Hero(Builder builder) {
        name = builder.name;
        heroClass = builder.heroClass;
        level = builder.level;
        xp = builder.xp;
        currentHp = builder.currentHp;
        weaponMod = builder.weaponMod;
        armorMod = builder.armorMod;
        helmMod = builder.helmMod;
    }

    public static Builder builder(String name, HeroClass heroClass) {
        return new Builder(name, heroClass);
    }

    public static Hero createNew(String name, HeroClass heroClass) {
        if (name == null) {
            throw new IllegalArgumentException("Hero name is required.");
        }
        if (!name.matches(NAME_PATTERN)) {
            throw new IllegalArgumentException(
                "Hero name must contain 1 to 16 letters, digits, underscores, or hyphens."
            );
        }
        return builder(name, heroClass).build();
    }

    public Hero copy() {
        return builder(name, heroClass)
            .level(level)
            .xp(xp)
            .currentHp(currentHp)
            .weaponMod(weaponMod)
            .armorMod(armorMod)
            .helmMod(helmMod)
            .build();
    }

    public static final class Builder {
        private final String name;
        private final HeroClass heroClass;

        private int level = 1;
        private long xp;
        private int currentHp;
        private int weaponMod = -1;
        private int armorMod = -1;
        private int helmMod = -1;

        private Builder(String name, HeroClass heroClass) {
            this.name = Objects.requireNonNull(name, "Hero name is required.");
            this.heroClass = Objects.requireNonNull(heroClass, "Hero class is required.");
            currentHp = heroClass.baseHp();
        }

        public Builder level(int value) {
            level = value;
            return this;
        }

        public Builder xp(long value) {
            xp = value;
            return this;
        }

        public Builder currentHp(int value) {
            currentHp = value;
            return this;
        }

        public Builder weaponMod(int value) {
            weaponMod = value;
            return this;
        }

        public Builder armorMod(int value) {
            armorMod = value;
            return this;
        }

        public Builder helmMod(int value) {
            helmMod = value;
            return this;
        }

        public Hero build() {
            return new Hero(this);
        }
    }

    @AssertTrue(message = "Hero level cannot be represented safely.")
    public boolean isLevelSupported() {
        return level < 1 || GameRules.isSupportedLevel(level);
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
            || helmMod < -1
            || helmMod > GameRules.MAX_ARTIFACT_MOD
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

        long nextXp = Math.addExact(xp, amount);
        int nextLevel = level;
        while (nextXp >= GameRules.xpThreshold(nextLevel)) {
            int candidateLevel = Math.incrementExact(nextLevel);
            if (!GameRules.isSupportedLevel(candidateLevel)) {
                throw new IllegalStateException(
                    "The next level cannot be represented safely."
                );
            }
            nextLevel = candidateLevel;
        }

        int levelsGained = nextLevel - level;
        int hpGain = Math.multiplyExact(levelsGained, 10);
        int nextHp = Math.min(
            maxHpAtLevel(nextLevel),
            Math.addExact(currentHp, hpGain)
        );
        level = nextLevel;
        xp = nextXp;
        currentHp = nextHp;
        return levelsGained;
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        currentHp = Math.max(0, currentHp - damage);
    }

    public void healToFull() {
        currentHp = getMaxHp();
    }

    public Artifact equip(Artifact artifact) {
        Objects.requireNonNull(artifact, "Artifact is required.");
        Artifact replaced = getArtifact(artifact.slot());
        switch (artifact.slot()) {
            case WEAPON -> weaponMod = artifact.mod();
            case ARMOR -> armorMod = artifact.mod();
            case HELM -> helmMod = artifact.mod();
        }
        currentHp = Math.min(currentHp, getMaxHp());
        return replaced;
    }

    public Artifact getArtifact(Artifact.Slot slot) {
        Objects.requireNonNull(slot, "Artifact slot is required.");
        int mod = switch (slot) {
            case WEAPON -> weaponMod;
            case ARMOR -> armorMod;
            case HELM -> helmMod;
        };
        return mod < 0 ? null : new Artifact(slot, mod);
    }

    private int baseAttack() {
        return Math.addExact(
            heroClass.baseAtk(),
            Math.multiplyExact(level - 1, 5)
        );
    }

    public int getAttack() {
        return Math.addExact(
            baseAttack(),
            Math.multiplyExact(GameRules.effectiveMod(weaponMod), 3)
        );
    }

    private int baseDefense() {
        return Math.addExact(
            heroClass.baseDef(),
            Math.multiplyExact(level - 1, 5)
        );
    }

    public int getDefense() {
        return Math.addExact(
            baseDefense(),
            Math.multiplyExact(GameRules.effectiveMod(armorMod), 3)
        );
    }

    public int getMaxHp() {
        return maxHpAtLevel(level);
    }

    private int baseMaxHpAtLevel(int targetLevel) {
        return Math.addExact(
            heroClass.baseHp(),
            Math.multiplyExact(targetLevel - 1, 10)
        );
    }

    private int maxHpAtLevel(int targetLevel) {
        return Math.addExact(
            baseMaxHpAtLevel(targetLevel),
            Math.multiplyExact(GameRules.effectiveMod(helmMod), 5)
        );
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

    public int getWeaponMod() {
        return weaponMod;
    }

    public int getArmorMod() {
        return armorMod;
    }

    public int getHelmMod() {
        return helmMod;
    }
}

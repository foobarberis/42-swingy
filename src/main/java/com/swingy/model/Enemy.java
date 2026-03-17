package com.swingy.model;

import com.swingy.model.world.Position;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public final class Enemy {
    @NotBlank(message = "Enemy name is required.")
    private final String name;

    @Min(1)
    private final int level;

    @Min(0)
    private final int atk;

    @Min(0)
    private final int def;

    @Min(0)
    private int currentHp;

    @NotNull
    private final Position position;

    public Enemy(String name, int level, int atk, int def, int currentHp, Position position) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enemy name is required.");
        }
        GameRules.requireSupportedLevel(level);
        if (atk < 0 || def < 0 || currentHp <= 0) {
            throw new IllegalArgumentException("Enemy statistics must be non-negative and HP positive.");
        }
        this.name = name;
        this.level = level;
        this.atk = atk;
        this.def = def;
        this.currentHp = currentHp;
        this.position = Objects.requireNonNull(position, "Enemy position is required.");
    }

    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        currentHp = Math.max(0, currentHp - damage);
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public Position getPosition() {
        return position;
    }
}

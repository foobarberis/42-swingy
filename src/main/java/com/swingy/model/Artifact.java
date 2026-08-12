package com.swingy.model;

import java.util.Objects;

public record Artifact(Slot slot, int modifier) {
    public enum Slot {
        WEAPON,
        ARMOR,
        HELM
    }

    public Artifact {
        Objects.requireNonNull(slot, "Artifact slot is required.");
        if (modifier < 1 || modifier > GameRules.MAX_LEVEL) {
            throw new IllegalArgumentException(
                "Artifact modifier must be between 1 and " + GameRules.MAX_LEVEL + "."
            );
        }
    }
}

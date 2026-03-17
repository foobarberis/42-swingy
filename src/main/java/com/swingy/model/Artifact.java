package com.swingy.model;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public record Artifact(
    @NotNull(message = "Artifact slot is required.") Slot slot,
    @Min(value = 0, message = "Artifact modifier cannot be negative.")
    @Max(value = GameRules.MAX_ARTIFACT_MOD, message = "Artifact modifier is too large.") int mod
) {
    public enum Slot {
        WEAPON,
        ARMOR,
        HELM
    }

    public Artifact {
        Objects.requireNonNull(slot, "Artifact slot is required.");
        if (mod < 0 || mod > GameRules.MAX_ARTIFACT_MOD) {
            throw new IllegalArgumentException(
                "Artifact modifier must be between 0 and " + GameRules.MAX_ARTIFACT_MOD + "."
            );
        }
    }
}

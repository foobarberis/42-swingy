package com.swingy.controller;

import com.swingy.view.ViewInput;

import java.util.Objects;

public record MissionResult(Type type, ViewInput exitInput) {
    public enum Type {
        WON,
        HERO_DIED,
        EXIT_APPLICATION
    }

    public MissionResult {
        Objects.requireNonNull(type, "Mission result type is required.");
        if (type == Type.EXIT_APPLICATION) {
            Objects.requireNonNull(exitInput, "Exit input is required.");
        }
    }

    public static MissionResult won() {
        return new MissionResult(Type.WON, null);
    }

    public static MissionResult heroDied() {
        return new MissionResult(Type.HERO_DIED, null);
    }

    public static MissionResult exit(ViewInput input) {
        return new MissionResult(Type.EXIT_APPLICATION, input);
    }
}

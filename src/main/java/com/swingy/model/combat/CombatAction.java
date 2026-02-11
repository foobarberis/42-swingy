package com.swingy.model.combat;

public enum CombatAction {
    ATTACK,
    DEFEND,
    SUNDER,
    IDLE;

    public static CombatAction fromInput(String input) {
        if (input == null) {
            return IDLE;
        }
        return switch (input.trim()) {
            case "attack", "a" -> ATTACK;
            case "defend", "d" -> DEFEND;
            case "sunder", "s" -> SUNDER;
            default -> null;
        };
    }
}

package com.swingy.logic;

import java.util.List;

public record CombatResult(boolean heroWon, List<CombatRound> rounds) {
    public CombatResult {
        rounds = List.copyOf(rounds);
    }
}

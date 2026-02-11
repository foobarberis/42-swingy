package com.swingy.model.combat;

public record CombatRound(CombatAction enemyAction, CombatAction playerAction, boolean qteTriggered) {
}

package com.swingy.model.combat;

public class DebuffState {
    private boolean armorBrokenCurrent;
    private boolean armorBrokenNext;

    public void beginRound() {
        armorBrokenCurrent = armorBrokenNext;
        armorBrokenNext = false;
    }

    public boolean isArmorBroken() {
        return armorBrokenCurrent;
    }

    public void applyArmorBrokenForNextRound() {
        armorBrokenNext = true;
    }
}

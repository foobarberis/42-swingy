package com.swingy.persistence;

import com.swingy.model.Hero;

public class CsvHeroSerializer {
    public String serialize(Hero h) {
        return String.join(",",
                h.getName(),
                h.getHeroClass().name(),
                String.valueOf(h.getLevel()),
                String.valueOf(h.getXp()),
                String.valueOf(h.getCurrentHp()),
                String.valueOf(h.getWeaponMod()),
                String.valueOf(h.getArmorMod()),
                String.valueOf(h.getHelmMod())
        );
    }
}

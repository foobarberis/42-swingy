package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;

public class CsvHeroParser {
    public Hero parse(String line) throws IllegalArgumentException {
        String[] p = line.split(",", -1);
        if (p.length != 8) throw new IllegalArgumentException("Malformed CSV");
        HeroClass cls;
        try {
            cls = HeroClass.valueOf(p[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad class");
        }
        int level = parseNonNegative(p[2]);
        int xp = parseNonNegative(p[3]);
        int hp = parseNonNegative(p[4]);
        int weapon = parseAtLeast(p[5], -1);
        int armor = parseAtLeast(p[6], -1);
        int helm = parseAtLeast(p[7], -1);
        if (level < 1) throw new IllegalArgumentException("Bad level");

        Hero hero = new Hero(p[0], cls, level, xp, hp, weapon, armor, helm);
        if (!hero.getName().matches("[A-Za-z0-9_-]{1,16}")) throw new IllegalArgumentException("Bad name");
        if (hero.getCurrentHp() > hero.effectiveMaxHp()) throw new IllegalArgumentException("HP too high");
        if (hero.getXp() >= hero.xpThreshold(hero.getLevel())) throw new IllegalArgumentException("XP out of range");
        return hero;
    }

    private int parseNonNegative(String s) {
        return parseAtLeast(s, 0);
    }

    private int parseAtLeast(String s, int min) {
        try {
            int value = Integer.parseInt(s);
            if (value < min) throw new IllegalArgumentException("Too small");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("NaN");
        }
    }
}

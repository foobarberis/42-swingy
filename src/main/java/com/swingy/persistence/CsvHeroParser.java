package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;

public class CsvHeroParser {
    public Hero parse(String line) throws CsvParseException {
        String[] p = line.split(",", -1);
        if (p.length != 8) throw new CsvParseException("Malformed CSV");
        HeroClass cls;
        try {
            cls = HeroClass.valueOf(p[1]);
        } catch (Exception e) {
            throw new CsvParseException("Bad class");
        }
        int level = parseNonNegative(p[2]);
        int xp = parseNonNegative(p[3]);
        int hp = parseNonNegative(p[4]);
        int weapon = parseAtLeast(p[5], -1);
        int armor = parseAtLeast(p[6], -1);
        int helm = parseAtLeast(p[7], -1);
        if (level < 1) throw new CsvParseException("Bad level");

        Hero hero = new Hero(p[0], cls, level, xp, hp, weapon, armor, helm);
        if (!hero.getName().matches("[A-Za-z0-9_-]{1,16}")) throw new CsvParseException("Bad name");
        if (hero.getCurrentHp() > hero.effectiveMaxHp()) throw new CsvParseException("HP too high");
        if (hero.getXp() >= hero.xpThreshold(hero.getLevel())) throw new CsvParseException("XP out of range");
        return hero;
    }

    private int parseNonNegative(String s) throws CsvParseException {
        return parseAtLeast(s, 0);
    }

    private int parseAtLeast(String s, int min) throws CsvParseException {
        final int value;
        try {
            value = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new CsvParseException("NaN");
        }
        if (value < min) throw new CsvParseException("Too small");
        return value;
    }
}

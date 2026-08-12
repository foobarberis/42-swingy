package com.swingy.view;

import com.swingy.model.Artifact;
import com.swingy.model.GameRules;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;

public final class ViewFormatter {
    private ViewFormatter() {
    }

    public static String heroStatus(Hero hero) {
        return "Name: "
            + hero.getName()
            + " | Class: "
            + className(hero.getHeroClass())
            + " | Level: "
            + hero.getLevel()
            + " | XP: "
            + hero.getXp()
            + "/"
            + GameRules.xpThreshold(hero.getLevel())
            + " | Attack: "
            + hero.getAttack()
            + " | Defense: "
            + hero.getDefense()
            + " | Hit Points: "
            + hero.getCurrentHp()
            + "/"
            + hero.getMaxHp();
    }

    public static String map(Room room, Position heroPosition) {
        StringBuilder text = new StringBuilder();
        for (int y = 0; y < room.getSize(); y++) {
            if (y > 0) {
                text.append('\n');
            }
            for (int x = 0; x < room.getSize(); x++) {
                Position position = new Position(x, y);
                if (position.equals(heroPosition)) {
                    text.append('@');
                } else if (room.enemyAt(position) != null) {
                    text.append('M');
                } else if (room.isBorder(position)) {
                    text.append('*');
                } else {
                    text.append('.');
                }
            }
        }
        return text.toString();
    }

    public static String artifactPrompt(Hero hero, Artifact artifact) {
        Artifact current = hero.getArtifact(artifact.slot());
        String replacement = current == null
            ? ""
            : " (replaces +" + current.modifier() + ")";
        return "You found "
            + slotName(artifact.slot())
            + " (+"
            + artifact.modifier()
            + " "
            + statName(artifact.slot())
            + ")"
            + replacement
            + ". Equip it [Y/n]?";
    }

    public static String slotName(Artifact.Slot slot) {
        return switch (slot) {
            case WEAPON -> "Weapon";
            case ARMOR -> "Armor";
            case HELM -> "Helm";
        };
    }

    public static String statName(Artifact.Slot slot) {
        return switch (slot) {
            case WEAPON -> "Attack";
            case ARMOR -> "Defense";
            case HELM -> "HP";
        };
    }

    private static String className(HeroClass heroClass) {
        return switch (heroClass) {
            case WARRIOR -> "Warrior";
            case ROGUE -> "Rogue";
            case MAGE -> "Mage";
        };
    }
}

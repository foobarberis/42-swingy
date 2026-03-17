package com.swingy.view;

import com.swingy.logic.CombatRound;
import com.swingy.model.GameRules;
import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;

import java.util.HashSet;
import java.util.Set;

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
        Set<Position> enemyPositions = new HashSet<>();
        for (Enemy enemy : room.getEnemies()) {
            enemyPositions.add(enemy.getPosition());
        }

        StringBuilder text = new StringBuilder();
        for (int y = 0; y < room.getSize(); y++) {
            if (y > 0) {
                text.append('\n');
            }
            for (int x = 0; x < room.getSize(); x++) {
                Position position = new Position(x, y);
                if (position.equals(heroPosition)) {
                    text.append('@');
                } else if (enemyPositions.contains(position)) {
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

    public static String artifactName(Hero hero, Artifact artifact) {
        return artifactBaseName(hero.getHeroClass(), artifact.slot());
    }

    public static String artifactPrompt(Hero hero, Artifact artifact) {
        return "You have found "
            + artifactName(hero, artifact)
            + " (+"
            + GameRules.artifactBonus(artifact)
            + " "
            + artifactStatName(artifact.slot())
            + "), do you want to equip it [Y/n]?";
    }

    public static String combatHeroHit(Enemy enemy, CombatRound round) {
        return "Round "
            + round.number()
            + ": you hit "
            + enemy.getName()
            + " for "
            + round.heroDamage()
            + " damage.";
    }

    public static String combatEnemyHit(Enemy enemy, CombatRound round) {
        return "Round "
            + round.number()
            + ": "
            + enemy.getName()
            + " hits you for "
            + round.enemyDamage()
            + " damage.";
    }

    public static String combatHealth(Hero hero, Enemy enemy, CombatRound round) {
        return "HP: "
            + round.heroHp()
            + "/"
            + hero.getMaxHp()
            + " vs "
            + enemy.getName()
            + " "
            + round.enemyHp();
    }

    public static String repositoryFailure(
        RepositoryAction action,
        String heroName,
        String detail
    ) {
        String operation = switch (action) {
            case LIST -> "list saved heroes";
            case LOAD -> "load hero '" + heroName + "'";
            case SAVE -> "save hero '" + heroName + "'";
            case DELETE -> "remove hero '" + heroName + "'";
        };
        return "Could not " + operation + ": " + nonEmptyDetail(detail);
    }

    public static String exitMessage(ExitReport report, boolean console) {
        StringBuilder message = new StringBuilder();
        switch (report.reason()) {
            case END_OF_INPUT -> message.append(
                console ? "EOF received (Ctrl-D). " : "Input ended. "
            );
            case INPUT_FAILURE -> message.append("Input failed: ")
                .append(nonEmptyDetail(report.inputFailure()))
                .append(' ');
            case VIEW_CLOSED, QUIT -> {
            }
        }

        switch (report.saveState()) {
            case SAVED -> message.append("Your progress has been saved. ");
            case FAILED -> message.append("Your progress could not be saved: ")
                .append(nonEmptyDetail(report.saveFailure()))
                .append(' ');
            case NOT_REQUIRED -> {
            }
        }
        message.append("Goodbye!");
        return message.toString();
    }

    private static String className(HeroClass heroClass) {
        return switch (heroClass) {
            case WARRIOR -> "Warrior";
            case ROGUE -> "Rogue";
            case MAGE -> "Mage";
        };
    }

    private static String artifactBaseName(HeroClass heroClass, Artifact.Slot slot) {
        return switch (slot) {
            case WEAPON -> switch (heroClass) {
                case WARRIOR -> "Sword";
                case ROGUE -> "Dagger";
                case MAGE -> "Staff";
            };
            case ARMOR -> switch (heroClass) {
                case WARRIOR -> "Plate Armor";
                case ROGUE -> "Leather Armor";
                case MAGE -> "Robe";
            };
            case HELM -> switch (heroClass) {
                case WARRIOR -> "Steel Helm";
                case ROGUE -> "Leather Helm";
                case MAGE -> "Wizard Hat";
            };
        };
    }

    private static String artifactStatName(Artifact.Slot slot) {
        return switch (slot) {
            case WEAPON -> "ATK";
            case ARMOR -> "DEF";
            case HELM -> "HP";
        };
    }

    private static String nonEmptyDetail(String detail) {
        return detail == null || detail.isBlank() ? "unknown error" : detail;
    }
}

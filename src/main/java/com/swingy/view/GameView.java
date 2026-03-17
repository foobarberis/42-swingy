package com.swingy.view;

import com.swingy.logic.CombatResult;
import com.swingy.logic.CombatRound;
import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;

import java.util.List;

public interface GameView {
    void println(String text);

    void renderStatus(String status);

    void renderMap(String mapText);

    void close();

    default void showWelcome() {
        println(
            "Welcome to Swingy! Available commands: list, "
                + "create warrior|rogue|mage <name>, load <name>, quit."
        );
    }

    default void renderMenu() {
        renderStatus("Main menu");
    }

    default void showNoHeroes() {
        println("No heroes available.");
    }

    default void showListUsage() {
        println("Usage: list");
    }

    default void showCreateUsage() {
        println("Usage: create warrior|rogue|mage <name>");
    }

    default void showLoadUsage() {
        println("Usage: load <name>");
    }

    default void showUnknownMenuCommand() {
        println(
            "Unknown command. Available commands: list, "
                + "create warrior|rogue|mage <name>, load <name>, quit."
        );
    }

    default void showUnknownHeroClass() {
        println("Unknown hero class. Choose warrior, rogue, or mage.");
    }

    default void showDuplicateName() {
        println("A character with that name already exists. Pick another name.");
    }

    default void showValidationErrors(List<String> errors) {
        for (String error : errors) {
            println("Validation failed: " + error);
        }
    }

    default void showRepositoryFailure(
        RepositoryAction action,
        String heroName,
        String detail
    ) {
        println(ViewFormatter.repositoryFailure(action, heroName, detail));
    }

    default void showProgressSaved() {
        println("Progress saved.");
    }

    default void renderHeroSummary(Hero hero) {
        println(ViewFormatter.heroStatus(hero));
    }

    default void renderHero(Hero hero) {
        renderStatus(ViewFormatter.heroStatus(hero));
    }

    default void renderRoom(Room room, Position heroPosition) {
        renderMap(ViewFormatter.map(room, heroPosition));
    }

    default void showUnknownMove() {
        println(
            "Unknown command. Available commands: north (n), south (s), "
                + "east (e), west (w), quit."
        );
    }

    default void showBlockedMove() {
        println("You cannot go there.");
    }

    default void showVictory() {
        println("Victory! You reached the border.");
    }

    default void showEncounter(Enemy enemy) {
        println("You encountered " + enemy.getName() + ". Fight or run [f/r]?");
    }

    default void showFightOrRunRequired() {
        println("Please answer with fight/f or run/r.");
    }

    default void showEscaped() {
        println("You escaped.");
    }

    default void showEscapeFailed() {
        println("You failed to escape.");
    }

    default void showCombatStarted(Enemy enemy) {
        println("Combat starts against " + enemy.getName() + ".");
    }

    default void renderCombat(Hero hero, Enemy enemy, CombatResult result) {
        for (CombatRound round : result.rounds()) {
            println(ViewFormatter.combatHeroHit(enemy, round));
            if (round.enemyDamage() > 0) {
                println(ViewFormatter.combatEnemyHit(enemy, round));
                println(ViewFormatter.combatHealth(hero, enemy, round));
            }
        }
    }

    default void showFightLost() {
        println("You lost the fight.");
    }

    default void showEnemyDefeated(Enemy enemy) {
        println("You defeated " + enemy.getName() + ".");
    }

    default void showExperienceGain(long amount, int levelsGained, Hero hero) {
        println("You gained " + amount + " XP.");
        if (levelsGained > 0) {
            println("Congratulations, you have reached level " + hero.getLevel() + "!");
        }
    }

    default void showExperienceFailure(String detail) {
        println("XP could not be applied safely: " + detail);
    }

    default void promptForArtifact(Hero hero, Artifact artifact) {
        println(ViewFormatter.artifactPrompt(hero, artifact));
    }

    default void showArtifactDiscarded(Hero hero, Artifact artifact) {
        println(ViewFormatter.artifactName(hero, artifact) + " has been discarded.");
    }

    default void showArtifactEquipped(Hero hero, Artifact artifact) {
        println(ViewFormatter.artifactName(hero, artifact) + " has been equipped.");
    }

    default void showYesNoRequired() {
        println("Please answer with y or n.");
    }

    default void showHeroDied() {
        println("You died.");
    }

    default void showHeroDeleted() {
        println("Your hero has been removed.");
    }

    default void showExit(ExitReport report) {
        println(ViewFormatter.exitMessage(report, false));
    }
}

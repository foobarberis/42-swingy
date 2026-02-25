package com.swingy.controller;

import com.swingy.model.Armor;
import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.Helm;
import com.swingy.model.Hero;
import com.swingy.model.Weapon;
import com.swingy.model.combat.CombatAction;
import com.swingy.model.combat.CombatOutcome;
import com.swingy.model.combat.CombatResolver;
import com.swingy.model.combat.QteChallenge;
import com.swingy.util.RandomProvider;
import com.swingy.view.RenderColor;
import com.swingy.view.View;

public class CombatController {
    private static final int COMBAT_TIMEOUT_MS = 3000;

    private final CombatResolver resolver;
    private final RandomProvider rng;

    public CombatController(CombatResolver resolver, RandomProvider rng) {
        this.resolver = resolver;
        this.rng = rng;
    }

    public boolean fight(View view, Hero hero, Enemy enemy) {
        view.setQuitLocked(true);
        try {
            while (hero.getCurrentHp() > 0 && enemy.getCurrentHp() > 0) {
                hero.debuffState().beginRound();
                enemy.debuffState().beginRound();

            CombatAction enemyAction = switch (rng.nextInt(3)) {
                case 0 -> CombatAction.ATTACK;
                case 1 -> CombatAction.DEFEND;
                default -> CombatAction.SUNDER;
            };

            RenderColor color = switch (enemyAction) {
                case ATTACK -> RenderColor.RED;
                case DEFEND -> RenderColor.BLUE;
                case SUNDER -> RenderColor.GREEN;
                default -> RenderColor.DEFAULT;
            };
            view.println(enemy.getName() + " uses " + enemyAction.name().toLowerCase() + ".", color);

            view.clearPendingInput();
            String in = view.readLine(COMBAT_TIMEOUT_MS);
            CombatAction playerAction;
            if (in == null) {
                playerAction = CombatAction.IDLE;
            } else {
                CombatAction parsed = CombatAction.fromInput(in.trim());
                if (parsed == null) {
                    view.println("Unknown command. Available commands: attack (a), defend (d), sunder (s)");
                    playerAction = CombatAction.IDLE;
                } else {
                    playerAction = parsed;
                }
            }

            Boolean qteSuccess = null;
            if (playerAction != CombatAction.IDLE && playerAction == enemyAction) {
                QteChallenge qte = QteChallenge.random(rng);
                view.println("QTE: " + qte.letters());
                view.clearPendingInput();
                String qteInput = view.readLine(COMBAT_TIMEOUT_MS);
                qteSuccess = qte.letters().equals(qteInput);
            }

            CombatOutcome out = resolver.resolve(hero, enemy, enemyAction, playerAction, qteSuccess);
            if (out.heroDamage > 0) hero.damage(out.heroDamage);
            if (out.enemyDamage > 0) enemy.damage(out.enemyDamage);
            if (out.heroHeal > 0) hero.heal(out.heroHeal);
            if (out.enemyHeal > 0) enemy.heal(out.enemyHeal);
            if (out.applyArmorBrokenToHero) hero.debuffState().applyArmorBrokenForNextRound();
            if (out.applyArmorBrokenToEnemy) enemy.debuffState().applyArmorBrokenForNextRound();

            view.println("You: " + hero.getCurrentHp() + " HP | " + enemy.getName() + ": " + enemy.getCurrentHp() + " HP");
        }

        if (hero.getCurrentHp() <= 0) {
            return false;
        }

        int xpToNext = hero.xpThreshold(hero.getLevel() + 1) - hero.xpThreshold(hero.getLevel());
        int xpGain = Math.round(xpToNext / 15f);
        hero.addXp(xpGain);

        boolean shouldDrop = enemy.isUnique() || rng.chance(0.35);
        if (shouldDrop) {
            Artifact artifact = randomArtifact(enemy.getLevel());
            promptEquip(view, hero, artifact);
        }

            return true;
        } finally {
            view.setQuitLocked(false);
        }
    }

    private Artifact randomArtifact(int mod) {
        return switch (rng.nextInt(3)) {
            case 0 -> new Weapon(mod);
            case 1 -> new Armor(mod);
            default -> new Helm(mod);
        };
    }

    private void promptEquip(View view, Hero hero, Artifact artifact) {
        int effectiveMod = hero.effectiveMod(artifact.mod());
        String stat;
        int bonus;
        if (artifact instanceof Weapon) {
            stat = "ATK";
            bonus = effectiveMod * 3;
        } else if (artifact instanceof Armor) {
            stat = "DEF";
            bonus = effectiveMod * 3;
        } else {
            stat = "HP";
            bonus = effectiveMod * 5;
        }
        String prompt = "You have found " + artifact.displayName(hero.getHeroClass()) + " (+" + bonus + " " + stat + "), do you want to equip it [Y/n]?";
        while (true) {
            view.println(prompt);
            String in = view.readLine();
            if (in == null) return;
            String a = in.trim().toLowerCase();
            if (a.isEmpty() || a.equals("y")) {
                if (artifact instanceof Weapon) hero.setWeaponMod(artifact.mod());
                if (artifact instanceof Armor) hero.setArmorMod(artifact.mod());
                if (artifact instanceof Helm) hero.setHelmMod(artifact.mod());
                hero.capHp();
                return;
            }
            if (a.equals("n")) {
                return;
            }
            view.println("Please answer with y or n.");
        }
    }
}

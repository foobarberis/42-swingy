package com.swingy.controller;

import com.swingy.model.Enemy;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.combat.CombatResolver;
import com.swingy.support.FakeView;
import com.swingy.util.RandomProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatControllerTest {

    @Test
    void timeoutResolvesAsIdleAndDoesNotTriggerQte() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        hero.damage(124); // leave 1 HP for immediate termination
        Enemy enemy = new Enemy("Goblin", false, 1, 1000, 15, 100);

        CombatController controller = new CombatController(new CombatResolver(),
                new SequenceRandomProvider(List.of(0), List.of(1.0)));

        FakeView view = new FakeView();

        boolean won = controller.fight(view, hero, enemy);

        assertFalse(won);
        assertTrue(view.outputs().stream().noneMatch(s -> s.startsWith("QTE:")));
    }

    @Test
    void qteTriggersOnlyWhenActionsMatchAndNotIdle() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Enemy enemy = new Enemy("Goblin", false, 1, 1, 0, 1);

        CombatController controller = new CombatController(new CombatResolver(),
                new SequenceRandomProvider(List.of(0, 0, 1, 2), List.of(1.0)));

        FakeView view = new FakeView().enqueue("attack", "abc");

        boolean won = controller.fight(view, hero, enemy);

        assertTrue(won);
        assertTrue(view.outputs().contains("QTE: abc"));
    }

    @Test
    void artifactEquipReplacesHelmAndCapsHp() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        hero.setHelmMod(10);
        hero.setCurrentHp(hero.effectiveMaxHp()); // 175 with +10 helm

        Enemy uniqueEnemy = new Enemy("Sigmund", true, 2, 1, 0, 1);

        CombatController controller = new CombatController(new CombatResolver(),
                new SequenceRandomProvider(List.of(1, 2), List.of()));

        FakeView view = new FakeView().enqueue("sunder", "y");

        boolean won = controller.fight(view, hero, uniqueEnemy);

        assertTrue(won);
        assertEquals(1, hero.getHelmMod());
        assertEquals(135, hero.getCurrentHp());
        assertTrue(view.outputs().contains("Steel Helm +10 has been discarded"));
    }

    private static class SequenceRandomProvider implements RandomProvider {
        private final Deque<Integer> ints;
        private final Deque<Double> doubles;

        SequenceRandomProvider(List<Integer> ints, List<Double> doubles) {
            this.ints = new ArrayDeque<>(ints);
            this.doubles = new ArrayDeque<>(doubles);
        }

        @Override
        public int nextInt(int bound) {
            if (ints.isEmpty()) {
                return 0;
            }
            int value = ints.removeFirst();
            if (value < 0) {
                return 0;
            }
            return value % bound;
        }

        @Override
        public double nextDouble() {
            if (doubles.isEmpty()) {
                return 1.0;
            }
            return doubles.removeFirst();
        }

        @Override
        public <T> void shuffle(List<T> list) {
        }
    }
}

package com.swingy.view;

import com.swingy.model.Artifact;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewFormatterTest {
    @Test
    void artifactPromptShowsTheActualBonusWithoutAMisleadingModifier() {
        Hero hero = Hero.createNew("Alice", HeroClass.WARRIOR);
        Artifact artifact = new Artifact(Artifact.Slot.WEAPON, 0);

        String prompt = ViewFormatter.artifactPrompt(hero, artifact);

        assertTrue(prompt.contains("+3 ATK"));
        assertFalse(prompt.contains("Sword +0"));
    }
}

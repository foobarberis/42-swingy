package com.swingy.view;

import com.swingy.model.Artifact;
import com.swingy.model.Enemy;
import com.swingy.model.EnemyType;
import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import com.swingy.model.world.Position;
import com.swingy.model.world.Room;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewFormatterTest {
    @Test
    void formatsClearStatsAndSimpleArtifactNames() {
        Hero hero = Hero.createNew("Ada", HeroClass.MAGE);
        assertTrue(ViewFormatter.heroStatus(hero).contains("Attack: 8 | Defense: 2"));
        String prompt = ViewFormatter.artifactPrompt(hero, new Artifact(Artifact.Slot.WEAPON, 1));
        assertTrue(prompt.contains("Weapon (+1 Attack)"));
    }

    @Test
    void mapReadsOccupancyFromRoom() {
        Room room = new Room(3);
        // A size-three room has no eligible enemy tile, so use a larger room for occupancy.
        room = new Room(5);
        room.addEnemy(new Position(1, 1), new Enemy(EnemyType.GOBLIN, 1));
        String[] rows = ViewFormatter.map(room, room.center()).split("\\n");
        assertEquals('M', rows[1].charAt(1));
        assertEquals('@', rows[2].charAt(2));
    }
}

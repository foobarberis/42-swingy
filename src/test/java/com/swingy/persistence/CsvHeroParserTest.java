package com.swingy.persistence;

import com.swingy.model.Hero;
import com.swingy.model.HeroClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvHeroParserTest {

    private final CsvHeroParser parser = new CsvHeroParser();
    private final CsvHeroSerializer serializer = new CsvHeroSerializer();

    @Test
    void serializeThenParseRoundTripKeepsFields() throws Exception {
        Hero original = new Hero("Alice", HeroClass.WARRIOR, 3, 100, 145, 4, 2, 1);

        String line = serializer.serialize(original);
        Hero parsed = parser.parse(line);

        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getHeroClass(), parsed.getHeroClass());
        assertEquals(original.getLevel(), parsed.getLevel());
        assertEquals(original.getXp(), parsed.getXp());
        assertEquals(original.getCurrentHp(), parsed.getCurrentHp());
        assertEquals(original.getWeaponMod(), parsed.getWeaponMod());
        assertEquals(original.getArmorMod(), parsed.getArmorMod());
        assertEquals(original.getHelmMod(), parsed.getHelmMod());
    }

    @Test
    void malformedColumnCountIsRejected() {
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,WARRIOR,1,0,125,-1,-1"));
    }

    @Test
    void badClassTokenIsRejected() {
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,PALADIN,1,0,125,-1,-1,-1"));
    }

    @Test
    void nanNumericFieldIsRejected() {
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,WARRIOR,one,0,125,-1,-1,-1"));
    }

    @Test
    void invalidRangesAreRejected() {
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,WARRIOR,0,0,125,-1,-1,-1"));
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,WARRIOR,1,-1,125,-1,-1,-1"));
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,WARRIOR,1,0,-1,-1,-1,-1"));
        assertThrows(CsvParseException.class, () -> parser.parse("Alice,WARRIOR,1,0,125,-2,-1,-1"));
    }
}

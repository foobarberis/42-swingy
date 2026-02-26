package com.swingy;

import com.swingy.persistence.CsvHeroParser;
import com.swingy.persistence.CsvHeroSerializer;
import com.swingy.persistence.CsvParseException;
import com.swingy.persistence.HeroCsvRepository;
import com.swingy.persistence.SaveFileCorruptedException;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvParsingTests {
    private static int passed = 0;

    private CsvParsingTests() {
    }

    public static void runAll() throws Exception {
        CsvHeroParser parser = new CsvHeroParser();

        System.out.println("[CSV] Starting parsing and repository corruption tests");

        runCase("Malformed CSV: too few columns", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-1,-1", "Malformed CSV"));
        runCase("Malformed CSV: too many columns", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-1,-1,-1,extra", "Malformed CSV"));

        runCase("Unknown class token", () ->
                expectParseError(parser, "Alice,PALADIN,1,0,125,-1,-1,-1", "Bad class"));

        runCase("Invalid name: spaces", () ->
                expectParseError(parser, "Bad Name,WARRIOR,1,0,125,-1,-1,-1", "Bad name"));
        runCase("Invalid name: too long", () ->
                expectParseError(parser, "NameIsWayTooLongForRule,WARRIOR,1,0,125,-1,-1,-1", "Bad name"));

        runCase("Non-integer level", () ->
                expectParseError(parser, "Alice,WARRIOR,one,0,125,-1,-1,-1", "NaN"));
        runCase("Non-integer xp", () ->
                expectParseError(parser, "Alice,WARRIOR,1,zero,125,-1,-1,-1", "NaN"));
        runCase("Non-integer currentHp", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,hp,-1,-1,-1", "NaN"));
        runCase("Non-integer weapon mod", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,weapon,-1,-1", "NaN"));
        runCase("Non-integer armor mod", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-1,armor,-1", "NaN"));
        runCase("Non-integer helm mod", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-1,-1,helm", "NaN"));

        runCase("Range: negative level", () ->
                expectParseError(parser, "Alice,WARRIOR,-1,0,125,-1,-1,-1", "Too small"));
        runCase("Range: level zero", () ->
                expectParseError(parser, "Alice,WARRIOR,0,0,125,-1,-1,-1", "Bad level"));
        runCase("Range: negative xp", () ->
                expectParseError(parser, "Alice,WARRIOR,1,-1,125,-1,-1,-1", "Too small"));
        runCase("Range: negative hp", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,-1,-1,-1,-1", "Too small"));
        runCase("Range: weapon mod < -1", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-2,-1,-1", "Too small"));
        runCase("Range: armor mod < -1", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-1,-2,-1", "Too small"));
        runCase("Range: helm mod < -1", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,125,-1,-1,-2", "Too small"));

        runCase("Consistency: HP above effective max", () ->
                expectParseError(parser, "Alice,WARRIOR,1,0,9999,-1,-1,-1", "HP too high"));
        runCase("Consistency: XP out of range", () ->
                expectParseError(parser, "Alice,WARRIOR,1,1000,125,-1,-1,-1", "XP out of range"));

        runCase("Repository: blank line with line number", () ->
                expectCorruptedFile(
                        List.of(
                                "Alice,WARRIOR,1,0,125,-1,-1,-1",
                                "",
                                "Bob,ROGUE,1,0,100,-1,-1,-1"
                        ),
                        2,
                        "Blank line"
                ));

        runCase("Repository: duplicate names with line number", () ->
                expectCorruptedFile(
                        List.of(
                                "Alice,WARRIOR,1,0,125,-1,-1,-1",
                                "Alice,ROGUE,1,0,100,-1,-1,-1"
                        ),
                        2,
                        "Duplicate names"
                ));

        runCase("Repository: malformed line propagated", () ->
                expectCorruptedFile(
                        List.of("Alice,WARRIOR,1,0,125,-1,-1"),
                        1,
                        "Malformed CSV"
                ));

        System.out.println("[CSV] Passed " + passed + " tests");
    }

    private static void runCase(String name, ThrowingRunnable test) throws Exception {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            System.out.println("[FAIL] " + name + " -> " + t.getMessage());
            throw t;
        }
    }

    private static void expectParseError(CsvHeroParser parser, String line, String expectedMessage) throws CsvParseException {
        boolean thrown = false;
        try {
            parser.parse(line);
        } catch (CsvParseException e) {
            thrown = true;
            assert e.getMessage() != null : "Missing parse error message";
            assert e.getMessage().contains(expectedMessage)
                    : "Expected parse error containing '" + expectedMessage + "' but got '" + e.getMessage() + "'";
        }
        assert thrown : "Expected CsvParseException for line: " + line;
    }

    private static void expectCorruptedFile(List<String> lines, int expectedLine, String expectedReason) throws Exception {
        Path file = Files.createTempFile("swingy-csv-tests-", ".csv");
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        HeroCsvRepository repository = new HeroCsvRepository(file, new CsvHeroParser(), new CsvHeroSerializer(), validator);

        try {
            Files.write(file, lines, StandardCharsets.UTF_8);

            boolean thrown = false;
            try {
                repository.list();
            } catch (SaveFileCorruptedException e) {
                thrown = true;
                assert e.getLineNumber() == expectedLine
                        : "Expected line " + expectedLine + " but got " + e.getLineNumber();
                assert e.getMessage() != null : "Missing repository corruption message";
                assert e.getMessage().contains(expectedReason)
                        : "Expected corruption reason containing '" + expectedReason + "' but got '" + e.getMessage() + "'";
            }
            assert thrown : "Expected SaveFileCorruptedException";
        } finally {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}

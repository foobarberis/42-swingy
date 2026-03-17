package com.swingy.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @TempDir
    Path tempDir;

    @Test
    void invalidArgumentPrintsUsageAndExitsWithCodeOne() throws Exception {
        Process process = process("invalid").start();

        assertTrue(process.waitFor(5, TimeUnit.SECONDS));
        String stderr = read(process, true);

        assertEquals(1, process.exitValue());
        assertTrue(stderr.contains("Usage: java -jar swingy.jar console|gui"));
    }

    @Test
    void heroSurvivesACompleteProcessRestart() throws Exception {
        Process first = process("console").directory(tempDir.toFile()).start();
        writeInput(first, "create warrior Alice\nquit\n");
        assertTrue(first.waitFor(10, TimeUnit.SECONDS));
        assertEquals(0, first.exitValue(), read(first, true));

        Process second = process("console").directory(tempDir.toFile()).start();
        writeInput(second, "list\nquit\n");
        assertTrue(second.waitFor(10, TimeUnit.SECONDS));
        String output = read(second, false);

        assertEquals(0, second.exitValue(), read(second, true));
        assertTrue(output.contains("Name: Alice | Class: Warrior"));
    }

    private ProcessBuilder process(String argument) {
        return new ProcessBuilder(
            javaBin(),
            "-cp",
            absoluteClassPath(),
            "com.swingy.app.Main",
            argument
        );
    }

    private void writeInput(Process process, String input) throws IOException {
        try (OutputStream stream = process.getOutputStream()) {
            stream.write(input.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String read(Process process, boolean errorStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                errorStream ? process.getErrorStream() : process.getInputStream(),
                StandardCharsets.UTF_8
            )
        )) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String absoluteClassPath() {
        return Arrays.stream(System.getProperty("java.class.path").split(java.io.File.pathSeparator))
            .map(entry -> Path.of(entry).toAbsolutePath().normalize().toString())
            .collect(Collectors.joining(java.io.File.pathSeparator));
    }

    private String javaBin() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }
}

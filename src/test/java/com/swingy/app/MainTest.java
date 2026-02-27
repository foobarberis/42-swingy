package com.swingy.app;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void invalidArgumentPrintsUsageAndExitsWithCodeOne() throws Exception {
        Process process = new ProcessBuilder(
                javaBin(),
                "-cp",
                System.getProperty("java.class.path"),
                "com.swingy.app.Main",
                "invalid"
        ).start();

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        assertTrue(finished);

        String stderr;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            stderr = reader.lines().reduce("", (a, b) -> a + b + "\n");
        }

        assertEquals(1, process.exitValue());
        assertTrue(stderr.contains("Usage: java -jar swingy.jar console|gui"));
    }

    private String javaBin() {
        return System.getProperty("java.home") + "/bin/java";
    }
}

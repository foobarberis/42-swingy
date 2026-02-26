package com.swingy;

public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        CsvParsingTests.runAll();
        System.out.println("All tests passed.");
    }
}

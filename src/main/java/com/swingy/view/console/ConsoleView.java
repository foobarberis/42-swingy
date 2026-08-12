package com.swingy.view.console;

import com.swingy.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ConsoleView implements View {
    private final BufferedReader reader;
    private final PrintStream output;

    public ConsoleView() {
        this(new InputStreamReader(System.in, StandardCharsets.UTF_8), System.out);
    }

    public ConsoleView(Reader reader, PrintStream output) {
        Objects.requireNonNull(reader, "Input reader is required.");
        this.reader = reader instanceof BufferedReader buffered
            ? buffered
            : new BufferedReader(reader);
        this.output = Objects.requireNonNull(output, "Output stream is required.");
    }

    @Override
    public String readInput() {
        output.print("> ");
        output.flush();
        try {
            return reader.readLine();
        } catch (IOException exception) {
            show("Input failed: " + detail(exception.getMessage()));
            return null;
        }
    }

    @Override
    public void show(String text) {
        output.println(text);
    }

    @Override
    public void close() {
        // System.in and System.out belong to the process, not this view.
    }

    private String detail(String message) {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

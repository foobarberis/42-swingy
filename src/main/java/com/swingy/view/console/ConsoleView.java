package com.swingy.view.console;

import com.swingy.view.ExitReport;
import com.swingy.view.View;
import com.swingy.view.ViewFormatter;
import com.swingy.view.ViewInput;

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
    private boolean closed;

    public ConsoleView() {
        this(
            new InputStreamReader(System.in, StandardCharsets.UTF_8),
            System.out
        );
    }

    public ConsoleView(Reader reader, PrintStream output) {
        Objects.requireNonNull(reader, "Input reader is required.");
        this.reader = reader instanceof BufferedReader buffered
            ? buffered
            : new BufferedReader(reader);
        this.output = Objects.requireNonNull(output, "Output stream is required.");
    }

    @Override
    public void println(String text) {
        output.println(text);
    }

    @Override
    public void renderStatus(String status) {
        output.println(status);
    }

    @Override
    public void renderMap(String mapText) {
        output.println(mapText);
    }

    @Override
    public void renderMenu() {
    }

    @Override
    public ViewInput readInput() {
        if (closed) {
            return ViewInput.viewClosed();
        }
        printPrompt();
        try {
            String line = reader.readLine();
            if (line == null) {
                closed = true;
                return ViewInput.endOfInput();
            }
            return ViewInput.line(line);
        } catch (IOException exception) {
            closed = true;
            return ViewInput.failure(exception);
        }
    }

    private void printPrompt() {
        output.print("> ");
        output.flush();
    }

    @Override
    public void showExit(ExitReport report) {
        println(ViewFormatter.exitMessage(report, true));
    }

    @Override
    public void close() {
        closed = true;
    }
}

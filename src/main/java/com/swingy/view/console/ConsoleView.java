package com.swingy.view.console;

import com.swingy.util.Ansi;
import com.swingy.view.RenderColor;
import com.swingy.view.View;

public class ConsoleView implements View {
    private final ConsoleInput input;

    public ConsoleView() {
        input = new ConsoleInput();
        input.start();
    }

    @Override
    public void println(String s) {
        System.out.println(s);
    }

    @Override
    public void println(String s, RenderColor color) {
        String prefix = switch (color) {
            case RED -> Ansi.RED;
            case BLUE -> Ansi.BLUE;
            case GREEN -> Ansi.GREEN;
            default -> "";
        };
        if (prefix.isEmpty()) {
            System.out.println(s);
        } else {
            System.out.println(prefix + s + Ansi.RESET);
        }
    }

    @Override
    public void renderStatus(String status) {
        System.out.println(status);
    }

    @Override
    public void renderMap(char[][] window) {
        int top = 0;
        int bottom = window.length - 1;

        while (top <= bottom && isBlankRow(window[top])) top++;
        while (bottom >= top && isBlankRow(window[bottom])) bottom--;

        System.out.println();
        if (top <= bottom) {
            for (int i = top; i <= bottom; i++) {
                System.out.println(new String(window[i]));
            }
        }
        System.out.println();
    }

    private boolean isBlankRow(char[] row) {
        for (char c : row) {
            if (c != ' ') return false;
        }
        return true;
    }

    @Override
    public String readLine() {
        printPrompt();
        return input.readBlocking();
    }

    @Override
    public String readLine(long timeoutMillis) {
        printPrompt();
        return input.readTimed(timeoutMillis);
    }

    private void printPrompt() {
        System.out.print("> ");
        System.out.flush();
    }

    @Override
    public void clearPendingInput() {
        input.clearPending();
    }

    @Override
    public void setQuitLocked(boolean locked) {
        input.setQuitLocked(locked);
    }

    @Override
    public boolean consumeQuitAttempt() {
        return input.consumeQuitAttempt();
    }

    @Override
    public boolean isClosed() {
        return input.isClosed();
    }

    @Override
    public void close() {
    }
}

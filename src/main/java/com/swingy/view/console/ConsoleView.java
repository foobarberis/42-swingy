package com.swingy.view.console;

import com.swingy.util.Ansi;
import com.swingy.view.RenderColor;
import com.swingy.view.View;

public class ConsoleView implements View {
    private final ConsoleInput input = new ConsoleInput();

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
    public void renderLook(char[][] window) {
        for (char[] row : window) {
            System.out.println(new String(row));
        }
    }

    @Override
    public String readLine() {
        return input.readBlocking();
    }

    @Override
    public String readLine(long timeoutMillis) {
        return input.readTimed(timeoutMillis);
    }

    @Override
    public void clearPendingInput() {
        input.clearPending();
    }

    @Override
    public boolean isClosed() {
        return input.isClosed();
    }

    @Override
    public void close() {
    }
}

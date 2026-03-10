package com.swingy.view.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.swingy.view.RenderColor;
import com.swingy.view.View;

public class ConsoleView implements View {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_GREEN = "\u001B[32m";

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
            case RED -> ANSI_RED;
            case BLUE -> ANSI_BLUE;
            case GREEN -> ANSI_GREEN;
            default -> "";
        };
        if (prefix.isEmpty()) {
            System.out.println(s);
        } else {
            System.out.println(prefix + s + ANSI_RESET);
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

        while (top <= bottom && isBlankRow(window[top])) {
            top++;
        }
        while (bottom >= top && isBlankRow(window[bottom])) {
            bottom--;
        }

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

    private static final class ConsoleInput {
        private static final String EOF = "__EOF__";
        private static final String QUIT_ATTEMPT = "__QUIT_ATTEMPT__";

        private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        private final Thread thread;
        private volatile boolean closed;
        private volatile boolean quitLocked;
        private volatile boolean quitAttempted;

        ConsoleInput() {
            thread = new Thread(this::runReader, "console-input-thread");
            thread.setDaemon(true);
        }

        void start() {
            thread.start();
        }

        private void runReader() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line != null) {
                        enqueue(line);
                        continue;
                    }
                    if (quitLocked) {
                        enqueue(QUIT_ATTEMPT);
                        continue;
                    }
                    closed = true;
                    System.out.println("\nEOF received (Ctrl-D). Your progress has been saved. Goodbye!");
                    enqueue(EOF);
                    return;
                }
            } catch (IOException e) {
                closed = true;
                enqueue(EOF);
            }
        }

        private void enqueue(String value) {
            try {
                queue.put(value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        String readBlocking() {
            while (true) {
                try {
                    String s = queue.take();
                    if (EOF.equals(s)) return null;
                    if (QUIT_ATTEMPT.equals(s)) {
                        quitAttempted = true;
                        return null;
                    }
                    return s;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        String readTimed(long timeoutMs) {
            try {
                String s = queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
                if (s == null) return null;
                if (EOF.equals(s)) return null;
                if (QUIT_ATTEMPT.equals(s)) {
                    quitAttempted = true;
                    return null;
                }
                return s;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        void clearPending() {
            queue.clear();
        }

        void setQuitLocked(boolean quitLocked) {
            this.quitLocked = quitLocked;
        }

        boolean consumeQuitAttempt() {
            boolean out = quitAttempted;
            quitAttempted = false;
            return out;
        }

        boolean isClosed() {
            return closed;
        }
    }
}

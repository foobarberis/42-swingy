package com.swingy.view.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleInput {
    private static final String EOF = "__EOF__";
    private static final String QUIT_ATTEMPT = "__QUIT_ATTEMPT__";

    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Thread thread;
    private volatile boolean closed;
    private volatile boolean quitLocked;
    private volatile boolean quitAttempted;

    public ConsoleInput() {
        thread = new Thread(this::runReader, "console-input-thread");
        thread.setDaemon(true);
    }

    public void start() {
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

    public String readBlocking() {
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

    public String readTimed(long timeoutMs) {
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

    public void clearPending() {
        queue.clear();
    }

    public void setQuitLocked(boolean quitLocked) {
        this.quitLocked = quitLocked;
    }

    public boolean consumeQuitAttempt() {
        boolean out = quitAttempted;
        quitAttempted = false;
        return out;
    }

    public boolean isClosed() {
        return closed;
    }
}

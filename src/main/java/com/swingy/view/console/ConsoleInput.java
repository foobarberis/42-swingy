package com.swingy.view.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleInput {
    private static final String EOF = "__EOF__";

    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Thread thread;
    private volatile boolean closed;

    public ConsoleInput() {
        thread = new Thread(this::runReader, "console-input-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void runReader() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                queue.offer(line);
            }
            closed = true;
            queue.offer(EOF);
        } catch (IOException e) {
            closed = true;
            queue.offer(EOF);
        }
    }

    public String readBlocking() {
        while (true) {
            try {
                String s = queue.take();
                if (EOF.equals(s)) return null;
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
            return s;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void clearPending() {
        queue.clear();
    }

    public boolean isClosed() {
        return closed;
    }
}

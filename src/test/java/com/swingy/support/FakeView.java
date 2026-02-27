package com.swingy.support;

import com.swingy.view.RenderColor;
import com.swingy.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class FakeView implements View {
    public static final String QUIT_ATTEMPT = "__QUIT_ATTEMPT__";

    private final Deque<String> inputs = new ArrayDeque<>();
    private final List<String> outputs = new ArrayList<>();
    private final List<String> coloredOutputs = new ArrayList<>();
    private boolean quitAttempted;
    private boolean closed;
    private boolean quitLocked;

    public FakeView enqueue(String... lines) {
        for (String line : lines) {
            inputs.addLast(line);
        }
        return this;
    }

    public List<String> outputs() {
        return outputs;
    }

    public List<String> coloredOutputs() {
        return coloredOutputs;
    }

    @Override
    public void println(String s) {
        outputs.add(s);
    }

    @Override
    public void println(String s, RenderColor color) {
        outputs.add(s);
        coloredOutputs.add(color.name() + ":" + s);
    }

    @Override
    public void renderStatus(String status) {
        outputs.add("STATUS:" + status);
    }

    @Override
    public void renderMap(char[][] window) {
        outputs.add("MAP:" + window.length + "x" + window[0].length);
    }

    @Override
    public String readLine() {
        return pollInput();
    }

    @Override
    public String readLine(long timeoutMillis) {
        return pollInput();
    }

    private String pollInput() {
        if (inputs.isEmpty()) {
            return null;
        }
        String next = inputs.removeFirst();
        if (QUIT_ATTEMPT.equals(next)) {
            quitAttempted = true;
            return null;
        }
        return next;
    }

    @Override
    public void clearPendingInput() {
    }

    @Override
    public void setQuitLocked(boolean locked) {
        quitLocked = locked;
    }

    @Override
    public boolean consumeQuitAttempt() {
        boolean out = quitAttempted;
        quitAttempted = false;
        return out;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        closed = true;
    }

    public boolean isQuitLocked() {
        return quitLocked;
    }
}

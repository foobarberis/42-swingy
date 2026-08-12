package com.swingy.support;

import com.swingy.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public final class FakeView implements View {
    private final Deque<String> inputs = new ArrayDeque<>();
    private final List<String> output = new ArrayList<>();
    private boolean closed;

    public FakeView(String... inputs) {
        this.inputs.addAll(Arrays.asList(inputs));
    }

    public void submit(String input) {
        inputs.addLast(input);
    }

    @Override
    public String readInput() {
        return inputs.pollFirst();
    }

    @Override
    public void show(String text) {
        output.add(text);
    }

    @Override
    public void close() {
        closed = true;
    }

    public List<String> output() {
        return List.copyOf(output);
    }

    public String renderedText() {
        return String.join("\n", output);
    }

    public boolean isClosed() {
        return closed;
    }
}

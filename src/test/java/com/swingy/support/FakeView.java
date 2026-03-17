package com.swingy.support;

import com.swingy.view.View;
import com.swingy.view.ViewInput;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class FakeView implements View {
    private final Deque<ViewInput> inputs = new ArrayDeque<>();
    private final List<String> outputs = new ArrayList<>();

    public FakeView enqueue(String... lines) {
        for (String line : lines) {
            inputs.addLast(ViewInput.line(line));
        }
        return this;
    }

    public FakeView enqueue(ViewInput input) {
        inputs.addLast(input);
        return this;
    }

    public List<String> outputs() {
        return List.copyOf(outputs);
    }

    @Override
    public void println(String text) {
        outputs.add(text);
    }

    @Override
    public void renderStatus(String status) {
        outputs.add("STATUS:" + status);
    }

    @Override
    public void renderMap(String mapText) {
        outputs.add("MAP:" + mapText.replace("\n", "|"));
    }

    @Override
    public ViewInput readInput() {
        return inputs.isEmpty() ? ViewInput.endOfInput() : inputs.removeFirst();
    }

    @Override
    public void close() {
        inputs.clear();
    }
}

package com.swingy.support;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class SequenceRandom extends Random {
    private static final long serialVersionUID = 1L;

    private final Deque<Integer> integers;
    private final Deque<Boolean> booleans;
    private final int defaultInteger;
    private final boolean defaultBoolean;

    public SequenceRandom(List<Integer> integers, List<Boolean> booleans) {
        this(integers, booleans, 1, false);
    }

    public SequenceRandom(
        List<Integer> integers,
        List<Boolean> booleans,
        int defaultInteger,
        boolean defaultBoolean
    ) {
        super(0L);
        this.integers = new ArrayDeque<>(integers);
        this.booleans = new ArrayDeque<>(booleans);
        this.defaultInteger = defaultInteger;
        this.defaultBoolean = defaultBoolean;
    }

    @Override
    public int nextInt(int bound) {
        int value = integers.isEmpty() ? defaultInteger : integers.removeFirst();
        return Math.floorMod(value, bound);
    }

    @Override
    public boolean nextBoolean() {
        return booleans.isEmpty() ? defaultBoolean : booleans.removeFirst();
    }
}

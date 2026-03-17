package com.swingy.support;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class SequenceRandom extends Random {
    private static final long serialVersionUID = 1L;

    private final Deque<Integer> ints;
    private final Deque<Double> doubles;
    private final int defaultInt;
    private final double defaultDouble;

    public SequenceRandom(List<Integer> ints, List<Double> doubles) {
        this(ints, doubles, 0, 1.0);
    }

    public SequenceRandom(
        List<Integer> ints,
        List<Double> doubles,
        int defaultInt,
        double defaultDouble
    ) {
        super(0L);
        this.ints = new ArrayDeque<>(ints);
        this.doubles = new ArrayDeque<>(doubles);
        this.defaultInt = defaultInt;
        this.defaultDouble = defaultDouble;
    }

    @Override
    public int nextInt(int bound) {
        int value = ints.isEmpty() ? defaultInt : ints.removeFirst();
        return Math.floorMod(value, bound);
    }

    @Override
    public double nextDouble() {
        return doubles.isEmpty() ? defaultDouble : doubles.removeFirst();
    }
}

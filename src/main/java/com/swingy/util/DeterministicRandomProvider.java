package com.swingy.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DeterministicRandomProvider implements RandomProvider {
    private final Random random;

    public DeterministicRandomProvider(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public <T> void shuffle(List<T> list) {
        Collections.shuffle(list, random);
    }
}

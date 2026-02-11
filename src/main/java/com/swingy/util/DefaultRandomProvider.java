package com.swingy.util;

import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

public class DefaultRandomProvider implements RandomProvider {
    private final RandomGenerator random;

    public DefaultRandomProvider() {
        this.random = RandomGenerator.getDefault();
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
        Collections.shuffle(list, new java.util.Random(random.nextLong()));
    }
}

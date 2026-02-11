package com.swingy.util;

import java.util.List;

public interface RandomProvider {
    int nextInt(int bound);
    double nextDouble();

    default boolean chance(double probability) {
        return nextDouble() < probability;
    }

    <T> void shuffle(List<T> list);
}

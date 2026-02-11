package com.swingy.model.combat;

import com.swingy.util.RandomProvider;

public record QteChallenge(String letters, long deadlineMillis) {
    public static QteChallenge random(RandomProvider rng) {
        char[] chars = new char[3];
        for (int i = 0; i < 3; i++) {
            chars[i] = (char) ('a' + rng.nextInt(26));
        }
        return new QteChallenge(new String(chars), 3000);
    }
}

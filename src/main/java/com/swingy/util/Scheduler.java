package com.swingy.util;

public interface Scheduler {
    Cancellable schedule(long delayMs, Runnable task);

    interface Cancellable {
        void cancel();
    }
}

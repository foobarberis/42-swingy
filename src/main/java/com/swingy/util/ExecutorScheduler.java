package com.swingy.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorScheduler implements Scheduler {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    @Override
    public Cancellable schedule(long delayMs, Runnable task) {
        var future = service.schedule(task, delayMs, TimeUnit.MILLISECONDS);
        return () -> future.cancel(true);
    }

    public void shutdown() {
        service.shutdownNow();
    }
}

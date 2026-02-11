package com.swingy.util;

import javax.swing.Timer;

public class SwingScheduler implements Scheduler {
    @Override
    public Cancellable schedule(long delayMs, Runnable task) {
        Timer timer = new Timer((int) delayMs, e -> task.run());
        timer.setRepeats(false);
        timer.start();
        return timer::stop;
    }
}

package com.swingy.app;

public class ShutdownManager {
    public void register(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }
}

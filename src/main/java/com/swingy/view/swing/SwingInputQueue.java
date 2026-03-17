package com.swingy.view.swing;

import com.swingy.view.ViewInput;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

final class SwingInputQueue {
    private static final Object CLOSED_EVENT = new Object();

    private final LinkedBlockingQueue<Object> events = new LinkedBlockingQueue<>();

    void offerLine(String line) {
        events.add(line);
    }

    void close() {
        events.clear();
        events.add(CLOSED_EVENT);
    }

    ViewInput take() {
        try {
            Object event = events.take();
            if (event == CLOSED_EVENT) {
                return ViewInput.viewClosed();
            }
            return ViewInput.line((String) event);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ViewInput.failure(new IOException("GUI input was interrupted.", exception));
        }
    }
}

package com.swingy.view.swing;

import com.swingy.view.ViewInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwingInputQueueTest {
    @Test
    void sentinelLikeUserTextRemainsARegularLine() {
        SwingInputQueue queue = new SwingInputQueue();
        queue.offerLine("__EOF__");

        ViewInput input = queue.take();

        assertEquals(ViewInput.Type.LINE, input.type());
        assertEquals("__EOF__", input.line());
    }

    @Test
    void privateCloseEventReleasesInputAsViewClosure() {
        SwingInputQueue queue = new SwingInputQueue();
        queue.close();

        assertEquals(ViewInput.Type.VIEW_CLOSED, queue.take().type());
    }
}

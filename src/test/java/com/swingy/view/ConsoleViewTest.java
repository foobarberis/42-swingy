package com.swingy.view;

import com.swingy.view.console.ConsoleView;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleViewTest {
    @Test
    void readsLinesShowsTextAndReturnsNullAtEof() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(
            new StringReader("north\n"),
            new PrintStream(bytes, true, StandardCharsets.UTF_8)
        );
        view.show("hello");
        assertEquals("north", view.readInput());
        assertNull(view.readInput());
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("hello\n> > "));
    }

    @Test
    void reportsInputFailureThenReturnsNull() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Reader failing = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("broken");
            }

            @Override
            public void close() {
            }
        };
        ConsoleView view = new ConsoleView(failing, new PrintStream(bytes));
        assertNull(view.readInput());
        assertTrue(bytes.toString().contains("Input failed: broken"));
    }
}

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleViewTest {
    @Test
    void eofIsDistinctFromInputFailure() {
        ConsoleView view = new ConsoleView(new StringReader(""), output());

        ViewInput input = view.readInput();

        assertEquals(ViewInput.Type.END_OF_INPUT, input.type());
    }

    @Test
    void ioFailureRetainsItsCauseAndMessage() {
        ConsoleView view = new ConsoleView(new FailingReader(), output());

        ViewInput input = view.readInput();

        assertEquals(ViewInput.Type.FAILURE, input.type());
        assertTrue(input.failure().contains("deliberate"));
    }

    @Test
    void userTextThatLooksLikeOldSentinelRemainsNormalInput() {
        ConsoleView view = new ConsoleView(new StringReader("__EOF__\n"), output());

        ViewInput input = view.readInput();

        assertEquals(ViewInput.Type.LINE, input.type());
        assertEquals("__EOF__", input.line());
    }

    private PrintStream output() {
        return new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
    }

    private static final class FailingReader extends Reader {
        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            throw new IOException("deliberate read failure");
        }

        @Override
        public void close() {
        }
    }
}

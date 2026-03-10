package com.swingy.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.swingy.view.RenderColor;
import com.swingy.view.View;

public class SwingView implements View {
    private static final String EOF = "__EOF__";
    private static final String QUIT_ATTEMPT = "__QUIT_ATTEMPT__";

    private static final String MAP_FONT_FAMILY = "Monospaced";

    private static final Color ACTION_ATTACK = Color.RED;
    private static final Color ACTION_DEFEND = Color.BLUE;
    private static final Color ACTION_SUNDER = Color.GREEN;

    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private volatile boolean closed;
    private volatile boolean quitLocked;
    private volatile boolean quitAttempted;

    private JFrame frame;
    private JTextPane logArea;
    private JTextField input;
    private JLabel status;

    public SwingView() {
        SwingUtilities.invokeLater(this::init);
        while (frame == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void init() {
        frame = new JFrame("Swingy");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);

        logArea = new JTextPane();
        logArea.setEditable(false);
        input = new JTextField();
        status = new JLabel();

        input.addActionListener(e -> {
            String txt = input.getText();
            input.setText("");
            enqueue(txt);
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (quitLocked) {
                    enqueue(QUIT_ATTEMPT);
                    return;
                }
                closeNow();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (!closed) {
                    closed = true;
                    enqueue(EOF);
                }
            }
        });

        JScrollPane logScrollPane = new JScrollPane(logArea);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(status, BorderLayout.CENTER);

        JPanel promptPanel = new JPanel(new BorderLayout());
        promptPanel.add(input, BorderLayout.CENTER);

        int oneLineHeight = input.getPreferredSize().height;
        statusPanel.setPreferredSize(new Dimension(0, oneLineHeight));
        statusPanel.setMinimumSize(new Dimension(0, oneLineHeight));
        promptPanel.setPreferredSize(new Dimension(0, oneLineHeight));
        promptPanel.setMinimumSize(new Dimension(0, oneLineHeight));

        frame.setLayout(new BorderLayout());
        frame.add(statusPanel, BorderLayout.NORTH);
        frame.add(logScrollPane, BorderLayout.CENTER);
        frame.add(promptPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    @Override
    public void println(String s) {
        SwingUtilities.invokeLater(() -> appendLine(s, Color.BLACK));
    }

    @Override
    public void println(String s, RenderColor color) {
        Color swingColor = switch (color) {
            case RED -> ACTION_ATTACK;
            case BLUE -> ACTION_DEFEND;
            case GREEN -> ACTION_SUNDER;
            default -> Color.BLACK;
        };
        SwingUtilities.invokeLater(() -> appendLine(s, swingColor));
    }

    private void appendLine(String text, Color color) {
        appendLine(text, color, null);
    }

    private void appendLine(String text, Color color, String fontFamily) {
        StyledDocument doc = logArea.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        if (fontFamily != null && !fontFamily.isBlank()) {
            StyleConstants.setFontFamily(attrs, fontFamily);
        }
        try {
            doc.insertString(doc.getLength(), text + "\n", attrs);
            logArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void appendMap(char[][] window) {
        if (window == null || window.length == 0) {
            appendLine("", Color.BLACK, MAP_FONT_FAMILY);
            return;
        }

        int top = 0;
        int bottom = window.length - 1;

        while (top <= bottom && isBlankRow(window[top])) top++;
        while (bottom >= top && isBlankRow(window[bottom])) bottom--;

        appendLine("", Color.BLACK, MAP_FONT_FAMILY);
        if (top <= bottom) {
            for (int i = top; i <= bottom; i++) {
                appendLine(new String(window[i]), Color.BLACK, MAP_FONT_FAMILY);
            }
        }
        appendLine("", Color.BLACK, MAP_FONT_FAMILY);
    }

    private boolean isBlankRow(char[] row) {
        if (row == null) return true;
        for (char c : row) {
            if (c != ' ') return false;
        }
        return true;
    }

    @Override
    public void renderStatus(String statusText) {
        SwingUtilities.invokeLater(() -> status.setText(statusText));
    }

    @Override
    public void renderMap(char[][] window) {
        SwingUtilities.invokeLater(() -> appendMap(window));
    }

    @Override
    public String readLine() {
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
        try {
            String value = queue.take();
            if (EOF.equals(value)) return null;
            if (QUIT_ATTEMPT.equals(value)) {
                quitAttempted = true;
                return null;
            }
            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public String readLine(long timeoutMillis) {
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
        try {
            String value = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            if (value == null) return null;
            if (EOF.equals(value)) return null;
            if (QUIT_ATTEMPT.equals(value)) {
                quitAttempted = true;
                return null;
            }
            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void enqueue(String value) {
        try {
            queue.put(value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void clearPendingInput() {
        queue.clear();
    }

    @Override
    public void setQuitLocked(boolean locked) {
        quitLocked = locked;
    }

    @Override
    public boolean consumeQuitAttempt() {
        boolean out = quitAttempted;
        quitAttempted = false;
        return out;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (frame != null) {
            SwingUtilities.invokeLater(this::closeNow);
        }
    }

    private void closeNow() {
        if (closed) return;
        closed = true;
        enqueue(EOF);
        frame.dispose();
    }
}

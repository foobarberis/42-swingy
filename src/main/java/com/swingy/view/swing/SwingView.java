package com.swingy.view.swing;

import com.swingy.view.RenderColor;
import com.swingy.view.View;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SwingView implements View {
    private static final String EOF = "__EOF__";

    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private volatile boolean closed;

    private JFrame frame;
    private JTextArea logArea;
    private JTextField input;
    private JLabel status;
    private SwingWorldPanel worldPanel;

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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 700);

        worldPanel = new SwingWorldPanel();
        logArea = new JTextArea();
        logArea.setEditable(false);
        input = new JTextField();
        status = new JLabel();

        input.addActionListener(e -> {
            String txt = input.getText();
            input.setText("");
            queue.offer(txt);
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                closed = true;
                queue.offer(EOF);
            }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(status, BorderLayout.NORTH);
        bottom.add(input, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout());
        frame.add(worldPanel, BorderLayout.CENTER);
        frame.add(new JScrollPane(logArea), BorderLayout.EAST);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    @Override
    public void println(String s) {
        SwingUtilities.invokeLater(() -> logArea.append(s + "\n"));
    }

    private void printPrompt() {
        SwingUtilities.invokeLater(() -> logArea.append("> "));
    }

    @Override
    public void println(String s, RenderColor color) {
        println(s);
    }

    @Override
    public void renderStatus(String statusText) {
        SwingUtilities.invokeLater(() -> status.setText(statusText));
    }

    @Override
    public void renderLook(char[][] window) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : window) {
            sb.append(new String(row)).append('\n');
        }
        String out = sb.toString();
        SwingUtilities.invokeLater(() -> worldPanel.setWorldText(out));
    }

    @Override
    public String readLine() {
        printPrompt();
        try {
            String value = queue.take();
            return EOF.equals(value) ? null : value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public String readLine(long timeoutMillis) {
        printPrompt();
        try {
            String value = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            if (value == null) return null;
            return EOF.equals(value) ? null : value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void clearPendingInput() {
        queue.clear();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }
}

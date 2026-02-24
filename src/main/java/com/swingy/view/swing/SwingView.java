package com.swingy.view.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.swingy.view.RenderColor;
import com.swingy.view.View;

public class SwingView implements View {
    private static final String EOF = "__EOF__";

    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private volatile boolean closed;
    private volatile boolean quitLocked;

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
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);

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
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (quitLocked) {
                    println("You cannot quit now.");
                    return;
                }
                closeNow();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (!closed) {
                    closed = true;
                    queue.offer(EOF);
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

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, worldPanel, logScrollPane);
        centerSplit.setResizeWeight(0.625);
        centerSplit.setContinuousLayout(true);
        centerSplit.setBorder(null);

        frame.setLayout(new BorderLayout());
        frame.add(statusPanel, BorderLayout.NORTH);
        frame.add(centerSplit, BorderLayout.CENTER);
        frame.add(promptPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        SwingUtilities.invokeLater(() -> centerSplit.setDividerLocation(0.625));
    }

    @Override
    public void println(String s) {
        SwingUtilities.invokeLater(() -> logArea.append(s + "\n"));
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
    public void renderMap(char[][] window) {
        SwingUtilities.invokeLater(() -> worldPanel.setViewport(window));
    }

    @Override
    public String readLine() {
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
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
        SwingUtilities.invokeLater(() -> input.requestFocusInWindow());
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
    public void setQuitLocked(boolean locked) {
        quitLocked = locked;
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
        queue.offer(EOF);
        frame.dispose();
    }
}

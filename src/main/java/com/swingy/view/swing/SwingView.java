package com.swingy.view.swing;

import com.swingy.view.View;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SwingView implements View {
    private static final Object CLOSED = new Object();

    private final BlockingQueue<Object> inputs = new LinkedBlockingQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    private JFrame frame;
    private JTextArea output;
    private JTextField input;

    public SwingView() {
        runAndWait(this::initialize);
    }

    private void initialize() {
        frame = new JFrame("Swingy");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationByPlatform(true);
        frame.setLayout(new BorderLayout());

        output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));

        input = new JTextField();
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
        input.addActionListener(event -> submitInput());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                signalClosed();
            }
        });
        frame.add(new JScrollPane(output), BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);
        frame.setVisible(true);
        input.requestFocusInWindow();
    }

    private void submitInput() {
        if (closed.get()) {
            return;
        }
        inputs.offer(input.getText());
        input.setText("");
    }

    @Override
    public void show(String text) {
        SwingUtilities.invokeLater(() -> {
            if (output != null) {
                output.append(text + "\n");
                output.setCaretPosition(output.getDocument().getLength());
            }
        });
    }

    @Override
    public String readInput() {
        if (closed.get()) {
            return null;
        }
        SwingUtilities.invokeLater(() -> {
            if (!closed.get()) {
                input.requestFocusInWindow();
            }
        });
        try {
            Object next = inputs.take();
            return next == CLOSED ? null : (String) next;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void close() {
        signalClosed();
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
            }
        });
    }

    private void signalClosed() {
        if (closed.compareAndSet(false, true)) {
            inputs.clear();
            inputs.offer(CLOSED);
        }
    }

    private void runAndWait(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(task);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Swing operation was interrupted.", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Swing operation failed.", exception.getCause());
        }
    }
}

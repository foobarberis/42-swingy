package com.swingy.view.swing;

import com.swingy.view.ExitReport;
import com.swingy.view.View;
import com.swingy.view.ViewFormatter;
import com.swingy.view.ViewInput;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public final class SwingView implements View {
    private final SwingInputQueue inputs = new SwingInputQueue();
    private final CountDownLatch initialized = new CountDownLatch(1);

    private volatile boolean closed;
    private volatile Throwable initializationFailure;

    private JFrame frame;
    private JTextArea logArea;
    private JTextField input;
    private JLabel status;

    public SwingView() {
        if (SwingUtilities.isEventDispatchThread()) {
            initializeSafely();
        } else {
            SwingUtilities.invokeLater(this::initializeSafely);
            try {
                initialized.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                closed = true;
                throw new IllegalStateException("GUI initialization was interrupted.", exception);
            }
        }
        if (initializationFailure != null) {
            closed = true;
            throw new IllegalStateException("Could not initialize the GUI.", initializationFailure);
        }
    }

    private void initializeSafely() {
        try {
            initializeComponents();
        } catch (RuntimeException | Error exception) {
            initializationFailure = exception;
            if (frame != null) {
                frame.dispose();
            }
        } finally {
            initialized.countDown();
        }
    }

    private void initializeComponents() {
        frame = new JFrame("Swingy");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        status = new JLabel();
        status.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));

        input = new JTextField();
        input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 24));
        input.addActionListener(event -> {
            String text = input.getText();
            input.setText("");
            inputs.offerLine(text);
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                requestCloseOnEventThread();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(status, BorderLayout.NORTH);
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);
        frame.add(input, BorderLayout.SOUTH);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    @Override
    public void println(String text) {
        SwingUtilities.invokeLater(() -> appendText(text + "\n"));
    }

    @Override
    public void renderStatus(String statusText) {
        SwingUtilities.invokeLater(() -> status.setText(statusText));
    }

    @Override
    public void renderMap(String mapText) {
        SwingUtilities.invokeLater(() -> appendText(mapText + "\n"));
    }

    @Override
    public ViewInput readInput() {
        SwingUtilities.invokeLater(() -> {
            if (!closed) {
                input.requestFocusInWindow();
            }
        });
        return inputs.take();
    }

    private void appendText(String text) {
        logArea.append(text);
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    @Override
    public void showExit(ExitReport report) {
        String message = ViewFormatter.exitMessage(report, false);
        if (report.saveState() == ExitReport.SaveState.FAILED
            || report.reason() == ExitReport.Reason.INPUT_FAILURE) {
            SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(
                    frame,
                    message,
                    "Swingy",
                    JOptionPane.ERROR_MESSAGE
                )
            );
        } else {
            println(message);
        }
    }

    @Override
    public void close() {
        if (SwingUtilities.isEventDispatchThread()) {
            disposeOnEventThread();
        } else {
            SwingUtilities.invokeLater(this::disposeOnEventThread);
        }
    }

    private void requestCloseOnEventThread() {
        if (closed) {
            return;
        }
        closed = true;
        input.setEnabled(false);
        inputs.close();
    }

    private void disposeOnEventThread() {
        requestCloseOnEventThread();
        if (frame != null) {
            frame.dispose();
        }
    }
}

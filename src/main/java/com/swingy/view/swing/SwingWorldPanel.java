package com.swingy.view.swing;

import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Graphics;

public class SwingWorldPanel extends JPanel {
    private String text = "";

    public SwingWorldPanel() {
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
    }

    public void setWorldText(String text) {
        this.text = text;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(getFont());
        int y = 20;
        for (String line : text.split("\\n")) {
            g.drawString(line, 10, y);
            y += 18;
        }
    }
}

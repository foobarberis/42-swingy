package com.swingy.view.swing;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class SwingWorldPanel extends JPanel {
    private static final int TILE_SIZE = 32;

    private char[][] viewport = new char[0][0];

    public void setViewport(char[][] viewport) {
        if (viewport == null || viewport.length == 0) {
            this.viewport = new char[0][0];
            setPreferredSize(new Dimension(0, 0));
            revalidate();
            repaint();
            return;
        }

        char[][] copy = new char[viewport.length][];
        for (int r = 0; r < viewport.length; r++) {
            copy[r] = viewport[r] == null ? new char[0] : viewport[r].clone();
        }
        this.viewport = copy;

        int rows = copy.length;
        int cols = maxColumns(copy);
        setPreferredSize(new Dimension(cols * TILE_SIZE, rows * TILE_SIZE));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (viewport.length == 0) {
            return;
        }

        int rows = viewport.length;
        int cols = maxColumns(viewport);
        if (cols == 0) {
            return;
        }

        int gridWidth = cols * TILE_SIZE;
        int gridHeight = rows * TILE_SIZE;
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        for (int r = 0; r < rows; r++) {
            int y = startY + (r * TILE_SIZE);
            for (int c = 0; c < cols; c++) {
                int x = startX + (c * TILE_SIZE);
                char tile = c < viewport[r].length ? viewport[r][c] : ' ';
                drawTile(g2, tile, x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private int maxColumns(char[][] map) {
        int cols = 0;
        for (char[] row : map) {
            cols = Math.max(cols, row.length);
        }
        return cols;
    }

    private void drawTile(Graphics2D g2, char tile, int x, int y, int w, int h) {
        switch (tile) {
            case '#':
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(x, y, w, h);
                break;
            case '.':
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(x, y, w, h);
                break;
            case '@':
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.BLUE);
                g2.fillOval(x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2));
                break;
            case 'M':
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.RED);
                g2.fillOval(x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2));
                break;
            case 'U':
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(x, y, w, h);
                g2.setColor(Color.MAGENTA);
                g2.fillOval(x + 1, y + 1, Math.max(1, w - 2), Math.max(1, h - 2));
                break;
            case '!':
                g2.setColor(Color.GREEN);
                g2.fillRect(x, y, w, h);
                break;
            case 'X':
                g2.setColor(Color.YELLOW);
                g2.fillRect(x, y, w, h);
                break;
            case ' ':
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y, w, h);
                break;
            default:
                g2.setColor(Color.GRAY);
                g2.fillRect(x, y, w, h);
                break;
        }
    }
}

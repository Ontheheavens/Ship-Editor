package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CoordinatePlanePanel extends JPanel {
    private static final int GRID_SIZE = 1;
    private static final Color GRID_COLOR = Color.LIGHT_GRAY;
    private static final Color AXIS_COLOR = Color.BLACK;
    private static final double ZOOM_FACTOR = 1.2;

    private double originX = 0;
    private double originY = 0;
    private double scale = 1.0;

    public CoordinatePlanePanel() {
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.WHITE);

        // Add mouse listeners for zooming and panning
        addMouseWheelListener(e -> {
            double oldScale = scale;
            if (e.getWheelRotation() < 0) {
                scale *= ZOOM_FACTOR;
            } else {
                scale /= ZOOM_FACTOR;
            }
            double factor = scale / oldScale;
            originX *= factor;
            originY *= factor;
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            private int x;
            private int y;

            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - x;
                int dy = e.getY() - y;
                originX -= dx / scale;
                originY -= dy / scale;
                x = e.getX();
                y = e.getY();
                repaint();
            }
        });
    }

    public void resetZoom() {
        originX = 0;
        originY = 0;
        scale = 1.0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        final BufferedImage image;

        @SuppressWarnings("SpellCheckingInspection") String imagePath = "C:\\Games\\msdr_drone_shield.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Image scaled = image.getScaledInstance((int) (image.getWidth() * scale),
                (int) (image.getHeight() * scale), 0);
        g.drawImage(scaled, (int) originX, (int) originY, null);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw the x-axis
        g2.setColor(AXIS_COLOR);
        g2.drawLine(0, (int) (originY * scale), width, (int) (originY * scale));

        // Draw the y-axis
        g2.drawLine((int) (originX * scale), 0, (int) (originX * scale), height);

        // Draw the grid
        if (scale > 5) {
            g2.setColor(GRID_COLOR);
            for (int x = (int) (originX - width / (2.0 * scale)); x <= originX + width / (scale); x += GRID_SIZE) {
                g2.drawLine((int) ((x - originX) * scale), 0, (int) ((x - originX) * scale), height);
            }
            for (int y = (int) (originY - height / (2.0 * scale)); y <= originY + height / (scale); y += GRID_SIZE) {
                g2.drawLine(0, (int) ((y - originY) * scale), width, (int) ((y - originY) * scale));
            }
        }
    }


}


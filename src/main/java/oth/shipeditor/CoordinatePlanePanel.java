package oth.shipeditor;

import lombok.Getter;
import oth.shipeditor.components.Axii;
import oth.shipeditor.components.ShipSprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CoordinatePlanePanel extends JPanel {
    private static final double ZOOM_FACTOR = 1.25;
    private static final double MIN_SCALE = 0.25;
    private static final double MAX_SCALE = 125;
    @Getter
    private static CoordinatePlanePanel instance;

    private double scale = 1.0;
    private ShipSprite sprite;
    private Axii axii;
    private boolean pressedRMB;

    private Point shipCenter;

    private List<Point> worldPoints = new ArrayList<>();

    private int offsetX, offsetY;
    private int panX, panY;
    private int mouseX, mouseY;

    public CoordinatePlanePanel() {
        instance = this;
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.GRAY);

        String imagePath = "C:\\Games\\aeroshuttle_base.png";
        sprite = new ShipSprite(imagePath, this);
        axii = new Axii();

        addMouseWheelListener(event -> {

            Point center = this.getCoordinateCenter();

            int eventX = center.x - event.getX();
            int eventY = center.y - event.getY();

            if (event.getWheelRotation() < 0 && scale < MAX_SCALE) {
                scale *= ZOOM_FACTOR;
                offsetX -= eventX / 4;
                offsetY -= eventY / 4;
            } else if (event.getWheelRotation() > 0 && scale > MIN_SCALE) {
                scale /= ZOOM_FACTOR;
                offsetX += eventX / 5;
                offsetY += eventY / 5;
            }
            sprite.setScale(scale);

            repaint();
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent me) {
                if(!SwingUtilities.isRightMouseButton(me)){
                    worldPoints.add(getWorldPoint(me));
                    return;
                }
                pressedRMB = true;
                super.mousePressed(me);
                panX = me.getX();
                panY = me.getY();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(SwingUtilities.isRightMouseButton(e)){
                    pressedRMB = false;
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if(SwingUtilities.isRightMouseButton(e)){
                    offsetX -= (e.getX() - panX);
                    offsetY -= (e.getY() - panY);
                    mouseX = e.getX();
                    mouseY = e.getY();
                    panX = e.getX();
                    panY = e.getY();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }
        });
    }

    public Point getShipCenter() {
        if (shipCenter == null) {
            shipCenter = getCoordinateCenter();
        }
        return shipCenter;
    }

    public Point getCoordinateCenter() {
        int positionX = (this.getWidth() / 2) - offsetX;
        int positionY = (this.getHeight() / 2) - offsetY;
        return new Point(positionX, positionY);
    }

    public void resetZoom() {
        scale = 1.0;
        sprite.setScale(scale);
        offsetX = 0;
        offsetY = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        sprite.paint(g);
        axii.paint(g);
        Graphics2D g2D = (Graphics2D) g;
        this.drawInfo(g2D);

        int imageWidth = sprite.getImageWidth();
        int imageHeight = sprite.getImageHeight();
        Point anchor = sprite.getDefaultImageAnchor();
        Point cursor = this.getMousePoint();


        g2D.drawRect((int) (cursor.x - (0.5 * scale)), anchor.y, (int) (1 * scale), imageHeight);
        g2D.drawRect(anchor.x, (int) (cursor.y - (0.5 * scale)), imageWidth, (int) (1 * scale));
    }

    public Point getWorldPoint(MouseEvent e) {
        Point center = getCoordinateCenter();
        Point mousePoint = e.getPoint();
        int worldX = (int) ((mousePoint.getX() + center.getX()) * scale);
        int worldY = (int) ((mousePoint.getY() + center.getY()) * scale);
        return new Point(worldX, worldY);
    }

    public Point getMousePoint() {
        Point anchor = sprite.getDefaultImageAnchor();
        // Calculate cursor position relative to anchor.
        double cursorRelX = (mouseX - anchor.x) / scale;
        double cursorRelY = (mouseY - anchor.y) / scale;
        // Align cursor position to nearest 0.5 scaled pixel.
        double alignedCursorRelX = Math.round(cursorRelX * 2) / 2.0;
        double alignedCursorRelY = Math.round(cursorRelY * 2) / 2.0;
        // Calculate cursor position in scaled pixels.
        int cursorX = (int) (anchor.x + alignedCursorRelX * scale);
        int cursorY = (int) (anchor.y + alignedCursorRelY * scale);
        return new Point(cursorX, cursorY);
    }

    private void drawInfo(Graphics2D g) {
        int height = this.getHeight() - 10;
        Point center = this.getCoordinateCenter();
        g.setFont(new Font("Orbitron", Font.BOLD, 16));
        g.drawString((int)(scale * 100) + "%", 10, height);
        double cursorX = Utility.round(mouseX / scale - center.x / scale, 1);
        double cursorY = Utility.round(mouseY / scale - center.y / scale, 1);
        g.drawString(Math.round(cursorX * 2) / 2.0 + "," + Math.round(cursorY * 2) / 2.0, 120, height);
        g.drawString(String.valueOf(pressedRMB), 240, height);

        for (Point point : worldPoints) {
            g.drawOval((int) ((point.x / scale - offsetX)), (int) ((point.y / scale - offsetY)), (int) (1 * scale), (int) (1 * scale));
        }
    }

}
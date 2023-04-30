package oth.shipeditor.components.entities;

import de.javagl.viewer.Painter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.04.2023
 */
public class WorldPoint {

    private final Point2D position;

    public WorldPoint(Point2D position) {
        this.position = position;
    }

    public Painter getPainter() {
        return new Painter() {
            final Point2D point = position;
            @Override
            public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
                Ellipse2D rect = new Ellipse2D.Double(point.getX() - 0.25, point.getY() - 0.25, 0.5, 0.5);
                Shape result = worldToScreen.createTransformedShape(rect);
                g.fill(result);

                Point2D dest = worldToScreen.transform(point, null);
                g.drawOval((int) dest.getX() - 6, (int) dest.getY() - 6, 12, 12);
            }
        };
    }

    @Override
    public String toString() {
        return "WorldPoint{" +
                "position=" + position +
                '}';
    }

}

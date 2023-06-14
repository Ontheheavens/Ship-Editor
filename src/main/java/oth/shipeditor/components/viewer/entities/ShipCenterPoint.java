package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.InstrumentMode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.05.2023
 */
public class ShipCenterPoint extends FeaturePoint{

    @Getter @Setter
    private float collisionRadius;

    private final Paint collisionCircleColor = new Color(0x33DCDC40, true);

    public ShipCenterPoint(Point2D position, float radius) {
        super(position);
        this.collisionRadius = radius;
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.CENTERS;
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            this.paintCollisionCircle(g, worldToScreen);
            Point2D center = worldToScreen.transform(getPosition(), null);
            int x = (int) center.getX(), y = (int) center.getY(), l = 15;
            g.drawLine(x - l, y - l, x + l, y + l);
            g.drawLine(x - l, y + l, x + l, y - l);
        };
    }

    private void paintCollisionCircle(Graphics2D g, AffineTransform worldToScreen) {
        float radius = this.getCollisionRadius();
        Point2D position = getPosition();
        Shape dot = new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
        Shape transformed = worldToScreen.createTransformedShape(dot);

        Paint old = g.getPaint();
        g.setPaint(collisionCircleColor);
        g.fill(transformed);
        g.setPaint(old);
    }

    @Override
    public String toString() {
        return "ShipCenter";
    }

}

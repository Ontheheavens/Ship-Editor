package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import oth.shipeditor.components.viewer.InstrumentMode;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 30.05.2023
 */
public class ShipCenterPoint extends FeaturePoint{

    public ShipCenterPoint(Point2D position) {
        super(position);
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.CENTERS;
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            Point2D center = worldToScreen.transform(getPosition(), null);
            int x = (int) center.getX(), y = (int) center.getY(), l = 15;
            g.drawLine(x - l, y - l, x + l, y + l);
            g.drawLine(x - l, y + l, x + l, y - l);
        };
    }
    @Override
    public String toString() {
        return "ShipCenter";
    }

}

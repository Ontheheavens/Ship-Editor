package oth.shipeditor.components.entities;

import de.javagl.viewer.Painter;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class BoundPoint extends FeaturePoint{

    public BoundPoint(Point2D position) {
        super(position);
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            Point2D center = worldToScreen.transform(getPosition(), null);
            int x = (int) center.getX(), y = (int) center.getY(), l = 5;
            g.drawLine(x-l, y-l, x+l, y+l);
            g.drawLine(x-l, y+l, x+l, y-l);
        };
    }

    @Override
    public String toString() {
        String sel = "";
        if (super.isSelected()) {
            sel = "[S]";
        }
        return sel + "BoundP{" + getPosition().getX() + "," + getPosition().getY() + '}';
    }

}

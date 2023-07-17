package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.utility.Utility;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class BoundPoint extends FeaturePoint{



    public BoundPoint(Point2D position) {
        this(position, null);
    }

    public BoundPoint(Point2D pointPosition, LayerPainter layer) {
        super(pointPosition, layer);
    }

    @Override
    public InstrumentMode getAssociatedMode() {
        return InstrumentMode.BOUNDS;
    }

    @Override
    protected Color createBaseColor() {
        return super.createHoverColor();
    }

    @Override
    protected Color createHoverColor() {
        return super.createBaseColor();
    }

    @Override
    protected Painter createSecondaryPainter() {
        return (g, worldToScreen, w, h) -> {
            Point2D center = worldToScreen.transform(getPosition(), null);

            Polygon hexagon = Utility.createHexagon(center, 5);

            Paint old = g.getPaint();
            g.setPaint(createBaseColor());
            g.fillPolygon(hexagon);

            g.setPaint(old);

            this.paintCoordsLabel(g, worldToScreen, w, h);
        };
    }

    @Override
    public String getNameForLabel() {
        return "Bound";
    }

    @Override
    public String toString() {
        Class<? extends BoundPoint> identity = this.getClass();
        Point2D location = this.getCoordinatesForDisplay();
        return identity.getSimpleName() + " (" + location.getX() + "," + location.getY() + ")";
    }

}

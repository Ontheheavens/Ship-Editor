package oth.shipeditor.components.viewer.entities;

import oth.shipeditor.components.instrument.ship.ShipInstrument;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 01.05.2023
 */
public class BoundPoint extends BaseWorldPoint{

    public BoundPoint(Point2D pointPosition, ShipPainter layer) {
        super(pointPosition, layer);
    }

    @Override
    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.BOUNDS;
    }

    public static Shape getShapeForPoint(AffineTransform worldToScreen, Point2D position, double sizeMult) {
        Shape hexagon = ShapeUtilities.createHexagon(position, 0.10f * sizeMult);

        return ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, hexagon, 12 * sizeMult);
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        Point2D position = getPosition();

        Shape hexagon = BoundPoint.getShapeForPoint(worldToScreen, position, getPaintSizeMultiplier());

        boolean cursorInBounds = StaticController.checkIsHovered(hexagon);
        this.setCursorInBounds(cursorInBounds);

        DrawUtilities.outlineShape(g, hexagon, Color.BLACK, 2);
        DrawUtilities.fillShape(g, hexagon, getCurrentColor());

        this.paintCoordsLabel(g, worldToScreen);
    }

    @Override
    public String getNameForLabel() {
        return "Bound";
    }

    @Override
    public String toString() {
        Class<? extends BoundPoint> identity = this.getClass();
        return identity.getSimpleName() + " " + getPositionText();
    }

}

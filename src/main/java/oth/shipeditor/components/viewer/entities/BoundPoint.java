package oth.shipeditor.components.viewer.entities;

import de.javagl.viewer.Painter;
import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.InstrumentMode;
import oth.shipeditor.components.viewer.layers.LayerPainter;
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

    @Getter @Setter
    private double paintSizeMultiplier = 1;

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

    public static Shape getShapeForPoint(AffineTransform worldToScreen, Point2D position, double sizeMult) {
        Shape hexagon = ShapeUtilities.createHexagon(position, 0.10f * sizeMult);

        return ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, hexagon, 12 * sizeMult);
    }

    @Override
    public Painter createPointPainter() {
        return (g, worldToScreen, w, h) -> {
            Point2D position = getPosition();

            Shape hexagon = BoundPoint.getShapeForPoint(worldToScreen, position, paintSizeMultiplier);

            boolean cursorInBounds = StaticController.checkIsHovered(hexagon);
            this.setCursorInBounds(cursorInBounds);

            DrawUtilities.outlineShape(g, hexagon, Color.BLACK, 2);
            DrawUtilities.fillShape(g, hexagon, getCurrentColor());

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

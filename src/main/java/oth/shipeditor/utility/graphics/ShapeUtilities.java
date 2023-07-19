package oth.shipeditor.utility.graphics;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Ontheheavens
 * @since 19.07.2023
 */
@SuppressWarnings("WeakerAccess")
public final class ShapeUtilities {

    private ShapeUtilities() {
    }

    public static Polygon createHexagon(Point2D point, int radius) {
        int x = (int) point.getX(), y = (int) point.getY();

        int[] xPoints = new int[6];
        int[] yPoints = new int[6];

        // Calculate the coordinates of the six points of the hexagon.
        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            xPoints[i] = x + (int) (radius * Math.cos(angle));
            yPoints[i] = y + (int) (radius * Math.sin(angle));
        }
        return new Polygon(xPoints, yPoints, 6);
    }

    @SuppressWarnings("unused")
    public static AffineTransform getScaledTransform(Point2D center, AffineTransform worldToScreen,
                                                     double scale) {
        AffineTransform scaleTX = new AffineTransform();
        scaleTX.translate(center.getX(), center.getY());
        scaleTX.scale(scale, scale);
        scaleTX.translate(-center.getX(), -center.getY());
        AffineTransform delegateWTS = new AffineTransform();
        delegateWTS.setTransform(worldToScreen);
        delegateWTS.concatenate(scaleTX);
        return delegateWTS;
    }

    public static RectangularShape createCircle(Point2D position, float radius) {
        return new Ellipse2D.Double(position.getX() - radius, position.getY() - radius,
                2 * radius, 2 * radius);
    }

    public static Shape createPerpendicularThickCross(Point2D positionWorld, double worldSize, double worldThickness) {
        double x = positionWorld.getX(), y = positionWorld.getY();

        double horizontalX = x - worldSize / 2;
        double horizontalY = y - worldThickness / 2;

        double verticalX = x - worldThickness / 2;
        double verticalY = y - worldSize / 2;

        Shape horizontal = new Rectangle2D.Double(horizontalX, horizontalY, worldSize, worldThickness);
        Shape vertical = new Rectangle2D.Double(verticalX, verticalY, worldThickness, worldSize);

        return ShapeUtilities.combineShapes(horizontal, vertical);
    }

    public static Shape createPerpendicularCross(Point2D positionWorld, double crossWorldSize) {
        double x = positionWorld.getX(), y = positionWorld.getY();

        Shape crossLineX = new Line2D.Double(x - crossWorldSize, y, x + crossWorldSize, y);
        Shape crossLineY = new Line2D.Double(x, y - crossWorldSize, x, y + crossWorldSize);

        return ShapeUtilities.combineShapes(crossLineX, crossLineY);
    }

    public static Shape createDiagonalCross(Point2D positionWorld, double crossWorldSize) {
        double x = positionWorld.getX(), y = positionWorld.getY();

        Shape crossLineX = new Line2D.Double(x - crossWorldSize, y - crossWorldSize,
                x + crossWorldSize, y + crossWorldSize);
        Shape crossLineY = new Line2D.Double(x + crossWorldSize, y - crossWorldSize,
                x - crossWorldSize, y + crossWorldSize);

        return ShapeUtilities.combineShapes(crossLineX, crossLineY);
    }

    @SuppressWarnings("WeakerAccess")
    public static Path2D combineShapes(Shape first, Shape second) {
        Path2D combinedPath = new Path2D.Double();
        combinedPath.append(first, false);
        combinedPath.append(second, false);
        return combinedPath;
    }

    public static Shape ensureDynamicScaleShape(AffineTransform worldToScreen,
                                                Point2D positionWorld, Shape worldShape, double minScreenSize) {
        Shape transformed = worldToScreen.createTransformedShape(worldShape);
        Rectangle2D bounds = transformed.getBounds2D();
        double currentScreenSize = Math.max(bounds.getWidth(), bounds.getHeight());

        double scaleFactor = minScreenSize / currentScreenSize;

        if (scaleFactor > 1) {
            AffineTransform scaleTX = ShapeUtilities.getScaledTransform(positionWorld, worldToScreen, scaleFactor);
            transformed = scaleTX.createTransformedShape(worldShape);
        }
        return transformed;
    }

}

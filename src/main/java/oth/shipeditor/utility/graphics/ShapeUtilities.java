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

    public static Path2D createHexagon(Point2D point, double radius) {
        double x = point.getX();
        double y = point.getY();

        Path2D path = new Path2D.Double();

        double angle = 0;
        double xPos = x + radius * Math.cos(angle);
        double yPos = y + radius * Math.sin(angle);
        path.moveTo(xPos, yPos);

        for (int i = 1; i < 6; i++) {
            angle = 2 * Math.PI / 6 * i;
            xPos = x + radius * Math.cos(angle);
            yPos = y + radius * Math.sin(angle);
            path.lineTo(xPos, yPos);
        }
        path.closePath();

        return path;
    }

    /**
     * Generates a scaled AffineTransform based on a given center point, world-to-screen transform,
     * and scaling factor.
     * @param center Point2D representing the center point around which the scaling should occur.
     * @param worldToScreen original AffineTransform representing the transformation from world coordinates to screen coordinates.
     * @param scale scaling factor applied to the transformation. A value greater than 1 scales up, and a value less than 1 scales down.
     * @return new AffineTransform that represents the scaled version of the original world-to-screen transformation.
     */
    public static AffineTransform getScaledWtS(Point2D center, AffineTransform worldToScreen,
                                               double scale) {
        return ShapeUtilities.getScaledWtS(center, worldToScreen, scale, scale);
    }

    public static AffineTransform getScaledWtS(Point2D center, AffineTransform worldToScreen,
                                               double scaleX, double scaleY) {
        AffineTransform scaleTX = ShapeUtilities.getScaled(center, scaleX, scaleY);
        AffineTransform delegateWTS = new AffineTransform();
        delegateWTS.setTransform(worldToScreen);
        delegateWTS.concatenate(scaleTX);
        return delegateWTS;
    }

    public static AffineTransform getScaled(Point2D center, double scaleX, double scaleY) {
        AffineTransform scaleTX = new AffineTransform();
        scaleTX.translate(center.getX(), center.getY());
        scaleTX.scale(scaleX, scaleY);
        scaleTX.translate(-center.getX(), -center.getY());
        return scaleTX;
    }

    public static Shape translateShape(Shape shape, double deltaX, double deltaY) {
        AffineTransform translation = AffineTransform.getTranslateInstance(deltaX, deltaY);
        return translation.createTransformedShape(shape);
    }

    public static Shape rotateShape(Shape shape, Point2D anchor, double degrees) {
        AffineTransform rotation = AffineTransform.getRotateInstance(degrees, anchor.getX(), anchor.getY());
        return rotation.createTransformedShape(shape);
    }

    @SuppressWarnings("unused")
    public static AffineTransform getScreenToWorldRotation(AffineTransform worldToScreen, Point2D positionWorld) {
        Point2D positionScreen = worldToScreen.transform(positionWorld, null);

        double screenX = positionScreen.getX(), screenY = positionScreen.getY();

        // Extracting the rotation component from the worldToScreen transform.
        double[] matrix = new double[6];
        worldToScreen.getMatrix(matrix);
        double scaleX = Math.sqrt(matrix[0] * matrix[0] + matrix[1] * matrix[1]);
        double rotationAngle = -Math.atan2(matrix[1] / scaleX, matrix[0] / scaleX);

        AffineTransform rotationTransform = new AffineTransform();
        // New AffineTransform by default is focused  on the 0,0 in screen coordinates.
        // We have to center it on our point, do the rotation, then translate back.
        rotationTransform.translate(screenX, screenY);
        rotationTransform.rotate(-rotationAngle);
        rotationTransform.translate(-screenX, -screenY);

        return rotationTransform;
    }

    public static Ellipse2D createCircle(Point2D position, float radius) {
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

    public static Point2D getPointInDirection(Point2D startPoint, double angleDegrees, double length) {
        double angleRadians = Math.toRadians(angleDegrees);

        double deltaX = length * Math.cos(angleRadians);
        double deltaY = length * Math.sin(angleRadians);

        double endX = startPoint.getX() + deltaX;
        double endY = startPoint.getY() + deltaY;
        return new Point2D.Double(endX, endY);
    }

    public static Path2D combineShapes(Shape first, Shape second) {
        Path2D combinedPath = new Path2D.Double();
        combinedPath.append(first, false);
        combinedPath.append(second, false);
        return combinedPath;
    }

    /**
     * Ensures dynamic scaling of a Shape to maintain a minimum screen size while preserving its position.
     * @param worldToScreen AffineTransform representing the transformation from world coordinates to screen coordinates.
     * @param positionWorld Point2D representing the position of the Shape in world coordinates.
     * @param worldShape original Shape in world coordinates that needs to be scaled.
     * @param minScreenSize minimum size, in screen coordinates, that the transformed Shape should have.
     * @return scaled and transformed version of the original Shape that meets the minimum screen size requirement.
     */
    public static Shape ensureDynamicScaleShape(AffineTransform worldToScreen,
                                                Point2D positionWorld, Shape worldShape, double minScreenSize) {
        Shape transformed = worldToScreen.createTransformedShape(worldShape);
        Rectangle2D bounds = transformed.getBounds2D();
        double currentScreenSize = Math.max(bounds.getWidth(), bounds.getHeight());

        double scaleFactor = minScreenSize / currentScreenSize;

        return ShapeUtilities.scaleShapeConditionally(worldToScreen, positionWorld,
                worldShape, transformed, scaleFactor);
    }

    public static Shape ensureSpecialScaleShape(AffineTransform worldToScreen, Point2D positionWorld,
                                                Shape worldShape, double minScreenSize, double currentLength) {
        Shape transformed = worldToScreen.createTransformedShape(worldShape);
        double scaleFactor = minScreenSize / currentLength;

        return ShapeUtilities.scaleShapeConditionally(worldToScreen, positionWorld,
                worldShape, transformed, scaleFactor);
    }

    public static Shape scaleShapeConditionally(AffineTransform worldToScreen, Point2D positionWorld,
                                                Shape worldShape, Shape transformed, double scaleFactor) {
        Shape shape = transformed;
        if (scaleFactor > 1) {
            AffineTransform scaleTX = ShapeUtilities.getScaledWtS(positionWorld, worldToScreen, scaleFactor);
            shape = scaleTX.createTransformedShape(worldShape);
        }
        return shape;
    }

    /**
     * Calculates the coordinates of a specified corner in a Rectangle2D.
     * <p>
     * This method takes a Rectangle2D and a RectangleCorner enum as input and returns
     * a Point2D representing the coordinates of the specified corner in the Rectangle2D.
     * <p>
     * Note: The Rectangle2D is assumed to have positive dimensions (non-negative width and height).
     *
     * @param rectangle The Rectangle2D from which to calculate the corner coordinates.
     * @param corner The specific corner of the rectangle for which to calculate the coordinates.
     * @return A Point2D representing the coordinates of the specified corner of the Rectangle2D.
     */
    public static Point2D calculateCornerCoordinates(Rectangle2D rectangle, RectangleCorner corner) {
        double x = 0;
        double y = 0;
        switch (corner) {
            case TOP_LEFT -> {
                x = rectangle.getX();
                y = rectangle.getY();
            }
            case TOP_RIGHT -> {
                x = rectangle.getX() - rectangle.getWidth();
                y = rectangle.getY();
            }
            case BOTTOM_LEFT -> {
                x = rectangle.getX();
                y = rectangle.getY() - rectangle.getHeight();
            }
            case BOTTOM_RIGHT -> {
                x = rectangle.getX() - rectangle.getWidth();
                y = rectangle.getY() - rectangle.getHeight();
            }
        }
        return new Point2D.Double(x, y);
    }

    public static Shape createCircumscribingTriangle(Shape circle) {
        Rectangle2D circleBounds = circle.getBounds2D();
        double centerX = circleBounds.getCenterX();
        double centerY = circleBounds.getCenterY();
        double radius = Math.max(circleBounds.getWidth(), circleBounds.getHeight()) * 1.25d;

        Path2D triangle = new Path2D.Double();

        double[] angles = { 30, -90, 150 };
        for (double angle : angles) {
            double radians = Math.toRadians(angle);
            double x = centerX + radius * Math.cos(radians);
            double y = centerY + radius * Math.sin(radians);

            if (Math.abs(angle - 30) < 5.96e-08) {
                triangle.moveTo(x, y);
            } else {
                triangle.lineTo(x, y);
            }
        }

        triangle.closePath();

        return triangle;
    }

}

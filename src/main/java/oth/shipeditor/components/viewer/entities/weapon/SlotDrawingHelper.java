package oth.shipeditor.components.viewer.entities.weapon;

import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.utility.StaticController;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotDrawingHelper {

    private final WeaponSlotPoint parentPoint;

    SlotDrawingHelper(WeaponSlotPoint point) {
        this.parentPoint = point;
    }

    void paintSlotVisuals(Graphics2D g, AffineTransform worldToScreen) {
        Point2D position = parentPoint.getPosition();

        double circleRadius = 0.10f;

        Ellipse2D circle = ShapeUtilities.createCircle(position, (float) circleRadius);

        this.drawMountShape(g, worldToScreen, circle, circleRadius);

        this.drawArc(g, worldToScreen, circle, circleRadius);
        this.drawAnglePointer(g, worldToScreen, circle, circleRadius);
    }


    private void drawMountShape(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        Point2D position = parentPoint.getPosition();
        Shape mountShape = null;
        WeaponMount slotMount = parentPoint.getWeaponMount();
        double enlargedRadius = circleRadius * 1.65f;
        switch (slotMount) {
            case TURRET -> mountShape = ShapeUtilities.createCircle(position, (float) enlargedRadius);
            case HARDPOINT -> mountShape = new Rectangle2D.Double(
                    position.getX() - enlargedRadius,
                    position.getY() - enlargedRadius,
                    enlargedRadius * 2,
                    enlargedRadius * 2
            );
            case HIDDEN -> mountShape = ShapeUtilities.createCircumscribingTriangle(circle);
        }
        AffineTransform flipVertical = new AffineTransform();
        flipVertical.translate(position.getX(), position.getY());
        flipVertical.scale(1, -1);
        flipVertical.translate(-position.getX(), -position.getY());

        Shape flippedShape = flipVertical.createTransformedShape(mountShape);

        this.paintMount(g, worldToScreen, flippedShape, 1.0d, 24);
        WeaponSize slotSize = parentPoint.getWeaponSize();
        if (slotSize == WeaponSize.MEDIUM || slotSize == WeaponSize.LARGE) {
            double scaleMedium = 1.25d;
            this.paintMount(g, worldToScreen, flippedShape, scaleMedium, 28);
            if (slotSize == WeaponSize.LARGE) {
                double scaleLarge = 1.5d;
                this.paintMount(g, worldToScreen, flippedShape, scaleLarge, 32);
            }
        }
    }

    private void paintMount(Graphics2D g, AffineTransform worldToScreen, Shape input, double scale, double screenSize) {
        Point2D position = parentPoint.getPosition();
        AffineTransform transform = ShapeUtilities.getScaled(position, scale, scale);
        Shape enlarged = transform.createTransformedShape(input);
        Shape transformed = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, enlarged, screenSize);

        boolean hovered = StaticController.checkIsHovered(transformed);
        parentPoint.setCursorInBounds(hovered);
        Color mountColor = parentPoint.getCurrentColor();
        DrawUtilities.outlineShape(g, transformed, mountColor, 1.5f);
    }

    private void drawArc(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        Point2D position = parentPoint.getPosition();
        double slotArc = parentPoint.getArc();
        double halfArc = slotArc * 0.5d;
        double transformedAngle = this.transformAngle(parentPoint.getAngle());

        double arcStartAngle = transformedAngle - halfArc;

        double lineLength = 0.55f;

        Point2D arcStartEndpoint = ShapeUtilities.getPointInDirection(position, arcStartAngle, lineLength);

        Point2D arcStartCirclePoint = ShapeUtilities.getPointInDirection(position,
                arcStartAngle, circleRadius);

        double arcEndAngle = transformedAngle + halfArc;

        Point2D arcEndEndpoint = ShapeUtilities.getPointInDirection(position, arcEndAngle, lineLength);

        Point2D arcEndCirclePoint = ShapeUtilities.getPointInDirection(position,
                arcEndAngle, circleRadius);

        Shape arcStartLine = new Line2D.Double(arcStartEndpoint, arcStartCirclePoint);
        Shape arcEndLine = new Line2D.Double(arcEndEndpoint, arcEndCirclePoint);

        Ellipse2D enlargedCircle = ShapeUtilities.createCircle(position, 0.40f);
        Rectangle2D circleBounds = enlargedCircle.getBounds2D();
        Shape arcFigure = new Arc2D.Double(circleBounds.getX(), circleBounds.getY(),
                circleBounds.getWidth(), circleBounds.getHeight(), this.transformAngle(arcEndAngle - 90),
                slotArc, Arc2D.OPEN);

        GeneralPath combinedPath = new GeneralPath();
        combinedPath.append(circle, false);
        combinedPath.append(arcStartLine, false);
        combinedPath.append(arcEndLine, false);
        combinedPath.append(arcFigure, false);

        double radiusDistance = getScreenCircleRadius(worldToScreen, arcStartCirclePoint);

        this.drawCompositeFigure(g, worldToScreen, combinedPath,
                radiusDistance * 2.0d, parentPoint.getCurrentColor());
    }

    private double getScreenCircleRadius(AffineTransform worldToScreen, Point2D closestIntersection) {
        Point2D position = parentPoint.getPosition();
        Point2D transformedIntersection = worldToScreen.transform(closestIntersection, null);
        return transformedIntersection.distance(worldToScreen.transform(position, null));
    }

    private void drawAnglePointer(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        double transformedAngle = this.transformAngle(parentPoint.getAngle());
        Point2D position = parentPoint.getPosition();

        Point2D lineEndpoint = ShapeUtilities.getPointInDirection(position,
                transformedAngle, 0.5f);
        Point2D closestIntersection = ShapeUtilities.getPointInDirection(position,
                transformedAngle, circleRadius);

        Shape angleLine = new Line2D.Double(lineEndpoint, closestIntersection);

        GeneralPath combinedPath = new GeneralPath();
        combinedPath.append(circle, false);
        combinedPath.append(angleLine, false);

        double radiusDistance = getScreenCircleRadius(worldToScreen, closestIntersection);

        this.drawCompositeFigure(g, worldToScreen, combinedPath, radiusDistance * 2.0d, Color.WHITE);

        Shape baseCircleTransformed = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, circle, 12);

        DrawUtilities.drawOutlined(g, baseCircleTransformed, parentPoint.getCurrentColor(), false);
    }

    private void drawCompositeFigure(Graphics2D g, AffineTransform worldToScreen, Shape figure,
                                     double measurement, Paint color) {
        Point2D position = parentPoint.getPosition();

        Shape transformed = ShapeUtilities.ensureSpecialScaleShape(worldToScreen,
                position, figure, 12, measurement);

        DrawUtilities.drawOutlined(g, transformed, color, false,
                new BasicStroke(3.0f), new BasicStroke(2.25f));
    }

    @SuppressWarnings("MethodMayBeStatic")
    private double transformAngle(double raw) {
        double transformed = raw % 360;
        if (transformed < 0) {
            transformed += 360;
        }

        transformed = (360 - transformed) % 360;
        return transformed - 90;
    }

}

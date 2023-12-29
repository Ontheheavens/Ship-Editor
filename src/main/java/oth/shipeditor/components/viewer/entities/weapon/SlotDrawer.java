package oth.shipeditor.components.viewer.entities.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
@SuppressWarnings("ClassWithTooManyFields")
@Getter @Setter
public class SlotDrawer {

    private SlotPoint parentPoint;

    private Point2D pointPosition;

    private WeaponMount mount;

    private WeaponSize size;

    private WeaponType type;

    private double angle;

    private double arc;

    private double paintSizeMultiplier = 1;

    private boolean drawArc = true;

    private boolean drawAngle = true;

    private static final AffineTransform SCALE_TRANSFORM = new AffineTransform();

    public SlotDrawer(SlotPoint parent) {
        this.parentPoint = parent;
    }

    public void paintSlotVisuals(Graphics2D g, AffineTransform worldToScreen) {
        Point2D position = this.pointPosition;

        double circleRadius = 0.10f * paintSizeMultiplier;
        Ellipse2D circle = ShapeUtilities.createCircle(position, (float) circleRadius);
        this.drawMountShape(g, worldToScreen, circle, circleRadius);

        if (drawArc) {
            this.drawArc(g, worldToScreen, circle, circleRadius);
        }
        if (drawAngle) {
            this.drawAnglePointer(g, worldToScreen, circle, circleRadius);
        }
    }


    private void drawMountShape(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        Point2D position = this.pointPosition;
        Shape mountShape = null;
        WeaponMount slotMount = this.mount;
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
        WeaponSize slotSize = this.size;
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
        Point2D position = this.pointPosition;
        double finalScale = scale * paintSizeMultiplier;
        SCALE_TRANSFORM.setToIdentity();
        AffineTransform transform = ShapeUtilities.getScaled(position, SCALE_TRANSFORM,
                finalScale, finalScale);
        Shape enlarged = transform.createTransformedShape(input);
        Shape transformed = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, enlarged, screenSize * paintSizeMultiplier);

        boolean hovered = StaticController.checkIsHovered(transformed);
        Color mountColor = this.type.getColor();
        if (parentPoint != null) {
            parentPoint.setCursorInBounds(hovered);
            mountColor = parentPoint.getCurrentColor();
        }

        DrawUtilities.outlineShape(g, transformed, Color.BLACK, 2.5f);
        DrawUtilities.outlineShape(g, transformed, mountColor, 1.5f);
    }

    private void drawArc(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        Point2D position = this.pointPosition;
        double slotArc = this.arc;
        double halfArc = slotArc * 0.5d;
        double transformedAngle = Utility.transformAngle(this.angle);

        double arcStartAngle = transformedAngle - halfArc;

        double lineLength = 0.55f * paintSizeMultiplier;

        Point2D arcStartEndpoint = ShapeUtilities.getPointInDirection(position, arcStartAngle, lineLength);

        Point2D arcStartCirclePoint = ShapeUtilities.getPointInDirection(position,
                arcStartAngle, circleRadius);

        double arcEndAngle = transformedAngle + halfArc;

        Point2D arcEndEndpoint = ShapeUtilities.getPointInDirection(position, arcEndAngle, lineLength);

        Point2D arcEndCirclePoint = ShapeUtilities.getPointInDirection(position,
                arcEndAngle, circleRadius);

        Shape arcStartLine = new Line2D.Double(arcStartEndpoint, arcStartCirclePoint);
        Shape arcEndLine = new Line2D.Double(arcEndEndpoint, arcEndCirclePoint);

        Ellipse2D enlargedCircle = ShapeUtilities.createCircle(position, (float) (0.40f * paintSizeMultiplier));
        Rectangle2D circleBounds = enlargedCircle.getBounds2D();
        Shape arcFigure = new Arc2D.Double(circleBounds.getX(), circleBounds.getY(),
                circleBounds.getWidth(), circleBounds.getHeight(), Utility.transformAngle(arcEndAngle - 90),
                slotArc, Arc2D.OPEN);

        GeneralPath combinedPath = new GeneralPath();
        combinedPath.append(circle, false);
        combinedPath.append(arcStartLine, false);
        combinedPath.append(arcEndLine, false);
        combinedPath.append(arcFigure, false);

        double radiusDistance = ShapeUtilities.getScreenCircleRadius(worldToScreen, position, arcStartCirclePoint);

        Color color = this.type.getColor();
        if (parentPoint != null) {
            color = parentPoint.getCurrentColor();
        }

        DrawUtilities.drawCompositeFigure(g, worldToScreen, position, combinedPath,
                radiusDistance * 2.0d, color, paintSizeMultiplier);
    }

    private void drawAnglePointer(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        Point2D position = this.pointPosition;
        Color color = this.type.getColor();
        if (parentPoint != null) {
            color = parentPoint.getCurrentColor();
        }
        DrawUtilities.drawAngledCirclePointer(g, worldToScreen, circle, circleRadius,
                this.angle, position, color, paintSizeMultiplier);
    }

}

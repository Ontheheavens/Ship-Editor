package oth.shipeditor.components.viewer.entities.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
@SuppressWarnings({"WeakerAccess", "unused", "ClassWithTooManyMethods"})
public class WeaponSlotPoint extends BaseWorldPoint {

    @Getter @Setter
    private String id;

    @Setter
    private WeaponSize weaponSize;

    @Setter
    private WeaponType weaponType;

    @Setter
    private WeaponMount weaponMount;

    @Getter @Setter
    private int renderOrderMod;

    @Setter
    private double arc;

    @Setter
    private double angle;

    @Getter @Setter
    private WeaponSlotOverride skinOverride;

    @Getter @Setter
    private double transparency;

    public WeaponSlotPoint(Point2D pointPosition, ShipPainter layer) {
        super(pointPosition, layer);
    }

    public WeaponMount getWeaponMount() {
        if (skinOverride != null && skinOverride.getWeaponMount() != null) {
            return skinOverride.getWeaponMount();
        } else return weaponMount;
    }

    public WeaponSize getWeaponSize() {
        if (skinOverride != null && skinOverride.getWeaponSize() != null) {
            return skinOverride.getWeaponSize();
        } else {
            return weaponSize;
        }
    }

    public WeaponType getWeaponType() {
        if (skinOverride != null && skinOverride.getWeaponType() != null) {
            return skinOverride.getWeaponType();
        } else {
            return weaponType;
        }
    }

    public double getArc() {
        if (skinOverride != null && skinOverride.getArc() != null) {
            return skinOverride.getArc();
        } else {
            return arc;
        }
    }

    public double getAngle() {
        if (skinOverride != null && skinOverride.getAngle() != null) {
            return skinOverride.getAngle();
        } else {
            return angle;
        }
    }

    public void changeSlotAngle(double degrees) {
        EditDispatch.postSlotAngleSet(this,this.angle,degrees);
    }

    public void changeSlotArc(double degrees) {
        EditDispatch.postSlotArcSet(this,this.arc,degrees);
    }

    @Override
    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.WEAPON_SLOTS;
    }

    @Override
    protected Color createBaseColor() {
        WeaponType type = this.getWeaponType();
        return type.getColor();
    }

    @Override
    protected Color createSelectColor() {
        WeaponType type = this.getWeaponType();
        return type.getColor();
    }

    public String getNameForLabel() {
        return weaponType.getDisplayName();
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        float alpha = (float) this.getTransparency();
        Composite old = Utility.setAlphaComposite(g, alpha);

        Point2D position = this.getPosition();

        double circleRadius = 0.10f;

        Ellipse2D circle = ShapeUtilities.createCircle(position, (float) circleRadius);

        this.drawArc(g, worldToScreen, circle, circleRadius);
        this.drawAnglePointer(g, worldToScreen, circle, circleRadius);

        if (!isPointSelected()) {
            super.paint(g, worldToScreen, w, h);
        }

        g.setComposite(old);

        this.paintCoordsLabel(g, worldToScreen);
    }

    private void drawArc(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        Point2D position = this.getPosition();
        double slotArc = this.getArc();
        double halfArc = slotArc * 0.5d;
        double transformedAngle = this.transformAngle(this.angle);

        double arcStartAngle = transformedAngle - halfArc;

        double lineLength = 0.45f;

        Point2D arcStartEndpoint = ShapeUtilities.getPointInDirection(position, arcStartAngle, lineLength);

        Point2D arcStartCirclePoint = ShapeUtilities.getPointInDirection(position,
                arcStartAngle, circleRadius);

        double arcEndAngle = transformedAngle + halfArc;

        Point2D arcEndEndpoint = ShapeUtilities.getPointInDirection(position, arcEndAngle, lineLength);

        Point2D arcEndCirclePoint = ShapeUtilities.getPointInDirection(position,
                arcEndAngle, circleRadius);

        Shape arcStartLine = new Line2D.Double(arcStartEndpoint, arcStartCirclePoint);
        Shape arcEndLine = new Line2D.Double(arcEndEndpoint, arcEndCirclePoint);

        Ellipse2D enlargedCircle = ShapeUtilities.createCircle(position, 0.30f);
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
                radiusDistance * 2.0d, createBaseColor());
    }

    private double getScreenCircleRadius(AffineTransform worldToScreen, Point2D closestIntersection) {
        Point2D position = this.getPosition();
        Point2D transformedIntersection = worldToScreen.transform(closestIntersection, null);
        return transformedIntersection.distance(worldToScreen.transform(position, null));
    }

    private void drawAnglePointer(Graphics2D g, AffineTransform worldToScreen, Shape circle, double circleRadius) {
        double transformedAngle = this.transformAngle(this.angle);
        Point2D position = this.getPosition();

        Point2D lineEndpoint = ShapeUtilities.getPointInDirection(position,
                transformedAngle, 0.40f);
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

        DrawUtilities.drawOutlined(g, baseCircleTransformed, createBaseColor(), true);
    }

    private void drawCompositeFigure(Graphics2D g, AffineTransform worldToScreen, Shape figure,
                                     double measurement, Paint color) {
        Point2D position = this.getPosition();

        Shape transformed = ShapeUtilities.ensureSpecialScaleShape(worldToScreen,
                position, figure, 12, measurement);

        DrawUtilities.drawOutlined(g, transformed, color, true,
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

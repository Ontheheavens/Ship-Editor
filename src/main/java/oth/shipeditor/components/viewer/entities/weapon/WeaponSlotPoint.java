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
@SuppressWarnings({"WeakerAccess", "unused"})
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

        if (!isPointSelected()) {
            super.paint(g, worldToScreen, w, h);
        }
        Point2D position = this.getPosition();

        double circleRadius = 0.10f;

        Ellipse2D circle = ShapeUtilities.createCircle(position, (float) circleRadius);

        this.drawAnglePointer(g, worldToScreen, position, circle, circleRadius);

        g.setComposite(old);

        this.paintCoordsLabel(g, worldToScreen);
    }

    private void drawAnglePointer(Graphics2D g, AffineTransform worldToScreen,Point2D position,
                                  Shape circle, double circleRadius) {
        double transformedAngle = this.transformAngle(this.angle);

        Point2D lineEndpoint = ShapeUtilities.getPointInDirection(position,
                transformedAngle, 0.45f);
        Point2D closestIntersection = ShapeUtilities.getPointInDirection(position,
                transformedAngle, circleRadius);

        Shape angleLine = new Line2D.Double(lineEndpoint, closestIntersection);

        GeneralPath combinedPath = new GeneralPath();
        combinedPath.append(circle, false);
        combinedPath.append(angleLine, false);

        Point2D transformedIntersection = worldToScreen.transform(closestIntersection, null);
        double radiusDistance = transformedIntersection.distance(worldToScreen.transform(position, null));

        Shape transformed = ShapeUtilities.ensureSpecialScaleShape(worldToScreen,
                position, combinedPath, 8, radiusDistance);

        DrawUtilities.drawOutlined(g, transformed, createBaseColor(), true);
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

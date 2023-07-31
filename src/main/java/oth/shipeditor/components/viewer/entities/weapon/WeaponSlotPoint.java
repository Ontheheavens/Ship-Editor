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
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

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
        super.paint(g, worldToScreen, w, h);
        Point2D position = this.getPosition();
        double transformedAngle = this.transformAngle(this.angle);

        Shape angleLine = ShapeUtilities.createLineInDirection(position, transformedAngle, 0.4f);

        Shape transformedAngleLine = ShapeUtilities.ensureDynamicScaleShape(worldToScreen,
                position, angleLine, 12);

        DrawUtilities.drawOutlined(g, transformedAngleLine, createBaseColor());

        this.paintCoordsLabel(g, worldToScreen);
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

package oth.shipeditor.components.viewer.entities;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.components.viewer.ShipInstrument;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.graphics.DrawUtilities;
import oth.shipeditor.utility.graphics.ShapeUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
@Getter @Setter
public class WeaponSlotPoint extends BaseWorldPoint {

    private String id;

    private WeaponSize weaponSize;

    private WeaponType weaponType;

    private WeaponMount weaponMount;

    private int renderOrderMod;

    private double arc;

    private double angle;

    public WeaponSlotPoint(Point2D pointPosition, ShipPainter layer) {
        super(pointPosition, layer);
    }

    @Override
    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.WEAPON_SLOTS;
    }

    @Override
    protected Color createBaseColor() {
        return weaponType.getColor();
    }

    @Override
    protected Color createSelectColor() {
        return weaponType.getColor();
    }

    public String getNameForLabel() {
        return weaponType.getDisplayName() + " Slot" + " (Angle: " + angle + ")";
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

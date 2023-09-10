package oth.shipeditor.components.viewer.entities.weapon;

import lombok.Getter;
import lombok.Setter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.CoordsDisplayMode;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.entities.AngledPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.representation.ShipTypeHints;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.ColorUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 25.07.2023
 */
@SuppressWarnings("WeakerAccess")
public class WeaponSlotPoint extends AngledPoint implements SlotPoint {

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
    private double transparency = 0.8d;

    private SlotDrawingHelper drawingHelper;

    public WeaponSlotPoint(Point2D pointPosition, ShipPainter layer) {
        this(pointPosition, layer, null);
    }

    public WeaponSlotPoint(Point2D pointPosition, ShipPainter layer, WeaponSlotPoint valuesSource) {
        super(pointPosition, layer);
        this.initHelper();
        if (valuesSource != null) {
            this.setWeaponSize(valuesSource.weaponSize);
            this.setWeaponType(valuesSource.weaponType);
            this.setWeaponMount(valuesSource.weaponMount);
            this.setAngle(valuesSource.angle);
            this.setArc(valuesSource.arc);
        }
    }

    @Override
    public ShipPainter getParent() {
        return (ShipPainter) super.getParent();
    }

    private void initHelper() {
        this.drawingHelper = new SlotDrawingHelper(this);
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
        if (skinOverride != null && skinOverride.getBoxedArc() != null) {
            return skinOverride.getBoxedArc();
        } else {
            return arc;
        }
    }

    @Override
    public double getAngle() {
        if (skinOverride != null && skinOverride.getBoxedAngle() != null) {
            return skinOverride.getAngle();
        } else {
            return angle;
        }
    }

    public void changeSlotID(String newId) {
        ShipPainter parent = this.getParent();
        if (!parent.isGeneratedIDUnassigned(newId)) {
            EventBus.publish(new ViewerRepaintQueued());
            EventBus.publish(new SlotControlRepaintQueued());
            return;
        }

        this.setId(newId);
        WeaponSlotPainter.setSlotOverrideFromSkin(this, parent.getActiveSkin());
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    public void changeSlotType(WeaponType newType) {
        if (skinOverride != null && skinOverride.getWeaponType() != null) return;
        EditDispatch.postSlotTypeChanged(this, newType);
    }

    public void changeSlotMount(WeaponMount newMount) {
        if (skinOverride != null && skinOverride.getWeaponMount() != null) return;
        EditDispatch.postSlotMountChanged(this, newMount);
    }

    public void changeSlotSize(WeaponSize newSize) {
        if (skinOverride != null && skinOverride.getWeaponSize() != null) return;
        EditDispatch.postSlotSizeChanged(this, newSize);
    }

    public void changeSlotAngle(double degrees) {
        EditDispatch.postSlotAngleSet(this,this.angle,degrees);
    }

    public void changeSlotArc(double degrees) {
        EditDispatch.postSlotArcSet(this,this.arc,degrees);
    }

    @Override
    public EditorInstrument getAssociatedMode() {
        return EditorInstrument.WEAPON_SLOTS;
    }

    @Override
    protected Color createBaseColor() {
        WeaponType type = this.getWeaponType();
        return type.getColor();
    }

    @Override
    protected Color createSelectColor() {
        Color base = this.createBaseColor();
        return ColorUtilities.getBlendedColor(base, Color.WHITE, 0.5);
    }

    public String getNameForLabel() {
        WeaponType type = getWeaponType();
        return type.getDisplayName();
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        float alpha = (float) this.getTransparency();
        Composite old = Utility.setAlphaComposite(g, alpha);

        drawingHelper.setPointPosition(this.getPosition());
        drawingHelper.setType(this.getWeaponType());
        drawingHelper.setMount(this.getWeaponMount());
        drawingHelper.setSize(this.getWeaponSize());
        drawingHelper.setAngle(this.getAngle());
        drawingHelper.setArc(this.getArc());
        drawingHelper.paintSlotVisuals(g, worldToScreen);

        if (!isPointSelected()) {
            super.paint(g, worldToScreen, w, h);
        }

        g.setComposite(old);

        this.paintCoordsLabel(g, worldToScreen);
    }

    public double getOffsetRelativeToAxis() {
        double result;

        ShipPainter shipPainter = this.getParent();
        List<ShipTypeHints> hints = shipPainter.getHintsModified();

        Point2D locationRelativeToCenter = Utility.getPointCoordinatesForDisplay(this.getPosition(),
                shipPainter, CoordsDisplayMode.SHIP_CENTER);

        if (hints.contains(ShipTypeHints.WEAPONS_FRONT_TO_BACK)) {
            result = Math.abs(locationRelativeToCenter.getY() / 50000)
                    + (locationRelativeToCenter.getX() / 10000);
        } else if (hints.contains(ShipTypeHints.WEAPONS_BACK_TO_FRONT)) {
            result = Math.abs(locationRelativeToCenter.getY() / 50000)
                    - (locationRelativeToCenter.getX() / 10000);
        } else {
            result = Math.abs(locationRelativeToCenter.getY() / 10000)
                    + Math.abs(locationRelativeToCenter.getX() / 10000000);
        }
        return result;
    }

}

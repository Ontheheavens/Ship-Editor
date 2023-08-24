package oth.shipeditor.components.viewer.entities.bays;

import lombok.Getter;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.BaysPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.instrument.ship.ShipInstrument;
import oth.shipeditor.components.viewer.entities.BaseWorldPoint;
import oth.shipeditor.components.viewer.entities.weapon.SlotDrawingHelper;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotOverride;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.TextPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.graphics.ColorUtilities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public class LaunchPortPoint extends BaseWorldPoint implements SlotPoint {

    @Getter
    private final LaunchBay parentBay;

    private SlotDrawingHelper drawingHelper;

    public LaunchPortPoint(Point2D pointPosition, ShipPainter layer, LaunchBay bay) {
        super(pointPosition, layer);
        this.parentBay = bay;
        this.initHelper();
    }

    public String getId() {
        return parentBay.getId();
    }

    @Override
    public void changeSlotID(String newId) {
        ShipPainter parent = (ShipPainter) this.getParentLayer();
        if (!parent.isGeneratedIDUnassigned(newId)) {
            EventBus.publish(new ViewerRepaintQueued());
            EventBus.publish(new BaysPanelRepaintQueued());
            return;
        }
        parentBay.setId(newId);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new BaysPanelRepaintQueued());
    }

    @Override
    public WeaponType getWeaponType() {
        return parentBay.getWeaponType();
    }

    @Override
    public void setWeaponType(WeaponType newType) {
        throw new UnsupportedOperationException("Type change is not relevant for launch bays!");
    }

    @Override
    public WeaponMount getWeaponMount() {
        return parentBay.getWeaponMount();
    }

    @Override
    public void setWeaponMount(WeaponMount newMount) {
        parentBay.setWeaponMount(newMount);
    }

    @Override
    public WeaponSize getWeaponSize() {
        return parentBay.getWeaponSize();
    }

    @Override
    public void setWeaponSize(WeaponSize newSize) {
        parentBay.setWeaponSize(newSize);
    }

    @Override
    public double getArc() {
        return parentBay.getArc();
    }

    @Override
    public void setArc(double degrees) {
        parentBay.setArc(degrees);
    }

    @Override
    public double getAngle() {
        return parentBay.getAngle();
    }

    @Override
    public void setAngle(double degrees) {
        parentBay.setAngle(degrees);
    }

    @Override
    public WeaponSlotOverride getSkinOverride() {
        return null;
    }

    private void initHelper() {
        this.drawingHelper = new SlotDrawingHelper(this);
        drawingHelper.setDrawAngle(false);
        drawingHelper.setDrawArc(false);
    }

    @Override
    public ShipInstrument getAssociatedMode() {
        return ShipInstrument.LAUNCH_BAYS;
    }

    @Override
    protected Color createBaseColor() {
        WeaponType type = this.parentBay.getWeaponType();
        return type.getColor();
    }

    @Override
    protected Color createSelectColor() {
        Color base = this.createBaseColor();
        return ColorUtilities.getBlendedColor(base, Color.WHITE, 0.5);
    }

    public String getNameForLabel() {
        return this.parentBay.getId();
    }

    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {

        drawingHelper.setPointPosition(this.getPosition());
        drawingHelper.setType(this.parentBay.getWeaponType());
        drawingHelper.setMount(this.parentBay.getWeaponMount());
        drawingHelper.setSize(this.parentBay.getWeaponSize());
        drawingHelper.setAngle(this.parentBay.getAngle());
        drawingHelper.setArc(this.parentBay.getArc());
        drawingHelper.paintSlotVisuals(g, worldToScreen);

        if (!isPointSelected()) {
            super.paint(g, worldToScreen, w, h);
        }

        this.paintCoordsLabel(g, worldToScreen);
    }

    @Override
    protected void paintCoordsLabel(Graphics2D g, AffineTransform worldToScreen) {
        Point2D coordsPoint = getPosition();

        TextPainter coordsLabel = this.getCoordsLabel();

        String coords = getNameForLabel();

        Font font = Utility.getOrbitron(12);

        coordsLabel.setWorldPosition(coordsPoint);
        coordsLabel.setText(coords);
        coordsLabel.paintText(g, worldToScreen, font);
    }

    public String getIndexToDisplay() {
        List<LaunchPortPoint> portPoints = parentBay.getPortPoints();
        return "#" + portPoints.indexOf(this);
    }

}

package oth.shipeditor.undo;

import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.communication.events.viewer.control.ViewerMouseReleased;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.components.viewer.entities.*;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.BoundPointsPainter;
import oth.shipeditor.components.viewer.painters.points.MirrorablePointPainter;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.edits.AnchorOffsetEdit;
import oth.shipeditor.undo.edits.LayerRotationEdit;
import oth.shipeditor.undo.edits.points.*;
import oth.shipeditor.undo.edits.points.slots.*;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Convenience class meant to free viewer classes from burden of also implementing all the edit dispatch methods.
 * @author Ontheheavens
 * @since 16.06.2023
 */
@SuppressWarnings("OverlyCoupledClass")
public final class EditDispatch {

    private EditDispatch() {
    }

    private static void handleContinuousEdit(Edit edit) {
        Class<? extends Edit> editClass = edit.getClass();
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (editClass.isInstance(previousEdit) && !previousEdit.isFinished()) {
            edit.setFinished(true);
            previousEdit.add(edit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !edit.isFinished()) {
                        edit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(edit);
        }
    }

    public static void postPointInserted(MirrorablePointPainter pointPainter, BoundPoint point, int index) {
        Edit addEdit = new PointAdditionEdit(pointPainter, point, index);
        UndoOverseer.post(addEdit);
        pointPainter.insertPoint(point, index);
        Events.repaintView();
    }

    public static void postBoundsRearranged(BoundPointsPainter pointPainter,
                                            List<BoundPoint> old,
                                            List<BoundPoint> changed) {
        Edit rearrangeEdit = new BoundsSortEdit(pointPainter, old, changed);
        UndoOverseer.post(rearrangeEdit);
        pointPainter.setBoundPoints(changed);
        Events.repaintView();
    }

    public static void postSlotsRearranged(WeaponSlotPainter pointPainter,
                                           List<WeaponSlotPoint> old,
                                           List<WeaponSlotPoint> changed) {
        Edit rearrangeEdit = new WeaponSlotsSortEdit(pointPainter, old, changed);
        UndoOverseer.post(rearrangeEdit);
        pointPainter.setSlotPoints(changed);
        Events.repaintView();
    }

    public static void postPointAdded(AbstractPointPainter pointPainter, BaseWorldPoint point) {
        Edit addEdit = new PointAdditionEdit(pointPainter, point);
        UndoOverseer.post(addEdit);
        pointPainter.addPoint(point);
        Events.repaintView();
    }

    public static void postPointRemoved(AbstractPointPainter pointPainter, BaseWorldPoint point) {
        int index = pointPainter.getIndexOfPoint(point);
        if (index == -1) return;
        Edit removeEdit = new PointRemovalEdit(pointPainter, point, index);
        UndoOverseer.post(removeEdit);
        pointPainter.removePoint(point);
        Events.repaintView();
    }

    public static void postAnchorOffsetChanged(LayerPainter layerPainter, Point2D updated) {
        Point2D oldOffset = layerPainter.getAnchor();
        Edit offsetChangeEdit = new AnchorOffsetEdit(layerPainter, oldOffset, updated);
        EditDispatch.handleContinuousEdit(offsetChangeEdit);
        Point2D difference = new Point2D.Double(oldOffset.getX() - updated.getX(),
                oldOffset.getY() - updated.getY());
        EventBus.publish(new AnchorOffsetQueued(layerPainter, difference));
        layerPainter.setAnchor(updated);
        Events.repaintView();
    }

    public static void postSlotAngleSet(SlotPoint slotPoint, double old, double updated ) {
        Edit angleEdit = new SlotAngleSet(slotPoint, old, updated);
        EditDispatch.handleContinuousEdit(angleEdit);
        slotPoint.setAngle(updated);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    public static void postSlotArcSet(SlotPoint slotPoint, double old, double updated ) {
        Edit arcEdit = new SlotArcSet(slotPoint, old, updated);
        EditDispatch.handleContinuousEdit(arcEdit);
        slotPoint.setArc(updated);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    public static void postLayerRotated(LayerPainter painter, double old, double updated) {
        Edit rotationEdit = new LayerRotationEdit(painter, old, updated);
        EditDispatch.handleContinuousEdit(rotationEdit);
        painter.setRotationRadians(updated);
        Events.repaintView();
    }

    public static void postPointDragged(WorldPoint selected, Point2D changedPosition) {
        Point2D position = selected.getPosition();
        Point2D wrappedOld = new Point2D.Double(position.getX(), position.getY());
        Point2D wrappedNew = new Point2D.Double(changedPosition.getX(), changedPosition.getY());
        Edit dragEdit = new PointDragEdit(selected, wrappedOld, wrappedNew);
        EditDispatch.handleContinuousEdit(dragEdit);
        selected.setPosition(changedPosition);
        Events.repaintView();
    }

    public static void postCollisionRadiusChanged(ShipCenterPoint point, float radius) {
        float oldRadius = point.getCollisionRadius();
        Edit radiusEdit = new CollisionRadiusEdit(point, oldRadius, radius);
        EditDispatch.handleContinuousEdit(radiusEdit);
        point.setCollisionRadius(radius);
        Events.repaintView();
    }

    public static void postShieldRadiusChanged(ShieldCenterPoint point, float radius) {
        float oldRadius = point.getShieldRadius();
        Edit radiusEdit = new ShieldRadiusEdit(point, oldRadius, radius);
        EditDispatch.handleContinuousEdit(radiusEdit);
        point.setShieldRadius(radius);
        Events.repaintView();
    }

    public static void postSlotIDChanged(SlotPoint point, String newID) {
        String oldID = point.getId();
        Edit renameEdit = new SlotIDChangeEdit(point, newID, oldID);
        UndoOverseer.post(renameEdit);
        point.changeSlotID(newID);
    }

    public static void postSlotTypeChanged(SlotPoint point, WeaponType newType) {
        WeaponType oldType = point.getWeaponType();
        Edit typeChangeEdit = new SlotTypeChangeEdit(point, oldType, newType);
        UndoOverseer.post(typeChangeEdit);
        point.setWeaponType(newType);
    }

    public static void postSlotMountChanged(SlotPoint point, WeaponMount newMount) {
        WeaponMount oldMount = point.getWeaponMount();
        Edit mountChangeEdit = new SlotMountChangeEdit(point, oldMount, newMount);
        UndoOverseer.post(mountChangeEdit);
        point.setWeaponMount(newMount);
    }

    public static void postSlotSizeChanged(SlotPoint point, WeaponSize newSize) {
        WeaponSize oldSize = point.getWeaponSize();
        Edit sizeChangeEdit = new SlotSizeChangeEdit(point, oldSize, newSize);
        UndoOverseer.post(sizeChangeEdit);
        point.setWeaponSize(newSize);
    }

}

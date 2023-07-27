package oth.shipeditor.undo;

import oth.shipeditor.communication.BusEventListener;
import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.BusEvent;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.control.ViewerMouseReleased;
import oth.shipeditor.communication.events.viewer.points.AnchorOffsetQueued;
import oth.shipeditor.components.viewer.entities.*;
import oth.shipeditor.components.viewer.layers.LayerPainter;
import oth.shipeditor.components.viewer.painters.points.AbstractPointPainter;
import oth.shipeditor.components.viewer.painters.points.BoundPointsPainter;
import oth.shipeditor.undo.edits.*;

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

    public static void postPointInserted(BoundPointsPainter pointPainter, BoundPoint point, int index) {
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
        Point2D oldOffset = layerPainter.getAnchorOffset();
        AnchorOffsetEdit offsetChangeEdit = new AnchorOffsetEdit(layerPainter, oldOffset, updated);

        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (previousEdit instanceof AnchorOffsetEdit checked && !checked.isFinished()) {
            offsetChangeEdit.setFinished(true);
            checked.add(offsetChangeEdit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !offsetChangeEdit.isFinished()) {
                        offsetChangeEdit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(offsetChangeEdit);
        }
        Point2D difference = new Point2D.Double(oldOffset.getX() - updated.getX(),
                oldOffset.getY() - updated.getY());
        EventBus.publish(new AnchorOffsetQueued(layerPainter, difference));
        layerPainter.setAnchorOffset(updated);
        Events.repaintView();
    }

    public static void postLayerRotated(LayerPainter painter, double old, double updated) {
        LayerRotationEdit rotationEdit = new LayerRotationEdit(painter, old, updated);
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (previousEdit instanceof LayerRotationEdit checked && !checked.isFinished()) {
            rotationEdit.setFinished(true);
            checked.add(rotationEdit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !rotationEdit.isFinished()) {
                        rotationEdit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(rotationEdit);
        }
        painter.setRotationRadians(updated);
        Events.repaintView();
    }

    public static void postPointDragged(WorldPoint selected, Point2D changedPosition) {
        Point2D position = selected.getPosition();
        Point2D wrappedOld = new Point2D.Double(position.getX(), position.getY());
        Point2D wrappedNew = new Point2D.Double(changedPosition.getX(), changedPosition.getY());
        PointDragEdit dragEdit = new PointDragEdit(selected, wrappedOld, wrappedNew);
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (previousEdit instanceof PointDragEdit checked && !checked.isFinished()) {
            dragEdit.setFinished(true);
            checked.add(dragEdit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !dragEdit.isFinished()) {
                        dragEdit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(dragEdit);
        }
        selected.setPosition(changedPosition);
        Events.repaintView();
    }

    public static void postCollisionRadiusChanged(ShipCenterPoint point, float radius) {
        float oldRadius = point.getCollisionRadius();
        CollisionRadiusEdit radiusEdit = new CollisionRadiusEdit(point, oldRadius, radius);
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (previousEdit instanceof CollisionRadiusEdit checked && !checked.isFinished()) {
            radiusEdit.setFinished(true);
            checked.add(radiusEdit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !radiusEdit.isFinished()) {
                        radiusEdit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(radiusEdit);
        }
        point.setCollisionRadius(radius);
        Events.repaintView();
    }

    public static void postShieldRadiusChanged(ShieldCenterPoint point, float radius) {
        float oldRadius = point.getShieldRadius();
        ShieldRadiusEdit radiusEdit = new ShieldRadiusEdit(point, oldRadius, radius);
        Edit previousEdit = UndoOverseer.getNextUndoable();
        if (previousEdit instanceof ShieldRadiusEdit checked && !checked.isFinished()) {
            radiusEdit.setFinished(true);
            checked.add(radiusEdit);
        } else {
            EventBus.subscribe(new BusEventListener() {
                @Override
                public void handleEvent(BusEvent event) {
                    if (event instanceof ViewerMouseReleased && !radiusEdit.isFinished()) {
                        radiusEdit.setFinished(true);
                        EventBus.unsubscribe(this);
                    }
                }
            });
            UndoOverseer.post(radiusEdit);
        }
        point.setShieldRadius(radius);
        Events.repaintView();
    }

}

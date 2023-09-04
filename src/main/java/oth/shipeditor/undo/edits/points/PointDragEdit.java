package oth.shipeditor.undo.edits.points;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 16.06.2023
 */
public final class PointDragEdit extends AbstractEdit implements PointEdit {

    private final WorldPoint point;
    private final Point2D oldPosition;
    private final Point2D newPosition;

    public PointDragEdit(WorldPoint worldPoint, Point2D oldInput, Point2D newInput) {
        this.point = worldPoint;
        this.oldPosition = oldInput;
        this.newPosition = newInput;
        this.setFinished(false);
    }

    public void adjustPositionOffset(Point2D offset) {
        oldPosition.setLocation(oldPosition.getX() - offset.getX(), oldPosition.getY() - offset.getY());
        newPosition.setLocation(newPosition.getX() - offset.getX(), newPosition.getY() - offset.getY());
    }

    @Override
    public String getName() {
        return "Point Drag";
    }

    @Override
    public void undo() {
        undoSubEdits();
        point.setPosition(oldPosition);
        PointDragEdit.repaintByPointType(point);
    }

    @Override
    public void redo() {
        point.setPosition(newPosition);
        redoSubEdits();
        PointDragEdit.repaintByPointType(point);
    }

    @Override
    public WorldPoint getPoint() {
        return point;
    }

    public static void repaintByPointType(WorldPoint point) {
        EventBus.publish(new ViewerRepaintQueued());
        if (point == null) return;
        var repainter = StaticController.getRepainter();
        switch (point.getAssociatedMode()) {
            case BOUNDS -> repainter.queueBoundsPanelRepaint();
            case COLLISION, SHIELD -> repainter.queueCenterPanelsRepaint();
            case WEAPON_SLOTS -> repainter.queueSlotControlRepaint();
            case ENGINES -> repainter.queueEnginesPanelRepaint();
        }
    }

}

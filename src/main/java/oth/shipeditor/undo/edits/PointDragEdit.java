package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.undo.AbstractEdit;

import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 16.06.2023
 */
public final class PointDragEdit extends AbstractEdit {

    private final WorldPoint point;
    private final Point2D oldPosition;
    private final Point2D newPosition;

    public PointDragEdit(WorldPoint worldPoint, Point2D oldInput, Point2D newInput) {
        this.point = worldPoint;
        this.oldPosition = oldInput;
        this.newPosition = newInput;
        this.setFinished(false);
    }

    @Override
    public String getName() {
        return "Point Drag";
    }

    @Override
    public void undo() {
        undoSubEdits();
        point.setPosition(oldPosition);
        EventBus.publish(new ViewerRepaintQueued());
    }

    @Override
    public void redo() {
        point.setPosition(newPosition);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
    }

    @Override
    public String toString() {
        Class<? extends PointDragEdit> identity = this.getClass();
        return identity.getSimpleName() + " " + hashCode();
    }

}

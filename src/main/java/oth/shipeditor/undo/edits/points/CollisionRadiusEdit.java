package oth.shipeditor.undo.edits.points;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 17.06.2023
 */
public final class CollisionRadiusEdit extends AbstractEdit {

    private final ShipCenterPoint parentPoint;

    private final float oldRadius;

    private final float newRadius;

    public CollisionRadiusEdit(ShipCenterPoint point, float oldValue, float newValue) {
        this.parentPoint = point;
        this.oldRadius = oldValue;
        this.newRadius = newValue;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        parentPoint.setCollisionRadius(oldRadius);
        Events.repaintView();
    }

    @Override
    public void redo() {
        redoSubEdits();
        parentPoint.setCollisionRadius(newRadius);
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Set Collision";
    }

}

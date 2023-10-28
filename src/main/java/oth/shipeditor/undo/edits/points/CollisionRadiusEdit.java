package oth.shipeditor.undo.edits.points;

import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

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
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueCenterPanelsRepaint();
    }

    @Override
    public void redo() {
        redoSubEdits();
        parentPoint.setCollisionRadius(newRadius);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueCenterPanelsRepaint();
    }

    @Override
    public String getName() {
        return "Set Collision";
    }

}

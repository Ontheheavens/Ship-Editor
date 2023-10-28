package oth.shipeditor.undo.edits.points;

import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 16.07.2023
 */
public class ShieldRadiusEdit extends AbstractEdit {

    private final ShieldCenterPoint parentPoint;

    private final float oldRadius;

    private final float newRadius;

    public ShieldRadiusEdit(ShieldCenterPoint point, float oldValue, float newValue) {
        this.parentPoint = point;
        this.oldRadius = oldValue;
        this.newRadius = newValue;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        parentPoint.setShieldRadius(oldRadius);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueCenterPanelsRepaint();
    }

    @Override
    public void redo() {
        redoSubEdits();
        parentPoint.setShieldRadius(newRadius);
        var repainter = StaticController.getScheduler();
        repainter.queueViewerRepaint();
        repainter.queueCenterPanelsRepaint();
    }

    @Override
    public String getName() {
        return "Set Shield Radius";
    }

}

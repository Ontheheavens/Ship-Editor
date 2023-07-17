package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.undo.AbstractEdit;

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
        Events.repaintView();
    }

    @Override
    public void redo() {
        redoSubEdits();
        parentPoint.setShieldRadius(newRadius);
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Set Shield Radius";
    }

}

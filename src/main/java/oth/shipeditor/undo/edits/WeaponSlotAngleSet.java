package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.WeaponSlotPoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class WeaponSlotAngleSet extends AbstractEdit {

    private final WeaponSlotPoint slotPoint;

    private final double oldAngle;

    private final double updatedAngle;

    public WeaponSlotAngleSet(WeaponSlotPoint point, double old, double updated) {
        this.slotPoint = point;
        this.oldAngle = old;
        this.updatedAngle = updated;
        this.setFinished(false);
    }


    @Override
    public void undo() {
        undoSubEdits();
        slotPoint.setAngle(oldAngle);
        Events.repaintView();
    }

    @Override
    public void redo() {
        slotPoint.setAngle(updatedAngle);
        redoSubEdits();
        Events.repaintView();
    }

    @Override
    public String getName() {
        return "Change Slot Angle";
    }

}

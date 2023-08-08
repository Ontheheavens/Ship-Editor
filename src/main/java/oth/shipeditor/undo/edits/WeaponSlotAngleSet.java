package oth.shipeditor.undo.edits;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotsPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
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
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotsPanelRepaintQueued());
    }

    @Override
    public void redo() {
        slotPoint.setAngle(updatedAngle);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotsPanelRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Slot Angle";
    }

}

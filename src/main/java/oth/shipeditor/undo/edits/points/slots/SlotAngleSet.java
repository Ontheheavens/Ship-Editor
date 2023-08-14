package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class SlotAngleSet extends AbstractEdit {

    private final SlotPoint slotPoint;

    private final double oldAngle;

    private final double updatedAngle;

    public SlotAngleSet(SlotPoint point, double old, double updated) {
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
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public void redo() {
        slotPoint.setAngle(updatedAngle);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Slot Angle";
    }

}

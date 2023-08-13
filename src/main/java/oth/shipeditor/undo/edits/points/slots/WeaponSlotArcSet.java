package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 09.08.2023
 */
public class WeaponSlotArcSet extends AbstractEdit {

    private final WeaponSlotPoint slotPoint;

    private final double oldArc;

    private final double updatedArc;

    public WeaponSlotArcSet(WeaponSlotPoint point, double old, double updated) {
        this.slotPoint = point;
        this.oldArc = old;
        this.updatedArc = updated;
        this.setFinished(false);
    }

    @Override
    public void undo() {
        undoSubEdits();
        slotPoint.setArc(oldArc);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public void redo() {
        slotPoint.setArc(updatedArc);
        redoSubEdits();
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public String getName() {
        return "Change Slot Arc";
    }

}

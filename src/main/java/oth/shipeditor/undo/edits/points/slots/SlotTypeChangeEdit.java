package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotTypeChangeEdit extends AbstractEdit {

    private final WeaponSlotPoint slot;

    private final WeaponType old;

    private final WeaponType updated;

    public SlotTypeChangeEdit(WeaponSlotPoint point, WeaponType oldType, WeaponType newType) {
        this.slot = point;
        this.old = oldType;
        this.updated = newType;
    }

    @Override
    public void undo() {
        slot.setWeaponType(old);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public void redo() {
        slot.setWeaponType(updated);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public String getName() {
        return "Slot Type Change";
    }

}

package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotIDChangeEdit extends AbstractEdit {

    private final WeaponSlotPoint slot;

    private final String old;

    private final String updated;

    public SlotIDChangeEdit(WeaponSlotPoint point, String newID, String oldID) {
        this.slot = point;
        this.old = oldID;
        this.updated = newID;
    }

    @Override
    public void undo() {
        slot.changeSlotID(old);
    }

    @Override
    public void redo() {
        slot.changeSlotID(updated);
    }

    @Override
    public String getName() {
        return "Slot ID Change";
    }

}

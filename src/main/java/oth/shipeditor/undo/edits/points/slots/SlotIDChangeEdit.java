package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotIDChangeEdit extends AbstractEdit {

    private final SlotData slot;

    private final String old;

    private final String updated;

    public SlotIDChangeEdit(SlotData point, String newID, String oldID) {
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

package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotTypeChangeEdit extends AbstractEdit {

    private final SlotData slot;

    private final WeaponType old;

    private final WeaponType updated;

    public SlotTypeChangeEdit(SlotData point, WeaponType oldType, WeaponType newType) {
        this.slot = point;
        this.old = oldType;
        this.updated = newType;
    }

    @Override
    public void undo() {
        slot.setWeaponType(old);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
    }

    @Override
    public void redo() {
        slot.setWeaponType(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
    }

    @Override
    public String getName() {
        return "Slot Type Change";
    }

}

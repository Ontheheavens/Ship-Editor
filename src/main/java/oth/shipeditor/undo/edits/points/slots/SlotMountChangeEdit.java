package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotMountChangeEdit extends AbstractEdit {

    private final SlotData slot;

    private final WeaponMount old;

    private final WeaponMount updated;

    public SlotMountChangeEdit(SlotData point, WeaponMount oldMount, WeaponMount newMount) {
        this.slot = point;
        this.old = oldMount;
        this.updated = newMount;
    }

    @Override
    public void undo() {
        slot.setWeaponMount(old);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public void redo() {
        slot.setWeaponMount(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public String getName() {
        return "Slot Mount Change";
    }

}

package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.SlotData;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotSizeChangeEdit extends AbstractEdit {

    private final SlotData slot;

    private final WeaponSize old;

    private final WeaponSize updated;

    public SlotSizeChangeEdit(SlotData point, WeaponSize oldSize, WeaponSize newSize) {
        this.slot = point;
        this.old = oldSize;
        this.updated = newSize;
    }

    @Override
    public void undo() {
        slot.setWeaponSize(old);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public void redo() {
        slot.setWeaponSize(updated);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
        repainter.queueBaysPanelRepaint();
        repainter.queueBuiltInsRepaint();
        repainter.queueBaysPanelRepaint();
    }

    @Override
    public String getName() {
        return "Slot Size Change";
    }

}

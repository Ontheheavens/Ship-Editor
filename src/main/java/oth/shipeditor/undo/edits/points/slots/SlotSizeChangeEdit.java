package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.representation.weapon.WeaponSize;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotSizeChangeEdit extends AbstractEdit {

    private final WeaponSlotPoint slot;

    private final WeaponSize old;

    private final WeaponSize updated;

    public SlotSizeChangeEdit(WeaponSlotPoint point, WeaponSize oldSize, WeaponSize newSize) {
        this.slot = point;
        this.old = oldSize;
        this.updated = newSize;
    }

    @Override
    public void undo() {
        slot.setWeaponSize(old);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public void redo() {
        slot.setWeaponSize(old);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public String getName() {
        return "Slot Size Change";
    }

}

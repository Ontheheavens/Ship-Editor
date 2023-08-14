package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.SlotPoint;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.undo.AbstractEdit;

/**
 * @author Ontheheavens
 * @since 12.08.2023
 */
public class SlotMountChangeEdit extends AbstractEdit {

    private final SlotPoint slot;

    private final WeaponMount old;

    private final WeaponMount updated;

    public SlotMountChangeEdit(SlotPoint point, WeaponMount oldMount, WeaponMount newMount) {
        this.slot = point;
        this.old = oldMount;
        this.updated = newMount;
    }

    @Override
    public void undo() {
        slot.setWeaponMount(old);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public void redo() {
        slot.setWeaponMount(updated);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public String getName() {
        return "Slot Mount Change";
    }

}

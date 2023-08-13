package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.SlotControlRepaintQueued;
import oth.shipeditor.communication.events.viewer.ViewerRepaintQueued;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.undo.AbstractEdit;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 13.08.2023
 */
public class WeaponSlotsSortEdit extends AbstractEdit {

    private final WeaponSlotPainter pointPainter;

    private final List<WeaponSlotPoint> oldList;

    private final List<WeaponSlotPoint> newList;

    public WeaponSlotsSortEdit(WeaponSlotPainter painter, List<WeaponSlotPoint> old, List<WeaponSlotPoint> changed) {
        this.pointPainter = painter;
        this.oldList = old;
        this.newList = changed;
    }

    @Override
    public void undo() {
        pointPainter.setSlotPoints(oldList);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public void redo() {
        pointPainter.setSlotPoints(newList);
        EventBus.publish(new ViewerRepaintQueued());
        EventBus.publish(new SlotControlRepaintQueued());
    }

    @Override
    public String getName() {
        return "Sort Weapon Slots";
    }

}

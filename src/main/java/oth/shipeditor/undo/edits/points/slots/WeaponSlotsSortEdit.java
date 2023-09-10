package oth.shipeditor.undo.edits.points.slots;

import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.painters.points.WeaponSlotPainter;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.StaticController;

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
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
    }

    @Override
    public void redo() {
        pointPainter.setSlotPoints(newList);
        var repainter = StaticController.getRepainter();
        repainter.queueViewerRepaint();
        repainter.queueSlotControlRepaint();
    }

    @Override
    public String getName() {
        return "Sort Weapon Slots";
    }

}

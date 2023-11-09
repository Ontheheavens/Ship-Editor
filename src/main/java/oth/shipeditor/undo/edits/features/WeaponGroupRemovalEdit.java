package oth.shipeditor.undo.edits.features;

import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;

/**
 * @author Ontheheavens
 * @since 09.11.2023
 */
public class WeaponGroupRemovalEdit extends AbstractEdit {

    private final List<FittedWeaponGroup> weaponGroups;
    private final int groupIndex;
    private final FittedWeaponGroup toRemove;

    @SuppressWarnings("ParameterHidesMemberVariable")
    public WeaponGroupRemovalEdit(List<FittedWeaponGroup> weaponGroups, int groupIndex, FittedWeaponGroup toRemove) {
        this.weaponGroups = weaponGroups;
        this.groupIndex = groupIndex;
        this.toRemove = toRemove;
    }

    @Override
    public void undo() {
        weaponGroups.add(groupIndex, toRemove);
        if (StaticController.getEditorMode() == EditorInstrument.VARIANT_WEAPONS) {
            StaticController.reselectCurrentLayer();
            var repainter = StaticController.getScheduler();
            repainter.queueViewerRepaint();
        }
    }

    @Override
    public void redo() {
        weaponGroups.remove(toRemove);
        if (StaticController.getEditorMode() == EditorInstrument.VARIANT_WEAPONS) {
            StaticController.reselectCurrentLayer();
            var repainter = StaticController.getScheduler();
            repainter.queueViewerRepaint();
        }
    }

    @Override
    public String getName() {
        return "Remove Weapon Group";
    }

}

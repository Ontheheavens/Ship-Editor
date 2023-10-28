package oth.shipeditor.undo.edits.features;

import lombok.RequiredArgsConstructor;
import oth.shipeditor.components.instrument.ship.EditorInstrument;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.components.viewer.painters.points.ship.features.InstalledFeature;
import oth.shipeditor.undo.AbstractEdit;
import oth.shipeditor.utility.overseers.StaticController;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 01.10.2023
 */
@RequiredArgsConstructor
public class WeaponGroupsSortEdit extends AbstractEdit {

    private final InstalledFeature feature;

    private final FittedWeaponGroup targetGroup;

    private final FittedWeaponGroup oldParentGroup;

    private final int targetIndex;

    private final int oldIndex;

    private int cachedOldGroupIndex;

    private int cachedNewGroupIndex;

    private boolean removeEmptyGroups;


    @Override
    public void undo() {
        this.transferFeature(targetGroup, oldParentGroup, oldIndex,
                integer -> cachedNewGroupIndex = integer,
                () -> cachedOldGroupIndex);
    }

    @Override
    public void redo() {
        this.transferFeature(oldParentGroup, targetGroup, targetIndex,
                integer -> cachedOldGroupIndex = integer,
                () -> cachedNewGroupIndex);
    }

    private void transferFeature(FittedWeaponGroup supplier, FittedWeaponGroup recipient, int index,
                                 Consumer<Integer> cachedIndexSetter, Supplier<Integer> cachedGroupIndex) {
        var oldParentWeapons = supplier.getWeapons();
        oldParentWeapons.remove(feature.getSlotID());

        if (oldParentWeapons.isEmpty() && removeEmptyGroups) {
            var variant = supplier.getParent();
            List<FittedWeaponGroup> groupList = variant.getWeaponGroups();
            cachedIndexSetter.accept(groupList.indexOf(supplier));
            groupList.remove(supplier);
        }

        feature.setParentGroup(recipient);
        var weapons = recipient.getWeapons();
        if (index != -1) {
            weapons.put(index, feature.getSlotID(), feature);

            var variant = recipient.getParent();
            List<FittedWeaponGroup> groupList = variant.getWeaponGroups();
            if (!groupList.contains(recipient)) {
                groupList.add(cachedGroupIndex.get(), recipient);
            }
        }

        if (StaticController.getEditorMode() == EditorInstrument.VARIANT_WEAPONS) {
            StaticController.reselectCurrentLayer();
            var repainter = StaticController.getScheduler();
            repainter.queueViewerRepaint();
        }
    }

    @Override
    public String getName() {
        return "Sort Weapon Groups";
    }

}

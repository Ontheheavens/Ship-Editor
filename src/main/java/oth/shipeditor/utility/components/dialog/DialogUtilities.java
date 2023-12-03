package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.components.viewer.painters.points.ship.features.FittedWeaponGroup;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public final class DialogUtilities {

    private DialogUtilities() {
    }

    public static void showAdjustPointDialog(WorldPoint point) {
        PointChangeDialog dialog = new PointChangeDialog(point.getPosition());
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Point Position", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            Point2D newPosition = dialog.getUpdatedPosition();
            EditDispatch.postPointDragged(point, newPosition);
        }
    }

    public static void showWeaponGroupsDialog(ShipVariant variant) {
        WeaponGroupTableDialog dialog = new WeaponGroupTableDialog(variant);
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Rearrange Weapon Groups", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            List<FittedWeaponGroup> updatedGroups = dialog.getUpdatedGroups();

            var oldGroups = variant.getWeaponGroups();
            updatedGroups.forEach(weaponGroup -> {
                int index = updatedGroups.indexOf(weaponGroup);
                if (index < oldGroups.size() - 1) {
                    var group = oldGroups.get(index);

                    if (group != null) {
                        weaponGroup.setAutofire(group.isAutofire());
                        weaponGroup.setMode(group.getMode());
                    }
                }
            });

            variant.setWeaponGroups(updatedGroups);

            var repainter = StaticController.getScheduler();
            repainter.queueVariantWeaponsRepaint();
        }
    }

}

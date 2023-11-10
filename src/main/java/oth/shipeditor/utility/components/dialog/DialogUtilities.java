package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.undo.EditDispatch;

import javax.swing.*;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 27.07.2023
 */
public final class DialogUtilities {

    private DialogUtilities() {
    }

    // TODO: Delete this stuff later.

    public static void showAdjustPointDialog(WorldPoint point) {
        PointChangeDialog dialog = new PointChangeDialog(point.getPosition());
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Point Position", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            Point2D newPosition = dialog.getUpdatedPosition();
            EditDispatch.postPointDragged(point, newPosition);
            Events.repaintShipView();
        }
    }

}

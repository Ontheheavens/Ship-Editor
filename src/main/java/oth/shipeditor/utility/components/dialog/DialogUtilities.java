package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.communication.events.Events;
import oth.shipeditor.components.viewer.entities.ShieldCenterPoint;
import oth.shipeditor.components.viewer.entities.ShipCenterPoint;
import oth.shipeditor.components.viewer.entities.WorldPoint;
import oth.shipeditor.components.viewer.layers.LayerPainter;
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

    public static void showAdjustLayerAnchorDialog(LayerPainter layerPainter, Point2D oldAnchor) {
        PointChangeDialog dialog = new PointChangeDialog(oldAnchor);
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Anchor Position", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            Point2D newPosition = dialog.getUpdatedPosition();
            EditDispatch.postAnchorOffsetChanged(layerPainter, newPosition);
            Events.repaintShipView();
        }
    }

    public static void showAdjustShieldRadiusDialog(ShieldCenterPoint point) {
        NumberChangeDialog dialog = new RadiusChangeDialog(point.getShieldRadius());
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Shield Radius", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            double newRadius = dialog.getUpdatedValue();
            EditDispatch.postShieldRadiusChanged(point, (float) newRadius);
            Events.repaintShipView();
        }
    }

    public static void showAdjustCollisionDialog(ShipCenterPoint point) {
        NumberChangeDialog dialog = new RadiusChangeDialog(point.getCollisionRadius());
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Collision Radius", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            double newRadius = dialog.getUpdatedValue();
            EditDispatch.postCollisionRadiusChanged(point, (float) newRadius);
            Events.repaintShipView();
        }
    }

    public static void showAdjustLayerRotationDialog(LayerPainter layerPainter, double oldRotation) {
        AngleChangeDialog dialog = new AngleChangeDialog(oldRotation);
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Layer Rotation", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            double newRotation = dialog.getUpdatedValue();
            double reversed = (360 - newRotation) % 360;
            layerPainter.rotateLayer(reversed);
            Events.repaintShipView();
        }
    }

}

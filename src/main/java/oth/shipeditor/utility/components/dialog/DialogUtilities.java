package oth.shipeditor.utility.components.dialog;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.Events;
import oth.shipeditor.communication.events.viewer.control.ViewerRotationSet;
import oth.shipeditor.communication.events.viewer.control.ViewerZoomSet;
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
            Events.repaintView();
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
            Events.repaintView();
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
            Events.repaintView();
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
            Events.repaintView();
        }
    }

    public static void showAdjustZoomDialog(double oldZoom) {
        NumberChangeDialog dialog = new NumberChangeDialog(oldZoom) {
            @Override
            protected JLabel createOriginalLabel() {
                int value = (int) getRounded();
                return new JLabel(value + "% ");
            }
            private double getRounded() {
                return Math.round(getOriginalNumber() * 100);
            }
            @Override
            protected SpinnerNumberModel createSpinnerModel() {
                double min = 20.0d;
                double max = 120000.0d;
                double step = 1.0d;
                return new SpinnerNumberModel(getRounded(), min, max, step);
            }
        };
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Zoom Level", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            double newZoom = dialog.getUpdatedValue() * 0.01;
            EventBus.publish(new ViewerZoomSet(newZoom));
            Events.repaintView();
        }
    }

    public static void showAdjustViewerRotationDialog(double oldRotation) {
        AngleChangeDialog dialog = new AngleChangeDialog(oldRotation);
        int option = JOptionPane.showConfirmDialog(null, dialog,
                "Change Viewer Rotation", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            double newRotation = dialog.getUpdatedValue();
            EventBus.publish(new ViewerRotationSet(newRotation));
            Events.repaintView();
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
            Events.repaintView();
        }
    }

}

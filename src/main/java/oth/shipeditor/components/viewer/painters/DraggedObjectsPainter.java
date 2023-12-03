package oth.shipeditor.components.viewer.painters;

import de.javagl.viewer.Painter;
import oth.shipeditor.components.datafiles.entities.CSVEntry;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.ViewerDropReceiver;
import oth.shipeditor.components.viewer.entities.weapon.WeaponSlotPoint;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.representation.weapon.WeaponMount;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * @author Ontheheavens
 * @since 02.12.2023
 */
public class DraggedObjectsPainter implements Painter {

    private final TextPainter draggedEntityText = new TextPainter();

    @SuppressWarnings("ChainOfInstanceofChecks")
    @Override
    public void paint(Graphics2D g, AffineTransform worldToScreen, double w, double h) {
        InstallableEntry dragged = ViewerDropReceiver.getDraggedEntry();
        Point2D currentCursor = StaticController.getCorrectedWithoutRotate();

        double rotation = 0;
        WeaponMount mount = WeaponMount.TURRET;
        WeaponSlotPoint selectedWeaponSlot = StaticController.getSelectedAndEligibleSlot();
        if (selectedWeaponSlot != null) {
            rotation = selectedWeaponSlot.getAngle();
            ShipPainter weaponSlotParent = selectedWeaponSlot.getParent();
            double rotationRadians = weaponSlotParent.getRotationRadians();
            rotation -= Math.toDegrees(rotationRadians);

            rotation = Utility.flipAngle(rotation);
            mount = selectedWeaponSlot.getWeaponMount();
        }

        EditorInstrument editorMode = StaticController.getEditorMode();

        boolean doesFit = false;
        draggedEntityText.setWorldPosition(currentCursor);

        Font font = Utility.getOrbitron(12);

        if (dragged instanceof ShipCSVEntry shipEntry) {
            double conditionalAngle = rotation;

            boolean isModuleMode = editorMode == EditorInstrument.VARIANT_MODULES;
            if (!isModuleMode) {
                conditionalAngle = 0;
            }
            shipEntry.paintEntry(g, worldToScreen, conditionalAngle, currentCursor);

            paintShipEntryHints(g, worldToScreen, shipEntry, isModuleMode, selectedWeaponSlot, font);

        } else if (dragged instanceof WeaponCSVEntry weaponEntry) {
            boolean isWeaponsMode = editorMode == EditorInstrument.BUILT_IN_WEAPONS
                    || editorMode == EditorInstrument.VARIANT_WEAPONS;
            if (!isWeaponsMode) {
                rotation = 0;
            }

            weaponEntry.paintEntry(g, worldToScreen,
                    rotation, currentCursor, mount);

            if (isWeaponsMode) {
                paintSlotStatus(g, worldToScreen, selectedWeaponSlot, font, weaponEntry);
            } else {
                draggedEntityText.setText("Not in install mode");
                draggedEntityText.paintText(g, worldToScreen, font, Color.GRAY);
            }
        }
    }

    private void paintShipEntryHints(Graphics2D g, AffineTransform worldToScreen, CSVEntry shipEntry,
                                     boolean isModuleMode, WeaponSlotPoint selectedWeaponSlot, Font font) {
        if (isModuleMode && StaticController.isShipLayerActive()) {
            paintSlotStatus(g, worldToScreen, selectedWeaponSlot, font, shipEntry);
        } else {
            DataFlavor draggedFlavor = ViewerDropReceiver.getCurrentFlavor();
            if (draggedFlavor != null) {
                if (draggedFlavor == TransferableEntry.TRANSFERABLE_SHIP) {
                    draggedEntityText.setText("Hull to layer");
                    draggedEntityText.paintText(g, worldToScreen, font, Color.GREEN);
                }
                else if (draggedFlavor == TransferableEntry.TRANSFERABLE_VARIANT) {
                    draggedEntityText.setText("Variant to layer");
                    draggedEntityText.paintText(g, worldToScreen, font, Color.GREEN);
                }
            }
        }
    }

    private void paintSlotStatus(Graphics2D g, AffineTransform worldToScreen,
                                 WeaponSlotPoint selectedWeaponSlot, Font font,
                                 CSVEntry entry) {
        if (selectedWeaponSlot == null) {
            draggedEntityText.setText(StringValues.SLOT_NOT_SELECTED);
            draggedEntityText.paintText(g, worldToScreen, font, Color.GRAY);
        } else if (!selectedWeaponSlot.canFit(entry)) {
            draggedEntityText.setText(StringValues.UNFIT_FOR_SLOT);
            draggedEntityText.paintText(g, worldToScreen, font, Color.RED);
        } else {
            draggedEntityText.setText(StringValues.CAN_INSTALL);
            draggedEntityText.paintText(g, worldToScreen, font, Color.GREEN);
        }
    }

}

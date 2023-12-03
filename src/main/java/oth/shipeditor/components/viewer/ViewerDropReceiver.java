package oth.shipeditor.components.viewer;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.InstallableEntry;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.FeaturesOverseer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.painters.points.ship.WeaponSlotPainter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.VariantFile;
import oth.shipeditor.representation.weapon.WeaponType;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.Errors;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
@Log4j2
public class ViewerDropReceiver extends DropTarget {

    private final PrimaryViewer viewer;

    @Getter
    private static InstallableEntry draggedEntry;

    @Getter
    private static DataFlavor currentFlavor;

    @Getter
    private static boolean dragToViewerInProgress;

    ViewerDropReceiver(PrimaryViewer parent) {
        this.viewer = parent;
    }

    @Override
    public synchronized void dragEnter(DropTargetDragEvent dtde) {
        super.dragEnter(dtde);
        ViewerControl controls = viewer.getViewerControls();
        controls.notifyCursorState(dtde.getLocation());
        viewer.setCursorInViewer(true);
    }

    @Override
    public synchronized void dragOver(DropTargetDragEvent dtde) {
        super.dragOver(dtde);
        ViewerControl controls = viewer.getViewerControls();
        controls.notifyCursorState(dtde.getLocation());
    }

    @Override
    public synchronized void dragExit(DropTargetEvent dte) {
        super.dragExit(dte);
        viewer.setCursorInViewer(false);
    }

    @SuppressWarnings("SynchronizedMethod")
    public static synchronized void commenceDragToViewer(InstallableEntry dragged, DataFlavor flavor) {
        dragToViewerInProgress = true;
        draggedEntry = dragged;
        currentFlavor = flavor;

        PrimaryViewer viewer = StaticController.getViewer();
        viewer.setCursorInViewer(false);
    }

    @SuppressWarnings("SynchronizedMethod")
    public static synchronized void finishDragToViewer() {
        dragToViewerInProgress = false;
        draggedEntry = null;
        currentFlavor = null;
    }

    @SuppressWarnings({"unchecked", "AccessToStaticFieldLockedOnInstance", "IfStatementWithTooManyBranches"})
    public synchronized void drop(DropTargetDropEvent dtde) {
        try {
            Transferable transferable = dtde.getTransferable();
            DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
            List<DataFlavor> flavorList = Arrays.asList(transferDataFlavors);

            DataFlavor filesFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor shipFlavor = TransferableEntry.TRANSFERABLE_SHIP;
            DataFlavor weaponFlavor = TransferableEntry.TRANSFERABLE_WEAPON;
            DataFlavor variantFlavor = TransferableEntry.TRANSFERABLE_VARIANT;

            if (flavorList.contains(filesFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                Iterable<File> droppedFiles = (Iterable<File>) transferable.getTransferData(filesFlavor);
                ViewerDropReceiver.handleExternalFilesDrop(dtde, droppedFiles);
            } else if (flavorList.contains(shipFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                ShipCSVEntry shipEntry = (ShipCSVEntry) transferable.getTransferData(shipFlavor);
                ViewerDropReceiver.handleShipEntryDrop(dtde, shipEntry);
            } else if (flavorList.contains(variantFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                VariantFile variantFile = (VariantFile) transferable.getTransferData(variantFlavor);
                ViewerDropReceiver.handleVariantFileDrop(dtde, variantFile);
            } else if (flavorList.contains(weaponFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                WeaponCSVEntry weaponEntry = (WeaponCSVEntry) transferable.getTransferData(weaponFlavor);
                ViewerDropReceiver.handleWeaponEntryDrop(dtde, weaponEntry);
            }
        } catch (Exception ex) {
            dtde.dropComplete(false);
            ViewerDropReceiver.finishDragToViewer();
            log.error("Drag-and-drop to viewer failed!");
            JOptionPane.showMessageDialog(null,
                    "Failed to conclude drag-and-drop action for viewer, exception thrown at: " + dtde,
                    "Drag-and-drop operation error!",
                    JOptionPane.ERROR_MESSAGE);
            Errors.printToStream(ex);
        }
        ViewerDropReceiver.finishDragToViewer();
    }

    private static void handleExternalFilesDrop(DropTargetDropEvent dtde, Iterable<File> files) {
        File firstEligible = null;
        for (File file : files) {
            boolean correctExtension = file.getName().toLowerCase(Locale.ROOT).endsWith(".png");
            boolean correctLocation = FileUtilities.isFileWithinGamePackages(file);
            if (correctExtension && correctLocation) {
                firstEligible = file;
                break;
            }
        }
        if (firstEligible == null) {
            log.error("Drag-and-drop sprite loading unsuccessful: requires PNG file located in game packages.");
            JOptionPane.showMessageDialog(null,
                    "Failed to load file as sprite or initialize layer with it:" +
                            " requires PNG file located in game packages.",
                    "Drag-and-drop layer initialization unsuccessful!",
                    JOptionPane.INFORMATION_MESSAGE);
            dtde.dropComplete(false);
            ViewerDropReceiver.finishDragToViewer();
            return;
        }
        FileUtilities.createShipLayerWithSprite(firstEligible);
        dtde.dropComplete(true);
        ViewerDropReceiver.finishDragToViewer();
    }

    private static boolean hasModuleSlotsInActiveLayer() {
        boolean hasModuleSlots = false;
        if (StaticController.getActiveLayer() instanceof ShipLayer targetLayer) {
            ShipPainter shipPainter = targetLayer.getPainter();
            if (shipPainter == null || shipPainter.isUninitialized()) return false;
            WeaponSlotPainter weaponSlotPainter = shipPainter.getWeaponSlotPainter();
            hasModuleSlots = weaponSlotPainter.hasSlotsOfType(WeaponType.STATION_MODULE);
        }
        return hasModuleSlots;
    }

    private static void handleShipEntryDrop(DropTargetDropEvent dtde, ShipCSVEntry shipEntry) {
        try {
            boolean hasSlots = ViewerDropReceiver.hasModuleSlotsInActiveLayer();
            boolean isModulesMode = StaticController.getEditorMode() == EditorInstrument.VARIANT_MODULES;

            if (isModulesMode && hasSlots) {
                VariantFile forInstall = FeaturesOverseer.moduleVariantForInstall;
                ViewerDropReceiver.addAsModule(dtde, forInstall);
            } else {
                ShipLayer shipLayer = shipEntry.loadLayerFromEntry();
                ViewerDropReceiver.dropShipLayer(dtde, shipLayer);
            }
        } catch (Exception exception) {
            Errors.printToStream(exception);
            dtde.dropComplete(false);
        }
        ViewerDropReceiver.finishDragToViewer();
    }

    private static void handleVariantFileDrop(DropTargetDropEvent dtde, VariantFile variantFile) {
        try {
            boolean hasSlots = ViewerDropReceiver.hasModuleSlotsInActiveLayer();
            boolean isModuleMode = StaticController.getEditorMode() == EditorInstrument.VARIANT_MODULES;
            if (isModuleMode && hasSlots) {
                ViewerDropReceiver.addAsModule(dtde, variantFile);
            } else {
                ShipLayer shipLayer = GameDataRepository.createLayerFromVariant(variantFile);
                ViewerDropReceiver.dropShipLayer(dtde, shipLayer);
            }
        } catch (Exception exception) {
            Errors.printToStream(exception);
            dtde.dropComplete(false);
        }
        ViewerDropReceiver.finishDragToViewer();
    }

    private static void dropShipLayer(DropTargetDropEvent dtde,
                                      ShipLayer shipLayer) {
        ShipPainter shipPainter = shipLayer.getPainter();
        Point2D difference = shipPainter.getSpriteCenterDifferenceToAnchor();

        Point2D currentCursor = StaticController.getCorrectedWithoutRotate();
        Point2D targetForSpriteCenter = new Point2D.Double(currentCursor.getX() - difference.getX(),
                currentCursor.getY() - difference.getY());

        shipPainter.updateAnchorOffset(targetForSpriteCenter);
        UndoOverseer.finishAllEdits();
        StaticController.reselectCurrentLayer();

        dtde.dropComplete(true);
        ViewerDropReceiver.finishDragToViewer();
    }

    private static void addAsModule(DropTargetDropEvent dtde, VariantFile variantFile) {
        try {
            ViewerLayer viewerLayer = StaticController.getActiveLayer();
            if (viewerLayer instanceof ShipLayer shipLayer) {
                FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
                if (featuresOverseer != null) {
                    featuresOverseer.addModuleToSelectedSlot(variantFile);
                    dtde.dropComplete(true);
                } else {
                    dtde.dropComplete(false);
                }
            } else {
                dtde.dropComplete(false);
            }
        } catch (Exception exception) {
            Errors.printToStream(exception);
            dtde.dropComplete(false);
        }
        ViewerDropReceiver.finishDragToViewer();
    }

    private static void handleWeaponEntryDrop(DropTargetDropEvent dtde, WeaponCSVEntry weaponEntry) {
        try {
            ViewerLayer viewerLayer = StaticController.getActiveLayer();
            if (viewerLayer instanceof ShipLayer shipLayer) {
                FeaturesOverseer featuresOverseer = shipLayer.getFeaturesOverseer();
                if (featuresOverseer != null) {
                    featuresOverseer.addWeaponToSelectedSlot(weaponEntry);
                    dtde.dropComplete(true);
                } else {
                    dtde.dropComplete(false);
                }
            } else {
                dtde.dropComplete(false);
            }
        } catch (Exception exception) {
            Errors.printToStream(exception);
            dtde.dropComplete(false);
        }
        ViewerDropReceiver.finishDragToViewer();
    }

}

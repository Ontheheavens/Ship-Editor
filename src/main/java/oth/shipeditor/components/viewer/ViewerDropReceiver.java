package oth.shipeditor.components.viewer;

import lombok.extern.log4j.Log4j2;
import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableEntry;
import oth.shipeditor.components.viewer.control.ControlPredicates;
import oth.shipeditor.components.viewer.control.ViewerControl;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.parsing.FileUtilities;
import oth.shipeditor.undo.UndoOverseer;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
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
class ViewerDropReceiver extends DropTarget {

    private final PrimaryViewer viewer;

    ViewerDropReceiver(PrimaryViewer parent) {
        this.viewer = parent;
    }

    @Override
    public synchronized void dragEnter(DropTargetDragEvent dtde) {
        super.dragEnter(dtde);
        ViewerControl controls = viewer.getViewerControls();
        controls.notifyCursorState(dtde.getLocation());
    }

    @Override
    public synchronized void dragOver(DropTargetDragEvent dtde) {
        super.dragOver(dtde);
        ViewerControl controls = viewer.getViewerControls();
        controls.notifyCursorState(dtde.getLocation());
    }

    @SuppressWarnings({"unchecked", "AccessToStaticFieldLockedOnInstance", "CallToPrintStackTrace"})
    public synchronized void drop(DropTargetDropEvent dtde) {
        try {
            Transferable transferable = dtde.getTransferable();
            DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
            List<DataFlavor> flavorList = Arrays.asList(transferDataFlavors);

            DataFlavor filesFlavor = DataFlavor.javaFileListFlavor;
            DataFlavor shipFlavor = TransferableEntry.TRANSFERABLE_SHIP;

            if (flavorList.contains(filesFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                Iterable<File> droppedFiles = (Iterable<File>) transferable.getTransferData(filesFlavor);
                ViewerDropReceiver.handleExternalFilesDrop(dtde, droppedFiles);
            } else if (flavorList.contains(shipFlavor)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                ShipCSVEntry shipEntry = (ShipCSVEntry) transferable.getTransferData(shipFlavor);
                ViewerDropReceiver.handleShipEntryDrop(dtde, shipEntry);
            }
        } catch (Exception ex) {
            dtde.dropComplete(false);
            ControlPredicates.setDragToViewerInProgress(false);
            log.error("Drag-and-drop to viewer failed!");
            JOptionPane.showMessageDialog(null,
                    "Failed to conclude drag-and-drop action for viewer, exception thrown at: " + dtde,
                    "Drag-and-drop operation error!",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
            ControlPredicates.setDragToViewerInProgress(false);
            return;
        }
        FileUtilities.createShipLayerWithSprite(firstEligible);
        dtde.dropComplete(true);
        ControlPredicates.setDragToViewerInProgress(false);
    }

    private static void handleShipEntryDrop(DropTargetDropEvent dtde, ShipCSVEntry shipEntry) {
        try {
            ShipLayer shipLayer = shipEntry.loadLayerFromEntry();
            ShipPainter shipPainter = shipLayer.getPainter();
            Point2D difference = shipPainter.getSpriteCenterDifferenceToAnchor();

            Point2D currentCursor = StaticController.getCorrectedCursor();
            Point2D targetForSpriteCenter = new Point2D.Double(currentCursor.getX() - difference.getX(),
                    currentCursor.getY() - difference.getY());

            shipPainter.updateAnchorOffset(targetForSpriteCenter);
            UndoOverseer.finishAllEdits();

            dtde.dropComplete(true);
        } catch (Exception exception) {
            dtde.dropComplete(false);
        } finally {
            ControlPredicates.setDragToViewerInProgress(false);
        }
    }

}

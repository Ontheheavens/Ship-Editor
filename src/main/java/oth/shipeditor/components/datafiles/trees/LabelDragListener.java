package oth.shipeditor.components.datafiles.trees;

import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableVariant;
import oth.shipeditor.components.viewer.ViewerDragListener;
import oth.shipeditor.components.viewer.ViewerDropReceiver;
import oth.shipeditor.representation.GameDataRepository;
import oth.shipeditor.representation.ship.VariantFile;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

/**
 * @author Ontheheavens
 * @since 06.11.2023
 */
class LabelDragListener implements DragGestureListener {

    private final VariantFile variant;

    private final Object parentSource;

    LabelDragListener(VariantFile variantFile, Object source) {
        this.variant = variantFile;
        this.parentSource = source;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        Transferable transferable = new TransferableVariant(variant, parentSource);

        String baseHullID = GameDataRepository.getBaseHullID(variant.getShipHullId());
        ShipCSVEntry shipEntry = GameDataRepository.retrieveShipCSVEntryByID(baseHullID);

        ViewerDropReceiver.commenceDragToViewer(shipEntry);
        dge.startDrag(DragSource.DefaultMoveDrop, transferable, new ViewerDragListener());
    }

}

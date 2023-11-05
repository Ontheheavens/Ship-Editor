package oth.shipeditor.components.datafiles.entities.transferable;

import oth.shipeditor.components.datafiles.entities.WingCSVEntry;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public class TransferableWing extends TransferableEntry {

    @SuppressWarnings("TypeMayBeWeakened")
    public TransferableWing(WingCSVEntry data) {
        super(data);
    }

    @Override
    protected DataFlavor getTypeFlavor() {
        return TRANSFERABLE_WING;
    }

    @Override
    public WingCSVEntry getNodeData() {
        return (WingCSVEntry) super.getNodeData();
    }


}

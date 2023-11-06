package oth.shipeditor.components.datafiles.entities.transferable;

import oth.shipeditor.components.datafiles.entities.ShipCSVEntry;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public class TransferableShip extends TransferableEntry {

    public TransferableShip(ShipCSVEntry data) {
        super(data);
    }

    @Override
    protected DataFlavor getTypeFlavor() {
        return TRANSFERABLE_SHIP;
    }

    @Override
    public ShipCSVEntry getNodeData() {
        return (ShipCSVEntry) super.getNodeData();
    }

}

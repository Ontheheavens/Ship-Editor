package oth.shipeditor.components.datafiles.entities.transferable;

import lombok.Getter;
import oth.shipeditor.components.datafiles.entities.*;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */

@Getter
public abstract class TransferableEntry implements Transferable {

    private final CSVEntry nodeData;

    public static final DataFlavor TRANSFERABLE_SHIP = new DataFlavor(ShipCSVEntry.class,
            "Ship Entry");

    public static final DataFlavor TRANSFERABLE_WEAPON = new DataFlavor(WeaponCSVEntry.class,
            "Weapon Entry");

    public static final DataFlavor TRANSFERABLE_MOD = new DataFlavor(HullmodCSVEntry.class,
            "Hullmod Entry");

    public static final DataFlavor TRANSFERABLE_WING = new DataFlavor(WingCSVEntry.class,
            "Wing Entry");

    TransferableEntry(CSVEntry data) {
        this.nodeData = data;
    }

    protected abstract DataFlavor getTypeFlavor();

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{getTypeFlavor()};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(getTypeFlavor());
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
                return nodeData;
        }
        throw new UnsupportedFlavorException(flavor);
    }

}

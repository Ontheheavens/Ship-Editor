package oth.shipeditor.components.datafiles.entities.transferable;

import lombok.Getter;
import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public class TransferableHullmod extends TransferableEntry {

    @Getter
    private final Object dragSource;

    public TransferableHullmod(HullmodCSVEntry data, Object source) {
        super(data);
        this.dragSource = source;
    }

    @Override
    protected DataFlavor getTypeFlavor() {
        return TRANSFERABLE_MOD;
    }

    @Override
    public HullmodCSVEntry getNodeData() {
        return (HullmodCSVEntry) super.getNodeData();
    }

}

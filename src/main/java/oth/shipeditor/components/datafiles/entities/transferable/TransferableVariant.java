package oth.shipeditor.components.datafiles.entities.transferable;

import oth.shipeditor.representation.ship.VariantFile;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Ontheheavens
 * @since 06.11.2023
 */
public class TransferableVariant extends TransferableEntry{

    public TransferableVariant(VariantFile variant) {
        super(variant);
    }

    @Override
    protected DataFlavor getTypeFlavor() {
        return TRANSFERABLE_VARIANT;
    }

    @Override
    public VariantFile getNodeData() {
        return (VariantFile) super.getNodeData();
    }

}

package oth.shipeditor.components.datafiles.entities.transferable;

import oth.shipeditor.components.datafiles.entities.WeaponCSVEntry;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Ontheheavens
 * @since 05.11.2023
 */
public class TransferableWeapon extends TransferableEntry {

    @SuppressWarnings("TypeMayBeWeakened")
    public TransferableWeapon(WeaponCSVEntry data) {
        super(data);
    }

    @Override
    protected DataFlavor getTypeFlavor() {
        return TRANSFERABLE_WEAPON;
    }

    @Override
    public WeaponCSVEntry getNodeData() {
        return (WeaponCSVEntry) super.getNodeData();
    }

}

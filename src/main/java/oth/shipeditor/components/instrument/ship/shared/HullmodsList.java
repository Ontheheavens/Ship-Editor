package oth.shipeditor.components.instrument.ship.shared;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableHullmod;
import oth.shipeditor.utility.components.containers.OrdnancedEntryList;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 25.11.2023
 */
public class HullmodsList extends OrdnancedEntryList<HullmodCSVEntry> {

    private final Consumer<HullmodCSVEntry> removeAction;

    public HullmodsList(Consumer<HullmodCSVEntry> removeSetter, ListModel<HullmodCSVEntry> dataModel,
                        Consumer<List<HullmodCSVEntry>> sortSetter) {
        super(dataModel, sortSetter);
        this.removeAction = removeSetter;
    }

    @Override
    protected boolean confirmDrop(int targetIndex, HullmodCSVEntry entry) {
        DefaultListModel<HullmodCSVEntry> model = this.getModel();
        if (model.contains(entry)) {
            int former = model.indexOf(entry);
            model.remove(former);
            model.add(Math.min(model.size(), targetIndex), entry);
            setSelectedIndex(targetIndex);
        } else {
            super.confirmDrop(targetIndex, entry);
        }
        return true;
    }

    @Override
    public DefaultListModel<HullmodCSVEntry> getModel() {
        return (DefaultListModel<HullmodCSVEntry>) super.getModel();
    }

    @Override
    protected Consumer<HullmodCSVEntry> getRemoveAction() {
        return removeAction;
    }

    @Override
    protected Transferable createTransferableFromEntry(HullmodCSVEntry entry) {
        return new TransferableHullmod(entry, this);
    }

    @Override
    protected boolean isSupported(Transferable transferable) {
        return transferable.getTransferDataFlavors()[0].equals(TransferableEntry.TRANSFERABLE_MOD);
    }

}

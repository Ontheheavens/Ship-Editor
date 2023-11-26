package oth.shipeditor.components.instrument.ship.shared;

import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableWing;
import oth.shipeditor.utility.components.containers.OrdnancedEntryList;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Ontheheavens
 * @since 26.11.2023
 */
public class WingsList extends OrdnancedEntryList<WingCSVEntry> {

    private final BiConsumer<Integer, WingCSVEntry> removeAction;

    public WingsList(BiConsumer<Integer, WingCSVEntry> removeSetter, ListModel<WingCSVEntry> dataModel,
                     Consumer<List<WingCSVEntry>> sortAction) {
        super(dataModel, sortAction);
        this.removeAction = removeSetter;
    }

    @Override
    protected Consumer<WingCSVEntry> getRemoveAction() {
        return wingCSVEntry -> actOnSelectedWing(removeAction);
    }

    private void actOnSelectedWing(BiConsumer<Integer, WingCSVEntry> action) {
        int index = this.getSelectedIndex();
        if (index != -1) {
            ListModel<WingCSVEntry> listModel = this.getModel();
            WingCSVEntry feature = listModel.getElementAt(index);
            action.accept(index, feature);
        }
    }

    protected JPopupMenu getContextMenu() {
        WingCSVEntry selected = getSelectedValue();
        if (selected == null) return null;

        JPopupMenu menu = new JPopupMenu();
        JMenuItem remove = new JMenuItem("Remove wing");
        remove.addActionListener(event -> actOnSelectedWing(removeAction));
        menu.add(remove);

        return menu;
    }

    @Override
    protected Transferable createTransferableFromEntry(WingCSVEntry entry) {
        return new TransferableWing(entry, this);
    }

    @Override
    protected boolean isSupported(Transferable transferable) {
        return transferable.getTransferDataFlavors()[0].equals(TransferableEntry.TRANSFERABLE_WING);
    }

}

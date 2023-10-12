package oth.shipeditor.utility.components.containers;

import oth.shipeditor.components.datafiles.entities.OrdnancedCSVEntry;

import javax.swing.*;

/**
 * @author Ontheheavens
 * @since 12.10.2023
 */
public abstract class OrdnancedEntryList<T extends OrdnancedCSVEntry> extends SortableList<T>{

    protected OrdnancedEntryList(ListModel<T> dataModel) {
        super(dataModel);
    }

    @Override
    protected void sortListModel() {

    }

}

package oth.shipeditor.utility.components.containers;

import oth.shipeditor.components.datafiles.entities.OrdnancedCSVEntry;
import oth.shipeditor.utility.components.rendering.OrdnancedEntryCellRenderer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ontheheavens
 * @since 12.10.2023
 */
public abstract class OrdnancedEntryList<T extends OrdnancedCSVEntry> extends SortableList<T>{

    protected OrdnancedEntryList(ListModel<T> dataModel) {
        super(dataModel);
        this.setCellRenderer(new OrdnancedEntryCellRenderer());
    }

    @Override
    protected void sortListModel() {
        ListModel<T> model = this.getModel();
        List<T> rearranged = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            T point = model.getElementAt(i);
            rearranged.add(point);
        }
        this.publishEntriesSorted(rearranged);
    }

    protected abstract void publishEntriesSorted(List<T> rearranged);

}

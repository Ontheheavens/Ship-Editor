package oth.shipeditor.components.instrument.ship.variant.hullmods;

import oth.shipeditor.components.datafiles.entities.HullmodCSVEntry;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.utility.components.containers.OrdnancedEntryList;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 15.10.2023
 */
class HullmodsListPanel extends JPanel{

    private final HullmodsList modsList;

    private DefaultListModel<HullmodCSVEntry> modsModel;

    private final Function<ShipVariant, List<HullmodCSVEntry>> modsGetter;

    HullmodsListPanel(Function<ShipVariant, List<HullmodCSVEntry>> getter) {
        this.modsModel = new DefaultListModel<>();
        this.modsList = new HullmodsList(modsModel);
        modsList.setBorder(new LineBorder(Color.LIGHT_GRAY));
        this.modsGetter = getter;
        this.setLayout(new BorderLayout());
        this.add(modsList, BorderLayout.CENTER);
    }

    void refreshListModel(ViewerLayer selected) {
        DefaultListModel<HullmodCSVEntry> newModel = new DefaultListModel<>();
        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.modsModel = newModel;
            this.modsList.setModel(newModel);
            this.modsList.setEnabled(false);
            return;
        }
        ShipPainter painter = checkedLayer.getPainter();
        if (painter != null && !painter.isUninitialized()) {
            ShipVariant active = painter.getActiveVariant();
            if (active != null && !active.isEmpty()) {
                List<HullmodCSVEntry> entries = modsGetter.apply(active);
                if (entries != null) {
                    newModel.addAll(entries);
                }
                this.modsList.setEnabled(true);
            } else {
                this.modsList.setEnabled(false);
            }
        } else {
            this.modsList.setEnabled(false);
        }
        this.modsModel = newModel;
        this.modsList.setModel(newModel);
    }

    private static class HullmodsList extends OrdnancedEntryList<HullmodCSVEntry> {

        HullmodsList(ListModel<HullmodCSVEntry> dataModel) {
            super(dataModel);
        }

        @Override
        protected void publishEntriesSorted(List<HullmodCSVEntry> rearranged) {

        }

    }

}

package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.OrdnancedEntryList;
import oth.shipeditor.utility.overseers.StaticController;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ontheheavens
 * @since 17.10.2023
 */
class VariantWingsPanel extends JPanel {

    private final WingsList wingsList;

    private DefaultListModel<WingCSVEntry> wingsModel;

    private final Function<ShipVariant, List<WingCSVEntry>> wingsGetter;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    VariantWingsPanel() {
        this.setLayout(new BorderLayout());

        BiConsumer<ShipVariant, List<WingCSVEntry>> sortSetter = ShipVariant::setWings;
        this.wingsGetter = ShipVariant::getWings;

        this.wingsModel = new DefaultListModel<>();
        this.wingsList = new WingsList(wingsModel, sortSetter);
//        wingsList.setBorder(new LineBorder(Color.LIGHT_GRAY));

        JScrollPane scroller = new JScrollPane(wingsList);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        this.add(scroller, BorderLayout.CENTER);
        ComponentUtilities.outfitPanelWithTitle(this, "Fitted wings");
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                refreshListModel(selected);
            }
        });
    }

    private void refreshListModel(ViewerLayer selected) {
        DefaultListModel<WingCSVEntry> newModel = new DefaultListModel<>();
        if (!(selected instanceof ShipLayer checkedLayer)) {
            this.wingsModel = newModel;
            this.wingsList.setModel(newModel);
            this.wingsList.setEnabled(false);
            return;
        }
        ShipPainter painter = checkedLayer.getPainter();
        if (painter != null && !painter.isUninitialized()) {
            ShipVariant active = painter.getActiveVariant();
            if (active != null && !active.isEmpty()) {
                List<WingCSVEntry> entries = wingsGetter.apply(active);
                if (entries != null) {
                    newModel.addAll(entries);
                }
                this.wingsList.setEnabled(true);
            } else {
                this.wingsList.setEnabled(false);
            }
        } else {
            this.wingsList.setEnabled(false);
        }
        this.wingsModel = newModel;
        this.wingsList.setModel(newModel);
    }

    private class WingsList extends OrdnancedEntryList<WingCSVEntry> {

        // TODO: finish sorting, removing (in EditDispatch) and adding (in WingsTreePanel context menu).

        private final Consumer<WingCSVEntry> removeAction = entry ->
                StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                    var entryList = wingsGetter.apply(variant);
//                    EditDispatch.postHullmodRemoved(entryList, shipLayer, entry);
                });

        WingsList(ListModel<WingCSVEntry> dataModel,
                     BiConsumer<ShipVariant, List<WingCSVEntry>> sortSetter) {
            super(dataModel, updatedList ->
                    StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                        var oldMods = wingsGetter.apply(variant);
//                        EditDispatch.postHullmodsSorted(oldMods, updatedList, shipLayer,
//                                list -> sortSetter.accept(variant, list));
                    }));
        }

        @Override
        protected Consumer<WingCSVEntry> getRemoveAction() {
            return removeAction;
        }

    }

}

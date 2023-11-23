package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableEntry;
import oth.shipeditor.components.datafiles.entities.transferable.TransferableWing;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.components.containers.OrdnancedEntryList;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 17.10.2023
 */
class VariantWingsPanel extends JPanel {

    private final WingsList wingsList;

    private DefaultListModel<WingCSVEntry> wingsModel;

    private final Function<ShipVariant, List<WingCSVEntry>> wingsGetter;

    private JLabel shipOPCap;

    private JLabel usedOPTotal;

    private JLabel usedOPInWings;


    private JLabel totalBayCount;

    private JLabel builtInWingsCount;

    private JLabel fittedWingsCount;

    VariantWingsPanel() {
        this.setLayout(new BorderLayout());

        BiConsumer<ShipVariant, List<WingCSVEntry>> sortSetter = ShipVariant::setWings;
        this.wingsGetter = ShipVariant::getWings;

        this.wingsModel = new DefaultListModel<>();
        this.wingsList = new WingsList(wingsModel, sortSetter);

        JScrollPane scroller = new JScrollPane(wingsList);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        JPanel infoPanel = createInfoPanel();
        this.add(infoPanel, BorderLayout.PAGE_START);
        this.add(scroller, BorderLayout.CENTER);

        this.initLayerListeners();
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        ComponentUtilities.outfitPanelWithTitle(infoPanel, "Fitted wings");
        infoPanel.setLayout(new GridBagLayout());

        JLabel shipOPCapLabel = new JLabel(StringValues.TOTAL_OP_CAPACITY);
        shipOPCap = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, shipOPCapLabel, shipOPCap, 0);

        JLabel usedOPTotalLabel = new JLabel("Used OP for ship:");
        usedOPTotal = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPTotalLabel, usedOPTotal, 1);

        JLabel usedOPLabel = new JLabel(StringValues.USED_OP_IN_WINGS);
        usedOPInWings = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPLabel, usedOPInWings, 2);

        // Empty placeholder to create some padding - is a quick hack, consider for proper rewrite later!
        ComponentUtilities.addLabelAndComponent(infoPanel, new JLabel(), new JLabel(), 3);

        JLabel totalBaysLabel = new JLabel("Total ship bays:");
        totalBayCount = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, totalBaysLabel, totalBayCount, 4);

        JLabel totalBuiltInsLabel = new JLabel("Total built-in wings:");
        builtInWingsCount = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, totalBuiltInsLabel, builtInWingsCount, 5);

        JLabel totalFittedLabel = new JLabel("Total fitted wings:");
        fittedWingsCount = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, totalFittedLabel, fittedWingsCount, 6);

        return infoPanel;
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                refreshListModel(selected);
                refreshLayerInfo(selected);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof VariantPanelRepaintQueued) {
                this.refreshLayerInfo(StaticController.getActiveLayer());
            }
        });
    }

    private static String translateIntegerValue(Supplier<Integer> getter) {
        String notInitialized = StringValues.NOT_INITIALIZED;
        int value = getter.get();
        String textResult;
        if (value == -1) {
            textResult = notInitialized;
        } else {
            textResult = String.valueOf(value);
        }
        return textResult;
    }

    private void refreshLayerInfo(ViewerLayer selected) {
        String notInitialized = StringValues.NOT_INITIALIZED;

        if (selected instanceof ShipLayer shipLayer) {

            String totalOP = VariantWingsPanel.translateIntegerValue(shipLayer::getTotalOP);
            shipOPCap.setText(totalOP);
            String totalBays = VariantWingsPanel.translateIntegerValue(shipLayer::getBayCount);
            totalBayCount.setText(totalBays);
            String totalBuiltIns =  VariantWingsPanel.translateIntegerValue(shipLayer::getBuiltInWingsCount);
            builtInWingsCount.setText(totalBuiltIns);

            var activeVariant = shipLayer.getActiveVariant();
            if (activeVariant == null) {
                usedOPTotal.setText(notInitialized);
                usedOPInWings.setText(notInitialized);
                fittedWingsCount.setText(notInitialized);
                return;
            }

            int totalUsedOP = shipLayer.getTotalUsedOP();
            usedOPTotal.setText(String.valueOf(totalUsedOP));

            int totalOPInWings = shipLayer.getTotalOPInWings();
            usedOPInWings.setText(String.valueOf(totalOPInWings));

            int wingsCount = activeVariant.getFittedWingsCount();
            fittedWingsCount.setText(String.valueOf(wingsCount));
        } else {
            shipOPCap.setText(notInitialized);
            usedOPTotal.setText(notInitialized);
            usedOPInWings.setText(notInitialized);
        }
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

        private final BiConsumer<Integer, WingCSVEntry> removeAction = (entryIndex, wingCSVEntry) ->
                StaticController.actOnCurrentVariant((shipLayer, variant) -> {
            var entryList = wingsGetter.apply(variant);
            EditDispatch.postWingRemoved(entryList, shipLayer, wingCSVEntry, entryIndex);
        });

        WingsList(ListModel<WingCSVEntry> dataModel,
                     BiConsumer<ShipVariant, List<WingCSVEntry>> sortSetter) {
            super(dataModel, updatedList ->
                    StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                        var oldWings = wingsGetter.apply(variant);
                        EditDispatch.postWingsSorted(oldWings, updatedList, shipLayer,
                                list -> sortSetter.accept(variant, list));
                    }));
        }

        BiConsumer<Integer, WingCSVEntry> getWingRemoveAction() {
            return removeAction;
        }

        @Override
        protected Consumer<WingCSVEntry> getRemoveAction() {
            return null;
        }

        void actOnSelectedWing(BiConsumer<Integer, WingCSVEntry> action) {
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
            remove.addActionListener(event -> actOnSelectedWing(getWingRemoveAction()));
            menu.add(remove);

            return menu;
        }

        @Override
        protected Transferable createTransferableFromEntry(WingCSVEntry entry) {
            return new TransferableWing(entry);
        }

        @Override
        protected boolean isSupported(Transferable transferable) {
            return transferable.getTransferDataFlavors()[0].equals(TransferableEntry.TRANSFERABLE_WING);
        }

    }

}

package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.datafiles.entities.WingCSVEntry;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.instrument.ship.shared.WingsList;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipPainter;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.undo.EditDispatch;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

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

        BiConsumer<Integer, WingCSVEntry> removeAction = (entryIndex, wingCSVEntry) ->
                StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                    var entryList = wingsGetter.apply(variant);
                    EditDispatch.postWingRemoved(entryList, shipLayer, wingCSVEntry, entryIndex);
                });

        Consumer<List<WingCSVEntry>> sortAction = updatedList ->
                StaticController.actOnCurrentVariant((shipLayer, variant) -> {
                    var oldWings = wingsGetter.apply(variant);
                    EditDispatch.postWingsSorted(oldWings, updatedList, shipLayer,
                            list -> sortSetter.accept(variant, list));
                });

        this.wingsList = new WingsList(removeAction, wingsModel, sortAction);

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

        JLabel totalBaysLabel = new JLabel(StringValues.TOTAL_SHIP_BAYS);
        totalBayCount = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, totalBaysLabel, totalBayCount, 4);

        JLabel totalBuiltInsLabel = new JLabel(StringValues.TOTAL_BUILT_IN_WINGS);
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
            if (event instanceof InstrumentRepaintQueued(EditorInstrument editorMode)) {
                if (editorMode == EditorInstrument.VARIANT_DATA) {
                    this.refreshLayerInfo(StaticController.getActiveLayer());
                }
            }
        });
    }

    private void refreshLayerInfo(ViewerLayer selected) {
        String notInitialized = StringValues.NOT_INITIALIZED;

        if (selected instanceof ShipLayer shipLayer) {

            String totalOP = Utility.translateIntegerValue(shipLayer::getTotalOP);
            shipOPCap.setText(totalOP);
            String totalBays = Utility.translateIntegerValue(shipLayer::getBayCount);
            totalBayCount.setText(totalBays);
            String totalBuiltIns =  Utility.translateIntegerValue(shipLayer::getBuiltInWingsCount);
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

}

package oth.shipeditor.components.instrument.ship.builtins.wings;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.InstrumentRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.instrument.EditorInstrument;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipHull;
import oth.shipeditor.components.viewer.layers.ship.data.ShipSkin;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 26.11.2023
 */
public class BuiltInWingsPanel extends JPanel {

    private final BaseWingListPane baseBuiltInWingsList;

    private final SkinWingListPane addedBySkinList;

    private final SkinWingListPane removedBySkinList;

    private JLabel totalBayCount;

    private JLabel builtInWingsCount;

    public BuiltInWingsPanel() {
        this.setLayout(new BorderLayout());

        this.baseBuiltInWingsList = new BaseWingListPane(ShipHull::getBuiltInWings, ShipHull::setBuiltInWings);
        ComponentUtilities.outfitPanelWithTitle(baseBuiltInWingsList, StringValues.BASE_BUILT_INS);
        this.addedBySkinList = new SkinWingListPane(ShipSkin::getBuiltInWings, ShipSkin::setBuiltInWings);
        ComponentUtilities.outfitPanelWithTitle(addedBySkinList, StringValues.ADDED_BY_SKIN);
        this.removedBySkinList = new SkinWingListPane(ShipSkin::getRemoveBuiltInWings, ShipSkin::setRemoveBuiltInWings);
        ComponentUtilities.outfitPanelWithTitle(removedBySkinList, StringValues.REMOVED_BY_SKIN);

        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 0.33;
        constraints.weightx = 1;
        constraints.ipady = 40;
        constraints.gridy = 0;

        container.add(baseBuiltInWingsList, constraints);
        constraints.gridy = 1;
        container.add(addedBySkinList, constraints);
        constraints.gridy = 2;
        container.add(removedBySkinList, constraints);

        JScrollPane scroller = new JScrollPane(container);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        this.add(scroller, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        this.add(infoPanel, BorderLayout.PAGE_START);

        this.initLayerListeners();
    }

    private JPanel createInfoPanel() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel dragHintPanel = ComponentUtilities.createDragInfoPanel();
        container.add(dragHintPanel, BorderLayout.PAGE_START);

        JPanel infoPanel = new JPanel();
        ComponentUtilities.outfitPanelWithTitle(infoPanel, "Built-in wings");
        infoPanel.setLayout(new GridBagLayout());

        JLabel totalBaysLabel = new JLabel(StringValues.TOTAL_SHIP_BAYS);
        totalBayCount = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, totalBaysLabel, totalBayCount, 0);

        JLabel totalBuiltInsLabel = new JLabel(StringValues.TOTAL_BUILT_IN_WINGS);
        builtInWingsCount = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, totalBuiltInsLabel, builtInWingsCount, 1);

        container.add(infoPanel, BorderLayout.CENTER);

        return container;
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();

                refreshLayerInfo(selected);

                baseBuiltInWingsList.refreshListModel(selected);
                addedBySkinList.refreshListModel(selected);
                removedBySkinList.refreshListModel(selected);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof InstrumentRepaintQueued checked) {
                if (checked.editorMode() == EditorInstrument.BUILT_IN_WINGS) {
                    this.refreshLayerInfo(StaticController.getActiveLayer());
                }
            }
        });
    }

    private void refreshLayerInfo(ViewerLayer selected) {
        String notInitialized = StringValues.NOT_INITIALIZED;

        if (selected instanceof ShipLayer shipLayer) {
            String totalBays = Utility.translateIntegerValue(shipLayer::getBayCount);
            totalBayCount.setText(totalBays);
            String totalBuiltIns =  Utility.translateIntegerValue(shipLayer::getBuiltInWingsCount);
            builtInWingsCount.setText(totalBuiltIns);
        } else {
            totalBayCount.setText(notInitialized);
            builtInWingsCount.setText(notInitialized);
        }
    }

}

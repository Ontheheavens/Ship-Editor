package oth.shipeditor.components.instrument.ship.variant.hullmods;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantPanelRepaintQueued;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.utility.Utility;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.overseers.StaticController;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 14.10.2023
 */
public class VariantHullmodsPanel extends JPanel {

    private final VariantHullmodsListPane normalModsPanel;

    private final VariantHullmodsListPane permaModsPanel;

    private final VariantHullmodsListPane sModsPanel;

    private JLabel shipOPCap;

    private JLabel usedOPTotal;

    private JLabel usedOPInHullmods;

    public VariantHullmodsPanel() {
        this.setLayout(new BorderLayout());

        this.normalModsPanel = new VariantHullmodsListPane(ShipVariant::getHullMods, ShipVariant::setHullMods);
        ComponentUtilities.outfitPanelWithTitle(normalModsPanel, "Normal");
        this.permaModsPanel = new VariantHullmodsListPane(ShipVariant::getPermaMods, ShipVariant::setPermaMods);
        ComponentUtilities.outfitPanelWithTitle(permaModsPanel, "Permanent");
        this.sModsPanel = new VariantHullmodsListPane(ShipVariant::getSMods, ShipVariant::setSMods);
        ComponentUtilities.outfitPanelWithTitle(sModsPanel, "S-Mods");

        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 0.33;
        constraints.weightx = 1;
        constraints.ipady = 40;
        constraints.gridy = 0;

        container.add(normalModsPanel, constraints);
        constraints.gridy = 1;
        container.add(permaModsPanel, constraints);
        constraints.gridy = 2;
        container.add(sModsPanel, constraints);

        JScrollPane scroller = new JScrollPane(container);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        this.add(scroller, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        this.add(infoPanel, BorderLayout.PAGE_START);

        this.initLayerListeners();
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        ComponentUtilities.outfitPanelWithTitle(infoPanel, "Fitted hullmods");
        infoPanel.setLayout(new GridBagLayout());

        JLabel shipOPCapLabel = new JLabel(StringValues.TOTAL_OP_CAPACITY);
        shipOPCap = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, shipOPCapLabel, shipOPCap, 0);

        JLabel usedOPTotalLabel = new JLabel("Used OP for ship:");
        usedOPTotal = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPTotalLabel, usedOPTotal, 1);

        JLabel usedOPLabel = new JLabel(StringValues.USED_OP_IN_HULLMODS);
        usedOPInHullmods = new JLabel();

        ComponentUtilities.addLabelAndComponent(infoPanel, usedOPLabel, usedOPInHullmods, 2);

        return infoPanel;
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                normalModsPanel.refreshListModel(selected);
                permaModsPanel.refreshListModel(selected);
                sModsPanel.refreshListModel(selected);

                refreshLayerInfo(selected);
            }
        });
        EventBus.subscribe(event -> {
            if (event instanceof VariantPanelRepaintQueued) {
                this.refreshLayerInfo(StaticController.getActiveLayer());
            }
        });
    }

    private void refreshLayerInfo(ViewerLayer selected) {
        String notInitialized = StringValues.NOT_INITIALIZED;

        if (selected instanceof ShipLayer shipLayer) {
            String totalOP = Utility.translateIntegerValue(shipLayer::getTotalOP);
            shipOPCap.setText(totalOP);

            var activeVariant = shipLayer.getActiveVariant();
            if (activeVariant == null) {
                usedOPTotal.setText(notInitialized);
                usedOPInHullmods.setText(notInitialized);
                return;
            }

            int totalUsedOP = shipLayer.getTotalUsedOP();
            usedOPTotal.setText(String.valueOf(totalUsedOP));

            int totalOPInMods = activeVariant.getTotalOPInHullmods(shipLayer);
            usedOPInHullmods.setText(String.valueOf(totalOPInMods));

        } else {
            shipOPCap.setText(notInitialized);
            usedOPTotal.setText(notInitialized);
            usedOPInHullmods.setText(notInitialized);
        }
    }

}

package oth.shipeditor.components.instrument.ship.variant.hullmods;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.ShipLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.utility.components.ComponentUtilities;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * @author Ontheheavens
 * @since 14.10.2023
 */
public class VariantHullmodsPanel extends JPanel {

    private final HullmodsListPanel normalModsPanel;

    private final HullmodsListPanel permaModsPanel;

    private final HullmodsListPanel sModsPanel;

    private JLabel shipOPCap;

    private JLabel usedOPTotal;

    private JLabel usedOPInHullmods;

    public VariantHullmodsPanel() {
        this.setLayout(new BorderLayout());

        this.normalModsPanel = new HullmodsListPanel(ShipVariant::getHullMods, ShipVariant::setHullMods);
        ComponentUtilities.outfitPanelWithTitle(normalModsPanel, "Normal");
        this.permaModsPanel = new HullmodsListPanel(ShipVariant::getPermaMods, ShipVariant::setPermaMods);
        ComponentUtilities.outfitPanelWithTitle(permaModsPanel, "Permanent");
        this.sModsPanel = new HullmodsListPanel(ShipVariant::getSMods, ShipVariant::setSMods);
        ComponentUtilities.outfitPanelWithTitle(sModsPanel, "S-Mods");

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.add(normalModsPanel);
        container.add(permaModsPanel);
        container.add(sModsPanel);

        JScrollPane scroller = new JScrollPane(container);
        JScrollBar verticalScrollBar = scroller.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);

        this.add(scroller, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        this.add(infoPanel, BorderLayout.PAGE_START);

        this.initLayerListeners();
    }

    // TODO: there's significant code duplication between variant wings and hullmods panels,
    //  created due to lack of time.
    //  Is to be considered for proper refactor later!

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

        JLabel usedOPLabel = new JLabel("Used OP in hullmods:");
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
            String totalOP = VariantHullmodsPanel.translateIntegerValue(shipLayer::getTotalOP);
            shipOPCap.setText(totalOP);

            var activeVariant = shipLayer.getActiveVariant();
            if (activeVariant == null) {
                usedOPTotal.setText(notInitialized);
                usedOPInHullmods.setText(notInitialized);
                return;
            }

            int totalUsedOP = shipLayer.getTotalUsedOP();
            usedOPTotal.setText(String.valueOf(totalUsedOP));

            int totalOPInWings = activeVariant.getTotalOPInHullmods(shipLayer);
            usedOPInHullmods.setText(String.valueOf(totalOPInWings));

        } else {
            shipOPCap.setText(notInitialized);
            usedOPTotal.setText(notInitialized);
            usedOPInHullmods.setText(notInitialized);
        }
    }

}

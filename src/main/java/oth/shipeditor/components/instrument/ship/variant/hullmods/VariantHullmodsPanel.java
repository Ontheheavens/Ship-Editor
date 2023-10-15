package oth.shipeditor.components.instrument.ship.variant.hullmods;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.viewer.layers.LayerWasSelected;
import oth.shipeditor.components.viewer.layers.ViewerLayer;
import oth.shipeditor.components.viewer.layers.ship.data.ShipVariant;
import oth.shipeditor.utility.components.ComponentUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 14.10.2023
 */
public class VariantHullmodsPanel extends JPanel {

    private final HullmodsListPanel normalModsPanel;

    private final HullmodsListPanel permaModsPanel;

    private final HullmodsListPanel sModsPanel;


    public VariantHullmodsPanel() {
        this.setLayout(new BorderLayout());

        this.normalModsPanel = new HullmodsListPanel(ShipVariant::getHullMods);
        ComponentUtilities.outfitPanelWithTitle(normalModsPanel, "Normal");
        this.permaModsPanel = new HullmodsListPanel(ShipVariant::getPermaMods);
        ComponentUtilities.outfitPanelWithTitle(permaModsPanel, "Permanent");
        this.sModsPanel = new HullmodsListPanel(ShipVariant::getSMods);
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
        this.initLayerListeners();
    }

    private void initLayerListeners() {
        EventBus.subscribe(event -> {
            if (event instanceof LayerWasSelected checked) {
                ViewerLayer selected = checked.selected();
                normalModsPanel.refreshListModel(selected);
                permaModsPanel.refreshListModel(selected);
                sModsPanel.refreshListModel(selected);
            }
        });
    }

}

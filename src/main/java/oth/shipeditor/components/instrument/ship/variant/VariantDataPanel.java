package oth.shipeditor.components.instrument.ship.variant;

import oth.shipeditor.communication.EventBus;
import oth.shipeditor.communication.events.components.VariantDataTabSelected;
import oth.shipeditor.components.instrument.ship.variant.hullmods.VariantHullmodsPanel;
import oth.shipeditor.utility.text.StringConstants;
import oth.shipeditor.utility.text.StringValues;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 13.10.2023
 */
public class VariantDataPanel extends JPanel {

    public VariantDataPanel() {
        this.setLayout(new BorderLayout());

        JTabbedPane tabContainer = new JTabbedPane(SwingConstants.BOTTOM);

        VariantMainPanel variantMainPanel = new VariantMainPanel();
        tabContainer.addTab("Main", variantMainPanel);

        VariantHullmodsPanel variantHullmodsPanel = new VariantHullmodsPanel();
        tabContainer.addTab(StringValues.HULLMODS, variantHullmodsPanel);

        VariantWingsPanel variantWingsPanel = new VariantWingsPanel();
        tabContainer.addTab(StringValues.WINGS, variantWingsPanel);

        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabContainer.putClientProperty(StringConstants.JTABBED_PANE_TAB_AREA_ALIGNMENT, "fill");
        this.add(tabContainer, BorderLayout.CENTER);

        tabContainer.addChangeListener(event -> {
            JPanel activePanel = (JPanel) tabContainer.getSelectedComponent();
            boolean hullmodsTabSelected = activePanel instanceof VariantHullmodsPanel;
            boolean wingsTabSelected = activePanel instanceof VariantWingsPanel;

            VariantDataTab selected = VariantDataTab.MAIN;
            if (hullmodsTabSelected) {
                selected = VariantDataTab.HULLMODS;
            } else if (wingsTabSelected) {
                selected = VariantDataTab.WINGS;
            }
            EventBus.publish(new VariantDataTabSelected(selected));
        });
    }

}

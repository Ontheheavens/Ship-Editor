package oth.shipeditor.components.instrument.ship.variant;

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
        tabContainer.addTab("Main", new VariantMainPanel());
        tabContainer.addTab(StringValues.HULLMODS, new VariantHullmodsPanel());
        tabContainer.addTab(StringValues.WINGS, new VariantWingsPanel());
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabContainer.putClientProperty(StringConstants.JTABBED_PANE_TAB_AREA_ALIGNMENT, "fill");
        this.add(tabContainer, BorderLayout.CENTER);
    }

}

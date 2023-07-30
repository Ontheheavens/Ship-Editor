package oth.shipeditor.components.instrument.ship.weaponslots;

import oth.shipeditor.utility.text.StringConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 29.07.2023
 */
public class WeaponSlotsPanel extends JPanel {


    public WeaponSlotsPanel() {
        this.setLayout(new BorderLayout());

        JTabbedPane tabContainer = new JTabbedPane(SwingConstants.BOTTOM);
        tabContainer.addTab("Add Slot", new JPanel());
        tabContainer.addTab("Slot List", new WeaponSlotListPanel());
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabContainer.putClientProperty(StringConstants.JTABBED_PANE_TAB_AREA_ALIGNMENT, "fill");
        this.add(tabContainer, BorderLayout.CENTER);
    }

}
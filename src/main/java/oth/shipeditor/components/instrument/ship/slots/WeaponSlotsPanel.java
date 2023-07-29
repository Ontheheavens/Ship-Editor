package oth.shipeditor.components.instrument.ship.slots;

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
        tabContainer.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");
        this.add(tabContainer, BorderLayout.CENTER);
    }

}

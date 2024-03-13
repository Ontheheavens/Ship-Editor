package oth.shipeditor.components.instrument.ship.slots;

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
        tabContainer.addTab("Slot List", new WeaponSlotListPanel());
        tabContainer.addTab("Add Slot", new SlotCreationPane());
        tabContainer.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabContainer.putClientProperty(StringConstants.TABBED_PANE_TAB_AREA_ALIGNMENT, "fill");
        this.add(tabContainer, BorderLayout.CENTER);
    }

}

package oth.shipeditor.components.instrument;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Ontheheavens
 * @since 06.06.2023
 */
final class HullPointsPanel extends JPanel {

    // TODO: Implement this whole panel. It should include ship center and shield center, and their radii.

    HullPointsPanel() {
        this.setBorder(new EmptyBorder(0, 6, 4, 6));
        LayoutManager layout = new GridLayout(3, 1);
        this.setLayout(layout);

        JPanel hullCenterPanel = new JPanel();

        // Collision panel needs to have a radio button that enables interaction with center point, which is otherwise locked.
        hullCenterPanel.setBorder(BorderFactory.createTitledBorder("Collision"));
        this.add(hullCenterPanel);
        JPanel shieldCenterPanel = new JPanel();
        shieldCenterPanel.setBorder(BorderFactory.createTitledBorder("Shield"));
        this.add(shieldCenterPanel);
    }



}
